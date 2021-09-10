package at.researchstudio.sat.mmsdesktop.model.ifc;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

public class IfcUnitAssignmentLine extends IfcLine {
    List<String> unitIds;

    private static final Pattern extractPattern =
            Pattern.compile("(?>#[0-9]*= IFCUNITASSIGNMENT\\(\\((?<unitIds>.*)\\)\\))");

    public IfcUnitAssignmentLine(String line) {
        super(line);

        Matcher matcher = extractPattern.matcher(line);

        if (matcher.find()) {
            String unitIdsString = StringUtils.trim(matcher.group("unitIds"));
            unitIds = Arrays.asList(unitIdsString.split(","));
        } else {
            throw new IllegalArgumentException("IfcUnitAssignmentLine invalid: " + line);
        }
    }

    public List<String> getUnitIds() {
        return unitIds;
    }
}
