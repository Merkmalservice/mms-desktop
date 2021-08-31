package at.researchstudio.sat.mmsdesktop.model.task;

import java.io.Serializable;
import java.util.List;

public class LoadResult implements Serializable {
    private final List<String> lines;

    public LoadResult(List<String> lines) {
        this.lines = lines;
    }

    public List<String> getLines() {
        return lines;
    }
}
