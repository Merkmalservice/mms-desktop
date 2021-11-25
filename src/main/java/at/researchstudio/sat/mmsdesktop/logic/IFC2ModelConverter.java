package at.researchstudio.sat.mmsdesktop.logic;

import be.ugent.IfcSpfReader;
import be.ugent.progress.AbortSignal;
import be.ugent.progress.StatefulTaskProgressListener;
import be.ugent.progress.TaskProgressListener;
import org.apache.jena.atlas.lib.Sink;
import org.apache.jena.ext.com.google.common.base.Throwables;
import org.apache.jena.ext.com.google.common.collect.Streams;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.system.StreamRDFLib;
import org.apache.jena.sparql.graph.GraphFactory;
import org.rdfhdt.hdt.dictionary.TempDictionary;
import org.rdfhdt.hdt.enums.TripleComponentRole;
import org.rdfhdt.hdt.exceptions.NotFoundException;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdt.hdt.HDTVocabulary;
import org.rdfhdt.hdt.hdt.TempHDT;
import org.rdfhdt.hdt.hdt.impl.HDTImpl;
import org.rdfhdt.hdt.hdt.impl.ModeOfLoading;
import org.rdfhdt.hdt.hdt.impl.TempHDTImpl;
import org.rdfhdt.hdt.header.HeaderUtil;
import org.rdfhdt.hdt.listener.ProgressListener;
import org.rdfhdt.hdt.options.HDTSpecification;
import org.rdfhdt.hdt.rdf.parsers.JenaNodeFormatter;
import org.rdfhdt.hdt.triples.TempTriples;
import org.rdfhdt.hdt.triples.TripleString;
import org.rdfhdt.hdt.util.listener.ListenerUtil;
import org.rdfhdt.hdtjena.HDTGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.NotificationEmitter;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.management.*;
import java.nio.file.FileSystems;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class IFC2ModelConverter {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String BASE_URI = "https://researchstudio.at/";
    private static final double TENURED_GEN_MEMORY_THRESHOLD = 0.95;

    public static Model readFromFile(File ifcFile, TaskProgressListener taskProgressListener)
            throws IOException {
        AbortSignal abortSignal = new AbortSignal();
        IfcSpfReader ifcSpfReader = new IfcSpfReader();
        ifcSpfReader.setup(ifcFile.getAbsolutePath(), taskProgressListener, abortSignal);
        ifcSpfReader.setUseUuidsForGeneratedResources(true);
        ifcSpfReader.setAvoidDuplicatePropertyResources(false);
        ifcSpfReader.setRemoveDuplicates(false);
        SinkToIterator sinkToIterator = new SinkToIterator();
        AtomicReference<Model> modelRef = new AtomicReference<>();
        int attempt = 0;
        setupLowMemoryChecker(abortSignal);
        do {
            attempt++;
            abortSignal.reset();
            sinkToIterator.reset();
            Thread ifcReaderThread = createIfcReaderThread(ifcFile, ifcSpfReader, sinkToIterator);
            Thread modelGeneratorThread = null;
            if (attempt == 1) {
                modelGeneratorThread =
                        createModelGeneratorThreadForOrdinaryModel(
                                taskProgressListener, abortSignal, sinkToIterator, modelRef);
            } else if (attempt == 2) {
                ((StatefulTaskProgressListener) taskProgressListener).reset();
                taskProgressListener.notifyProgress(
                        "Reading IFC into plain RDF model",
                        "failed due to insufficient memory, retrying map-reduce HDT generation",
                        0);
                modelGeneratorThread =
                        createModelGeneratorThreadForHdtModelUsingMapReduce(
                                taskProgressListener, abortSignal, sinkToIterator, modelRef);
            }
            modelGeneratorThread.start();
            ifcReaderThread.start();
            try {
                ifcReaderThread.join();
                modelGeneratorThread.join();
            } catch (InterruptedException e) {
                throw new CancellationException("Cancelled while waiting for converter thread");
            }
        } while (modelRef.get() == null && attempt <= 1);

        return modelRef.get();
    }

    private static void setupLowMemoryChecker(AbortSignal abortSignal) {
        MemoryPoolMXBean tenuredGen =
                ManagementFactory.getMemoryPoolMXBeans().stream()
                        .filter(pool -> pool.getType() == MemoryType.HEAP)
                        .filter(MemoryPoolMXBean::isUsageThresholdSupported)
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Can't find tenured generation MemoryPoolMXBean"));
        MemoryUsage usage = tenuredGen.getUsage();
        tenuredGen.setCollectionUsageThreshold(
                (int) Math.floor(usage.getMax() * TENURED_GEN_MEMORY_THRESHOLD));
        NotificationEmitter notificationEmitter =
                (NotificationEmitter) ManagementFactory.getMemoryMXBean();
        notificationEmitter.addNotificationListener(
                (notification, handback) -> {
                    if (MemoryNotificationInfo.MEMORY_COLLECTION_THRESHOLD_EXCEEDED.equals(
                            notification.getType())) {
                        // Log, send an alert or whatever makes sense in your situation
                        System.err.println(
                                "Running low on memory, aborting IFC to Model generation");
                        abortSignal.abort();
                    }
                },
                null,
                null);
    }

    private static Thread createModelGeneratorThreadForOrdinaryModel(
            TaskProgressListener taskProgressListener,
            AbortSignal abortSignal,
            SinkToIterator sinkToIterator,
            AtomicReference<Model> modelRef) {
        return new Thread(
                () -> {
                    Model model =
                            collectAsOrdinaryModel(
                                    taskProgressListener, sinkToIterator, abortSignal);
                    if (model != null) {
                        // model is null if the computation was aborted
                        modelRef.set(model);
                        logger.info("Finished loading ordinary model");
                        return;
                    }
                },
                "RDF Model Generator");
    }

    private static Thread createModelGeneratorThreadForHdtModelUsingMapReduce(
            TaskProgressListener taskProgressListener,
            AbortSignal abortSignal,
            SinkToIterator sinkToIterator,
            AtomicReference<Model> modelRef) {
        return new Thread(
                () -> {
                    String tmpdir = System.getProperty("java.io.tmpdir");
                    File tempDirectory =
                            new File(
                                    tmpdir
                                            + FileSystems.getDefault().getSeparator()
                                            + System.currentTimeMillis());
                    tempDirectory.mkdirs();
                    tempDirectory.deleteOnExit();
                    Model model =
                            collectAsHdtModelMapReduce(
                                    taskProgressListener,
                                    sinkToIterator,
                                    tempDirectory,
                                    abortSignal);
                    if (model != null) {
                        modelRef.set(model);
                        logger.info("Finished generating HDT via map-reduce");
                    }
                },
                "RDF Model Generator");
    }

    private static Thread createIfcReaderThread(
            File ifcFile, IfcSpfReader ifcSpfReader, SinkToIterator sinkToIterator) {
        return new Thread(
                () -> {
                    try {
                        ifcSpfReader.convert(
                                ifcFile.getAbsolutePath(),
                                StreamRDFLib.sinkTriples(sinkToIterator),
                                BASE_URI);
                    } catch (IOException e) {
                        throw new RuntimeException(
                                "Error converting IFC to RDF: " + e.getMessage(), e);
                    } finally {
                        sinkToIterator.close();
                    }
                    logger.info("Finished extracting triples");
                },
                "IFC2RDF");
    }

    private static Model collectAsOrdinaryModel(
            TaskProgressListener taskProgressListener,
            SinkToIterator sinkToIterator,
            AbortSignal abortSignal) {
        Iterator<Triple> it = sinkToIterator.iterator();
        Graph graph = GraphFactory.createGraphMem();
        int i = 0;
        while (it.hasNext()) {
            if (++i % 1000000 == 0) {
                taskProgressListener.notifyProgress(
                        "Collecting triples", String.format("Collected %d triples", i), 0);
            }
            graph.add(it.next());
        }
        if (abortSignal.isAborted()) {
            taskProgressListener.notifyFailed("Collecting triples");
            return null;
        }
        taskProgressListener.notifyFinished("Collecting triples");
        return ModelFactory.createModelForGraph(graph);
    }

    private static Model collectAsHdtModel(
            TaskProgressListener taskProgressListener,
            SinkToIterator sinkToIterator,
            File tempDirectory,
            AbortSignal abortSignal) {
        String collectTaskName = "Collecting triples for HDT model";
        String compressTaskName = "Compressing HDT model";
        try {
            Iterator<Triple> it = sinkToIterator.iterator();
            HDT hdt =
                    HDTManager.generateHDT(
                            new Iterator<>() {
                                @Override
                                public boolean hasNext() {
                                    boolean doesHaveNext = it.hasNext();
                                    if (!doesHaveNext) {
                                        taskProgressListener.notifyFinished(collectTaskName);
                                        taskProgressListener.notifyProgress(
                                                compressTaskName, "Compression in progress", 0);
                                    }
                                    return doesHaveNext;
                                }

                                @Override
                                public TripleString next() {
                                    return toTripleString(it.next());
                                }
                            },
                            BASE_URI,
                            new HDTSpecification(),
                            (level, message) ->
                                    taskProgressListener.notifyProgress(
                                            collectTaskName, message, level));
            String createModelTaskName = "Creating model";
            if (abortSignal.isAborted()) {
                taskProgressListener.notifyFailed(collectTaskName);
                taskProgressListener.notifyFailed(compressTaskName);
                return null;
            }
            taskProgressListener.notifyFinished(compressTaskName);
            taskProgressListener.notifyProgress(createModelTaskName, "started", 0);
            Model model = toModel(hdt);
            taskProgressListener.notifyFinished(createModelTaskName);
            return model;
        } catch (Exception e) {
            throw new RuntimeException("Error converting to HDT", e);
        }
    }

    private static Model collectAsHdtModelMapReduce(
            TaskProgressListener taskProgressListener,
            SinkToIterator sinkToIterator,
            File tempDirectory,
            AbortSignal abortSignal) {
        try {
            StreamToHdt streamToHdt = new StreamToHdt(taskProgressListener, abortSignal);
            Streams.stream(sinkToIterator.iterator()).parallel().forEach(streamToHdt::addTriple);
            streamToHdt.finishAllChunks();
            if (abortSignal.isAborted()) {
                return null;
            }
            AtomicInteger reduceSteps = new AtomicInteger(0);
            Set<HdtChunkWriter> chunks = streamToHdt.getFinishedChunks();
            int totalReduceSteps = chunks.size() - 1;
            Optional<HDT> resultOpt =
                    chunks.stream()
                            .parallel()
                            .map(HdtChunkWriter::getHdt)
                            .reduce(
                                    (left, right) -> {
                                        try {
                                            if (left.getTriples().getNumberOfElements() == 0) {
                                                return right;
                                            }
                                            if (right == null
                                                    || right.getTriples().getNumberOfElements()
                                                            == 0) {
                                                return left;
                                            }
                                            int reducerStep = reduceSteps.incrementAndGet();
                                            ProgressListener listener =
                                                    (level, message) -> {
                                                        float reduceLevel =
                                                                (float) reducerStep
                                                                        / (float) totalReduceSteps;
                                                        if (taskProgressListener != null) {
                                                            taskProgressListener.notifyProgress(
                                                                    "Combining HDT chunks",
                                                                    String.format(
                                                                            "step %d/%d: %s",
                                                                            reducerStep,
                                                                            totalReduceSteps,
                                                                            message),
                                                                    reduceLevel);
                                                        }
                                                    };
                                            File location =
                                                    new File(
                                                            tempDirectory,
                                                            UUID.randomUUID().toString());
                                            location.mkdir();
                                            HDTImpl merged = new HDTImpl(new HDTSpecification());
                                            merged.cat(
                                                    location.getAbsolutePath() + "/",
                                                    left,
                                                    right,
                                                    listener);
                                            left.close();
                                            right.close();
                                            return merged;
                                        } catch (Throwable e) {
                                            throw new RuntimeException(
                                                    "Error combining chunks ", e);
                                        }
                                    });
            taskProgressListener.notifyProgress("Combining HDT chunks", "finished", 1f);
            HDT result = resultOpt.orElse(null);
            return toModel(result);
        } catch (Exception e) {
            throw new RuntimeException("Error writing HDT", e);
        }
    }

    private static Model toModel(HDT hdt) {
        if (hdt == null) {
            return null;
        } else {
            Graph graph = new HDTGraph(hdt);
            return ModelFactory.createModelForGraph(graph);
        }
    }

    private static class SinkToIterator implements Sink<Triple> {
        private final BlockingQueue<Triple> queue = new ArrayBlockingQueue<>(5000);
        private final AtomicBoolean closed = new AtomicBoolean(false);

        @Override
        public void send(Triple triple) {
            try {
                queue.put(triple);
            } catch (InterruptedException e) {
                throw new RuntimeException("Interrupted while waiting for queue space", e);
            }
        }

        public void reset() {
            this.queue.clear();
            this.closed.set(false);
        }

        @Override
        public void flush() {}

        @Override
        public void close() {
            this.closed.set(true);
        }

        public Iterator<Triple> iterator() {
            return new Iterator<>() {
                private static final long INITIAL_TIMEOUT = 1;
                private Triple next = null;

                @Override
                public boolean hasNext() {
                    long backoffTimeout = INITIAL_TIMEOUT;
                    int timeouts = 0;
                    boolean loggedTimeoutInfo = false;
                    do {
                        this.next = waitForNext(backoffTimeout);
                        if (this.next == null) {
                            logger.info("hit timeout!");
                            timeouts++;
                            if (timeouts > 1) {
                                // the first timeout it hit when the producer is
                                // finished and the following events happen:
                                // 1. producer submits last triple
                                // 2. consumer calls hasNext(), processes triple
                                // 3. consumer calls hasNext() again
                                // 4. producer calls close()
                                // with this logic, we allow one INITIAL_TIMEOUT
                                // for the time between 3 and 4
                                logger.warn(
                                        "Timeout of {} seconds reached while waiting for next triple. ",
                                        backoffTimeout);
                                logger.warn(
                                        "The triple stream is not closed yet, which means one of the following:");
                                logger.warn(
                                        "   a) the producer is blocked or busy doing something else, but will continue producing triples");
                                logger.warn(
                                        "   b) the producer is done but a bit slow to call close()");
                                logger.warn(
                                        "   c) the producer is done but due to a bug will never call close()");
                                logger.warn(
                                        "   ... Trying again with twice the timeout. If this message is shown many times over, it's probably c)");
                                loggedTimeoutInfo = true;
                            }
                        }
                        backoffTimeout *= 2;
                    } while (this.next == null && (queue.size() > 0 || !closed.get()));
                    if (loggedTimeoutInfo) {
                        if (this.next != null) {
                            logger.warn(
                                    "Got another triple - we were seeing a): the producer was busy/blocked and is now back to normal");
                        } else if (closed.get()) {
                            logger.warn(
                                    "The producer now did close the triple stream, we were seeing b): the producer was just a bit slow to call close()");
                        }
                    }
                    return this.next != null;
                }

                @Override
                public Triple next() {
                    if (this.next == null) {
                        // if the client did not call hasNext() prior to next(),
                        // we have to do it here
                        if (!hasNext()) {
                            return null;
                        }
                    }
                    Triple result = this.next;
                    this.next = null;
                    return result;
                }

                private Triple waitForNext(long timeout) {
                    Triple t;
                    try {
                        t = queue.poll(timeout, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(
                                "Got interrupted while waiting for next triple. Maybe the producer forgot to call close() on the sink?",
                                e);
                    }
                    return t; // may be null, which indicates that the timeout was hit.
                }
            };
        }
    }

    /** Holds the data corresponding to one temp file output. Not thread-safe. */
    private static class HdtChunkWriter {
        private final String baseUri;
        private long num;
        private long size;
        private final long maxSize;
        private final int chunkIndex;
        private TempHDT modHDT;
        private HDTImpl hdt;
        private TempDictionary dictionary;
        private TempTriples triples;
        private final ProgressListener listener;
        private final AbortSignal abortSignal;

        public HdtChunkWriter(
                long maxSize,
                String baseUri,
                ProgressListener listener,
                int chunkIndex,
                AbortSignal abortSignal)
                throws IOException {
            this.maxSize = maxSize;
            this.num = 0;
            this.size = 0;
            this.baseUri = baseUri;
            this.listener = listener;
            this.chunkIndex = chunkIndex;
            this.abortSignal = abortSignal;
            init();
        }

        private void init() {
            // Create Modifiable Instance
            modHDT = new TempHDTImpl(new HDTSpecification(), this.baseUri, ModeOfLoading.ONE_PASS);
            dictionary = modHDT.getDictionary();
            triples = modHDT.getTriples();
            // Load RDF in the dictionary and generate triples
            dictionary.startProcessing();
            long size = 0;
        }

        public void close() throws IOException {
            dictionary.endProcessing();
            // Reorganize both the dictionary and the triples
            modHDT.reorganizeDictionary(listener);
            modHDT.reorganizeTriples(listener);
            modHDT.getHeader().insert("_:statistics", HDTVocabulary.ORIGINAL_SIZE, size);
            // Convert to HDT
            hdt = new HDTImpl(new HDTSpecification());
            hdt.loadFromModifiableHDT(modHDT, listener);
            hdt.populateHeaderStructure(modHDT.getBaseURI());

            // Add file size to Header
            try {
                long originalSize =
                        HeaderUtil.getPropertyLong(
                                modHDT.getHeader(), "_:statistics", HDTVocabulary.ORIGINAL_SIZE);
                hdt.getHeader().insert("_:statistics", HDTVocabulary.ORIGINAL_SIZE, originalSize);
            } catch (NotFoundException e) {
                logger.error(Throwables.getStackTraceAsString(e));
            }

            modHDT.close();
            modHDT = null;
            triples.clear();
            triples = null;
            dictionary.clear();
            dictionary.close();
            dictionary = null;
        }

        public HDT getHdt() {
            if (abortSignal.isAborted()) {
                return null;
            }
            return hdt;
        }

        public void addTriple(Triple triple) throws IOException {
            if (abortSignal.isAborted()) {
                return;
            }
            TripleString ts = toTripleString(triple);
            triples.insert(
                    dictionary.insert(ts.getSubject(), TripleComponentRole.SUBJECT),
                    dictionary.insert(ts.getPredicate(), TripleComponentRole.PREDICATE),
                    dictionary.insert(ts.getObject(), TripleComponentRole.OBJECT));
            num++;
            size +=
                    ts.getSubject().length()
                            + ts.getPredicate().length()
                            + ts.getObject().length()
                            + 4; // Spaces and final dot
            ListenerUtil.notifyCond(listener, "Loaded " + num + " triples", num, 0, 100);
        }

        public boolean isMaxChunkSizeReached() {
            return this.size >= maxSize;
        }
    }

    private static TripleString toTripleString(Triple triple) {
        String subject = JenaNodeFormatter.format(triple.getSubject());
        String predicate = JenaNodeFormatter.format(triple.getPredicate());
        String object = JenaNodeFormatter.format(triple.getObject());
        return new TripleString(subject, predicate, object);
    }

    private static class StreamToHdt {
        private static final long MAX_CHUNK_SIZE = 100000000;
        Map<Thread, HdtChunkWriter> writerMap;
        final Set<HdtChunkWriter> finishedChunks;
        private final AtomicInteger chunks;
        private final TaskProgressListener taskProgressListener;
        private final AbortSignal abortSignal;

        public StreamToHdt(TaskProgressListener taskProgressListener, AbortSignal abortSignal) {
            this.taskProgressListener = taskProgressListener;
            this.writerMap = new ConcurrentHashMap<>();
            this.finishedChunks = Collections.synchronizedSet(new HashSet<>());
            this.chunks = new AtomicInteger(0);
            this.abortSignal = abortSignal;
        }

        public void addTriple(Triple triple) {
            if (abortSignal.isAborted()) {
                return;
            }
            try {
                HdtChunkWriter writer = getChunkWriterAndPossiblySwapForNew();
                writer.addTriple(triple);
            } catch (Exception e) {
                throw new RuntimeException("Error writing triple", e);
            }
        }

        private HdtChunkWriter getChunkWriterAndPossiblySwapForNew() throws Exception {
            HdtChunkWriter writer =
                    writerMap.computeIfAbsent(
                            Thread.currentThread(),
                            t -> {
                                try {
                                    int chunkIndex = chunks.incrementAndGet();
                                    return new HdtChunkWriter(
                                            MAX_CHUNK_SIZE,
                                            BASE_URI,
                                            (level, message) -> {
                                                if (taskProgressListener != null) {
                                                    taskProgressListener.notifyProgress(
                                                            "Generating HDT chunks",
                                                            String.format(
                                                                    "Status of chunk %d: %s",
                                                                    chunkIndex, message),
                                                            0f);
                                                }
                                            },
                                            chunkIndex,
                                            abortSignal);
                                } catch (Exception e) {
                                    throw new RuntimeException(
                                            "Cannot create temporary hdt file", e);
                                }
                            });
            if (writer != null) {
                if (writer.isMaxChunkSizeReached()) {
                    writer.close();
                    finishedChunks.add(writer);
                    taskProgressListener.notifyProgress(
                            "Generating HDT chunks",
                            String.format("Status of chunk %d: finished", writer.chunkIndex),
                            0f);
                    writer = null;
                    writerMap.remove(Thread.currentThread());
                    return getChunkWriterAndPossiblySwapForNew();
                }
                return writer;
            }
            return null;
        }

        public void finishAllChunks() {
            if (abortSignal.isAborted()) {
                taskProgressListener.notifyFailed("Generating HDT chunks");
                this.writerMap.clear();
            }
            this.writerMap
                    .values()
                    .forEach(
                            hdtChunkWriter -> {
                                try {
                                    hdtChunkWriter.close();
                                    finishedChunks.add(hdtChunkWriter);
                                } catch (Exception e) {
                                    throw new RuntimeException("Error closing chunk writer", e);
                                }
                            });
            taskProgressListener.notifyProgress("Generating HDT chunks", "finished", 1f);
            this.writerMap.clear();
        }

        public Set<HdtChunkWriter> getFinishedChunks() {
            return this.finishedChunks;
        }

        public void clear() {
            this.writerMap = null;
        }
    }
}
