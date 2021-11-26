package at.researchstudio.sat.merkmalservice.ifc.model;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

public class IfcUnitAssignmentLine extends IfcLine {
    public static final String IDENTIFIER = "IFCUNITASSIGNMENT";
    List<Integer> unitIds;

    private static final Pattern extractPattern =
            Pattern.compile("(?>#[0-9]*= IFCUNITASSIGNMENT\\(\\((?<unitIds>.*)\\)\\))");

    public IfcUnitAssignmentLine(String line) {
        super(line);

        Matcher matcher = extractPattern.matcher(line);

        if (matcher.find()) {
            String unitIdsString = StringUtils.trim(matcher.group("unitIds"));
            unitIds =
                    Arrays.stream(unitIdsString.split(","))
                            .map(unitIdString -> Integer.parseInt(unitIdString.substring(1)))
                            .collect(Collectors.toList());
        } else {
            throw new IllegalArgumentException("IfcUnitAssignmentLine invalid: " + line);
        }
    }

    public List<Integer> getUnitIds() {
        return unitIds;
    }
}
