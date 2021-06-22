package at.researchstudio.sat.mmsdesktop.logic;

import be.ugent.IfcSpfReader;
import org.rdfhdt.hdt.enums.RDFNotation;
import org.rdfhdt.hdt.exceptions.ParserException;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdt.hdt.HDTManager;
import org.rdfhdt.hdt.options.HDTSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;

public class IFC2HDTConverter {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String BASE_URI = "https://researchstudio.at/";

    public static HDT readFromFile(boolean keepTempFiles, File ifcFile, File outputFile) throws IOException, ParserException {
        IfcSpfReader r = new IfcSpfReader();

        r.setup(ifcFile.getAbsolutePath());
        r.convert(ifcFile.getAbsolutePath(), outputFile.getAbsolutePath(), BASE_URI);

        HDT hdt = HDTManager.generateHDT(outputFile.getAbsolutePath(), BASE_URI, RDFNotation.TURTLE, new HDTSpecification(), null);

        if (!keepTempFiles && !outputFile.delete()) {

            logger.error("Could not delete temp-file: " + outputFile.getAbsolutePath() + " try removing it manually later...");
        }
        return hdt;
    }
}
