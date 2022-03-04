package at.researchstudio.sat.merkmalservice.ifc;

import at.researchstudio.sat.merkmalservice.ifc.convert.support.change.HighlevelChange;
import at.researchstudio.sat.merkmalservice.ifc.convert.support.change.HighlevelChangeFormatter;

import java.io.*;
import java.util.List;

public abstract class IfcFileWriter {

    public static void write(ParsedIfcFile parsedIfcFile, String outFile)
            throws FileNotFoundException {
        write(parsedIfcFile, new File(outFile));
    }

    public static void write(ParsedIfcFile parsedIfcFile, File outFile)
            throws FileNotFoundException {
        try (PrintWriter writer = new PrintWriter(new FileOutputStream(outFile))) {
            write(parsedIfcFile, writer);
        }
    }

    public static void write(ParsedIfcFile parsedIfcFile, OutputStream out) {
        try (PrintWriter writer = new PrintWriter(out)) {
            write(parsedIfcFile, new PrintWriter(out));
        }
    }

    public static void write(ParsedIfcFile parsedIfcFile, Writer writer) {
        write(parsedIfcFile, new PrintWriter(writer));
    }

    public static void write(ParsedIfcFile parsedIfcFile, PrintWriter printWriter) {
        parsedIfcFile.getLines().forEach(line -> printWriter.println(line.getModifiedLine()));
    }

    public static void writeChangeLog(List<HighlevelChange> changes, BufferedWriter writer) throws IOException {
        HighlevelChangeFormatter changeFormatter = new HighlevelChangeFormatter();
        for(HighlevelChange change: changes) {
            writer.write(changeFormatter.format(change));
        }
    }
}
