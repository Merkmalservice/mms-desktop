package at.researchstudio.sat.mmsdesktop.model.ifc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

public class IfcProjectLine extends IfcLine {
    private static final Pattern projectExtractPattern =
            Pattern.compile("(?>#[0-9]*= IFCPROJECT\\((?<projectInfo>.*)\\))");
    // <- warning might contain whitespaces, trim needed

    private String unitAssignmentId;

    public IfcProjectLine(String line) {
        super(line);

        Matcher matcher = projectExtractPattern.matcher(line);

        if (matcher.find()) {
            String projectInfo = StringUtils.trim(matcher.group("projectInfo"));
            String[] projectInfoArray = projectInfo.split(",");
            unitAssignmentId = projectInfoArray[projectInfoArray.length - 1];
        } else {
            throw new IllegalArgumentException("IfcProject invalid: " + line);
        }
    }

    public String getUnitAssignmentId() {
        return unitAssignmentId;
    }
}
