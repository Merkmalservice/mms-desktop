package at.researchstudio.sat.mmsdesktop.model.task;

import at.researchstudio.sat.merkmalservice.ifc.ParsedIfcFile;
import at.researchstudio.sat.merkmalservice.ifc.model.IfcLine;
import at.researchstudio.sat.merkmalservice.model.Feature;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class LoadResult implements Serializable {
    private final ParsedIfcFile parsedIfcFile;

    public LoadResult(ParsedIfcFile parsedIfcFile) {
        this.parsedIfcFile = parsedIfcFile;
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
}
