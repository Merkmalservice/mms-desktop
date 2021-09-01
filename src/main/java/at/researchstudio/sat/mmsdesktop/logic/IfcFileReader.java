package at.researchstudio.sat.mmsdesktop.logic;

import at.researchstudio.sat.mmsdesktop.model.ifc.*;
import at.researchstudio.sat.mmsdesktop.util.IfcFileWrapper;
import be.ugent.progress.TaskProgressListener;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IfcFileReader {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static List<IfcLine> readIfcFile(IfcFileWrapper ifcFile) throws IOException {
        return readIfcFile(ifcFile, null);
    }

    public static List<IfcLine> readIfcFile(
            IfcFileWrapper ifcFile, TaskProgressListener taskProgressListener) throws IOException {
        List<IfcLine> lines = new ArrayList<IfcLine>();

        try (LineIterator it =
                FileUtils.lineIterator(ifcFile.getFile(), StandardCharsets.UTF_8.toString())) {
            boolean updateProgress = Objects.nonNull(taskProgressListener);

            while (it.hasNext()) {
                String line = it.nextLine();
                try {
                    if (line.contains("IFCPROPERTYSINGLEVALUE(")) {
                        lines.add(new IfcSinglePropertyValueLine(line));
                    } else if (line.contains("IFCSIUNIT(")) {
                        lines.add(new IfcSIUnitLine(line));
                    } else if (line.contains("IFCDERIVEDUNITELEMENT(")) {
                        lines.add(new IfcDerivedUnitElementLine(line));
                    } else if (line.contains("IFCDERIVEDUNIT(")) {
                        lines.add(new IfcDerivedUnitLine(line));
                    } else if (line.contains("IFCUNITASSIGMENT(")) {
                        lines.add(new IfcUnitAssignmentLine(line));
                    } else if (line.contains("IFCPROJECT(")) {
                        lines.add(new IfcProjectLine(line));
                    } else {
                        lines.add(new IfcLine(line));
                    }
                } catch (IllegalArgumentException e) {
                    // TODO: FIX PARSING OR IGNORE
                    logger.warn("Couldnt parse Line: " + line + " adding it as IfcLine");
                    lines.add(new IfcLine(line));
                }

                if (updateProgress) {
                    taskProgressListener.notifyProgress(null, line, 0);
                    // updateTitle(line);
                }
            }
        }

        Map<Class<? extends IfcLine>, List<IfcLine>> ifcLinesGrouped =
                lines.parallelStream().collect(Collectors.groupingBy(IfcLine::getClass));

        return lines;
    }
}
