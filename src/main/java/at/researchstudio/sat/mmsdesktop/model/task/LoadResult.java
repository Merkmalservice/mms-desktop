package at.researchstudio.sat.mmsdesktop.model.task;

import at.researchstudio.sat.merkmalservice.model.Feature;
import at.researchstudio.sat.mmsdesktop.model.ifc.IfcLine;
import at.researchstudio.sat.mmsdesktop.model.ifc.ParsedIfcFile;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class LoadResult implements Serializable {
    private final ParsedIfcFile parsedIfcFile;
    private final List<IfcLine> lines;
    private final Map<String, IfcLine> dataLines;
    private final Map<Class<? extends IfcLine>, List<IfcLine>> dataLinesByClass;
    private final List<Feature> extractedFeatures;

    public LoadResult(ParsedIfcFile parsedIfcFile) {
        this.parsedIfcFile = parsedIfcFile;
        this.lines = parsedIfcFile.getLines();
        this.dataLines = parsedIfcFile.getDataLines();
        this.dataLinesByClass = parsedIfcFile.getDataLinesByClass();
        this.extractedFeatures = parsedIfcFile.getFeatures();
    }

    public ParsedIfcFile getParsedIfcFile() {
        return parsedIfcFile;
    }

    public List<IfcLine> getLines() {
        return lines;
    }

    public Map<String, IfcLine> getDataLines() {
        return dataLines;
    }

    public Map<Class<? extends IfcLine>, List<IfcLine>> getDataLinesByClass() {
        return dataLinesByClass;
    }

    public List<Feature> getExtractedFeatures() {
        return extractedFeatures;
    }
}
