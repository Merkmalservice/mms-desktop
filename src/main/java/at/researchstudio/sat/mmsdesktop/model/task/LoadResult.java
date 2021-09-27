package at.researchstudio.sat.mmsdesktop.model.task;

import at.researchstudio.sat.merkmalservice.model.Feature;
import at.researchstudio.sat.mmsdesktop.model.ifc.IfcLine;
import at.researchstudio.sat.mmsdesktop.model.ifc.ParsedIfcFile;
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

    public Map<String, IfcLine> getDataLines() {
        return parsedIfcFile.getDataLines();
    }

    public Map<Class<? extends IfcLine>, List<IfcLine>> getDataLinesByClass() {
        return parsedIfcFile.getDataLinesByClass();
    }

    public List<Feature> getExtractedFeatures() {
        return parsedIfcFile.getFeatures();
    }
}
