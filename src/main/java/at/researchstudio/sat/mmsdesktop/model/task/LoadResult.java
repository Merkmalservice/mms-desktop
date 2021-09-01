package at.researchstudio.sat.mmsdesktop.model.task;

import at.researchstudio.sat.mmsdesktop.model.ifc.IfcLine;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class LoadResult implements Serializable {
    private final List<IfcLine> lines;
    private final Map<String, IfcLine> dataLines = new HashMap<>();

    public LoadResult(List<IfcLine> lines) {
        this.lines = lines;
        if (Objects.nonNull(lines) && lines.size() > 0) {
            lines.stream()
                    .filter(line -> Objects.nonNull(line) && line.getId() != null)
                    .map(line -> dataLines.put(line.getId(), line));
        }
    }

    public List<IfcLine> getLines() {
        return lines;
    }

    public Map<String, IfcLine> getDataLines() {
        return dataLines;
    }
}
