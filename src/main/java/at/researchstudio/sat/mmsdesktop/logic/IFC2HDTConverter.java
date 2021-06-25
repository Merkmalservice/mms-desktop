package at.researchstudio.sat.mmsdesktop.logic;

import be.ugent.IfcSpfReader;
import be.ugent.progress.TaskProgressListener;
import org.apache.jena.atlas.lib.Sink;
import org.apache.jena.ext.com.google.common.base.Throwables;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.system.StreamRDFLib;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdt.listener.ProgressListener;
import org.rdfhdt.hdt.options.HDTSpecification;
import org.rdfhdt.hdt.triples.TripleString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class IFC2HDTConverter {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String BASE_URI = "https://researchstudio.at/";

    public static HDT readFromFile(File ifcFile, TaskProgressListener taskProgressListener) throws IOException {
        IfcSpfReader r = new IfcSpfReader();
        r.setup(ifcFile.getAbsolutePath(), taskProgressListener);
        SinkToIterator sinkToIterator = new SinkToIterator();
        Thread converterThread = new Thread(() -> {
            try {
                r.convert(ifcFile.getAbsolutePath(), StreamRDFLib.sinkTriples(sinkToIterator), BASE_URI);
            } catch (IOException e) {
                throw new RuntimeException("Error converting IFC to RDF: " + e.getMessage(), e);
            } finally {
                sinkToIterator.close();
            }
        }, "IFC2RDF");
        AtomicReference<HDT> hdt = new AtomicReference<>();
        Thread hdtWriterThread = new Thread(() -> {
            try {
                /*File hdtFile = File.createTempFile("ifc-hdt","hdt");
                System.out.println("writing to " + hdtFile);
                TripleWriter writer = null;
                try {
                    writer = HDTManager
                                    .getHDTWriter(hdtFile.getAbsolutePath(), BASE_URI, new HDTSpecification());
                    Iterator<TripleString> it = sinkToIterator.iterator();
                    while (it.hasNext()) {
                        writer.addTriple(it.next());
                    }
                    hdt.set(HDTManager.loadHDT(hdtFile.getAbsolutePath(), new ProgressListener() {
                        @Override public void notifyProgress(float level, String message) {
                            taskProgressListener.notifyProgress("Reading back generated HDT file", message, level);
                        }
                    }));
                } finally {
                    if (writer != null) {
                        writer.close();
                    }
                }*/

                hdt.set(HDTManager.generateHDT(sinkToIterator.iterator(), BASE_URI, new HDTSpecification(),
                                new ProgressListener() {
                                    @Override public void notifyProgress(float level, String message) {
                                        taskProgressListener.notifyProgress("writing HDT", message, level);
                                    }
                                }));
            } catch (Exception e) {
                logger.error(Throwables.getStackTraceAsString(e));
            }
        }, "HDT Writer");
        hdtWriterThread.start();
        converterThread.start();
        try {
            converterThread.join();
            hdtWriterThread.join();
        } catch (InterruptedException e) {
            throw new CancellationException("Cancelled while waiting for converter thread");
        }
        return hdt.get();
    }

    private static class SinkToIterator implements Sink<Triple> {
        private BlockingQueue<Triple> queue = new LinkedBlockingQueue<>();
        private AtomicBoolean closed = new AtomicBoolean(false);

        @Override
        public void send(Triple triple) {
            queue.add(triple);
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() {
            this.closed.set(true);
        }

        public Iterator<TripleString> iterator() {
            return new Iterator<>() {
                private static final long INITIAL_TIMEOUT = 1;
                private TripleString next = null;

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
                                logger.warn("Timeout of {} seconds reached while waiting for next triple. ", backoffTimeout);
                                logger.warn("The triple stream is not closed yet, which means one of the follwing:");
                                logger.warn("   a) the producer is blocked or busy doing something else, but will continue producing triples");
                                logger.warn("   b) the producer is done but a bit slow to call close()");
                                logger.warn("   c) the producer is done but due to a bug will never call close()");
                                logger.warn("   ... Trying again with twice the timeout. If this message is shown many times over, it's probably c)");
                                loggedTimeoutInfo = true;
                            }
                        }
                        backoffTimeout *= 2;
                    } while (this.next == null && (queue.size() > 0 || !closed.get()));
                    if (loggedTimeoutInfo) {
                        if (this.next != null) {
                            logger.warn("Got another triple - we were seeing a): the producer was busy/blocked and is now back to normal");
                        } else if (closed.get()) {
                            logger.warn("The producer now did close the triple stream, we were seeing b): the producer was just a bit slow to call close()");
                        }
                    }
                    return this.next != null;
                }

                @Override
                public TripleString next() {
                    if (this.next == null) {
                        // if the client did not call hasNext() prior to next(),
                        // we have to do it here
                        if (!hasNext()) {
                            return null;
                        }
                    }
                    TripleString result = this.next;
                    this.next = null;
                    return result;
                }

                private TripleString waitForNext(long timeout) {
                    Triple t;
                    try {
                        t = queue.poll(timeout, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        throw new RuntimeException("Got interrupted while waiting for next triple. Maybe the producer forgot to call close() on the sink?", e);
                    }
                    if (t == null) {
                        return null;
                    }
                    return new TripleString(t.getSubject().toString(), t.getPredicate().toString(), t.getObject().toString());
                }
            };
        }
    }
}
