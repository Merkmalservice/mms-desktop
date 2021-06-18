package at.researchstudio.sat.mmsdesktop.logic;

import be.ugent.IfcSpfReader;
import org.apache.jena.atlas.lib.Sink;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.system.StreamRDFLib;
import org.rdfhdt.hdt.exceptions.ParserException;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdt.options.HDTSpecification;
import org.rdfhdt.hdt.triples.TripleString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Iterator;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class IFC2HDTConverter {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String BASE_URI = "https://researchstudio.at/";

    public static HDT readFromFile(File ifcFile)
            throws IOException, ParserException {
        IfcSpfReader r = new IfcSpfReader();
        r.setup(ifcFile.getAbsolutePath());
        SinkToIterator sinkToIterator = new SinkToIterator();
        Thread converterThread = new Thread(() ->
        {
            try {
                r.convert(ifcFile.getAbsolutePath(), StreamRDFLib.sinkTriples(sinkToIterator), BASE_URI);
            } catch (IOException e) {
                throw new RuntimeException("Error converting IFC to RDF: " + e.getMessage(), e);
            }
        }, "IFC2RDF");
        converterThread.start();
        HDT hdt =
                HDTManager.generateHDT(
                        sinkToIterator.iterator(),
                        BASE_URI,
                        new HDTSpecification(),
                        null);
        try {
            converterThread.join();
        } catch (InterruptedException e) {
            throw new CancellationException("Cancelled while waiting for converter thread");
        }
        return hdt;
    }

    private static class SinkToIterator implements Sink<Triple> {
        private BlockingQueue<Triple> queue = new LinkedBlockingQueue();
        private AtomicBoolean closed = new AtomicBoolean(false);

        @Override public void send(Triple triple) {
            queue.add(triple);
        }

        @Override public void flush() {
        }

        @Override public void close() {
            this.closed.set(true);
        }   

        public Iterator<TripleString> iterator(){
            return new Iterator<TripleString>() {
                @Override public boolean hasNext() {
                    return ! closed.get();
                }

                @Override public TripleString next() {
                    Triple t = null;
                    try {
                        t = queue.take();
                    } catch (InterruptedException e) {
                        throw new RuntimeException("Got interrupted while waiting for next triple. Maybe the producer forgot to call close() on the sink?",e);
                    }
                    return new TripleString(
                                    t.getSubject().toString(),
                                    t.getPredicate().toString(),
                                    t.getObject().toString()
                    );
                }
            };
        }
    }
}
