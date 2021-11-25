package at.researchstudio.sat.mmsdesktop.model.ifc;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IfcProjectLine extends IfcLine {
    public static final String IDENTIFIER = "IFCPROJECT";
    private static final Pattern extractPattern =
            Pattern.compile("(?>#[0-9]*= IFCPROJECT\\((?<projectInfo>.*)\\))");
    // <- warning might contain whitespaces, trim needed

    private int unitAssignmentId;

    public IfcProjectLine(String line) {
        super(line);

        Matcher matcher = extractPattern.matcher(line);

        if (matcher.find()) {
            String projectInfo = StringUtils.trim(matcher.group("projectInfo"));
            String[] projectInfoArray = projectInfo.split(",");
            unitAssignmentId =
                    Integer.parseInt(projectInfoArray[projectInfoArray.length - 1].substring(1));
        } else {
            throw new IllegalArgumentException("IfcProject invalid: " + line);
        }
    }

    public int getUnitAssignmentId() {
        return unitAssignmentId;
    }
}
