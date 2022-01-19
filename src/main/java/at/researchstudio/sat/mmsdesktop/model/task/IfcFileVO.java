package at.researchstudio.sat.mmsdesktop.model.task;

import at.researchstudio.sat.merkmalservice.ifc.ParsedIfcFile;
import at.researchstudio.sat.merkmalservice.ifc.model.IfcLine;
import at.researchstudio.sat.merkmalservice.model.Feature;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class IfcFileVO implements Serializable {
    private final ParsedIfcFile parsedIfcFile;
    private final String logOutput;

    public IfcFileVO(ParsedIfcFile parsedIfcFile) {
        this(parsedIfcFile, null);
    }

    public IfcFileVO(ParsedIfcFile parsedIfcFile, String logOutput) {
        this.parsedIfcFile = parsedIfcFile;
        this.logOutput = logOutput;
    }

    public ParsedIfcFile getParsedIfcFile() {
        return parsedIfcFile;
    }

    public List<IfcLine> getLines() {
        return parsedIfcFile.getLines();
    }

    public Map<Integer, IfcLine> getDataLines() {
        return parsedIfcFile.getDataLines();
    }

    public List<Feature> getExtractedFeatures() {
        return parsedIfcFile.getFeatures();
    }

    public String getLogOutput() {
        return logOutput;
    }
}
