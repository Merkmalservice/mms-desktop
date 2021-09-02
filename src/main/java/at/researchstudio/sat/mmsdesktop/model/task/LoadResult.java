package at.researchstudio.sat.mmsdesktop.model.task;

import at.researchstudio.sat.mmsdesktop.model.ifc.IfcLine;
import at.researchstudio.sat.mmsdesktop.model.ifc.ParsedIfcFile;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class LoadResult implements Serializable {
    private final ParsedIfcFile parsedIfcFile;
    private final List<IfcLine> lines;
    private final Map<String, IfcLine> dataLines;

    public LoadResult(ParsedIfcFile parsedIfcFile) {
        this.parsedIfcFile = parsedIfcFile;
        this.lines = parsedIfcFile.getLines();
        this.dataLines = parsedIfcFile.getDataLines();
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
}
