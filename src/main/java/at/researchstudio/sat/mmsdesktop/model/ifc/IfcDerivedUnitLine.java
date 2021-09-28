package at.researchstudio.sat.mmsdesktop.model.ifc;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

public class IfcDerivedUnitLine extends IfcLine {
    public static final String IDENTIFIER = "IFCDERIVEDUNIT(";
    private static final Pattern extractPattern =
            Pattern.compile(
                    "(?>#[0-9]*= IFCDERIVEDUNIT\\(\\((?<unitIds>.*)\\),.(?<type>.*).,(('(?<name>.*)')|\\$)\\))");

    private List<Integer> unitElementIds;
    private String type;
    private String name;

    public IfcDerivedUnitLine(String line) {
        super(line);

        Matcher matcher = extractPattern.matcher(line);
        if (matcher.find()) {
            type = StringUtils.trim(matcher.group("type"));
            name = StringUtils.trim(matcher.group("name"));
            String unitIdsString = StringUtils.trim(matcher.group("unitIds"));
            unitElementIds =
                    Arrays.asList(unitIdsString.split(",")).stream()
                            .map(unitIdString -> Integer.parseInt(unitIdString.substring(1)))
                            .collect(Collectors.toList());
        } else {
            throw new IllegalArgumentException("IfcDerivedUnitLine invalid: " + line);
        }
    }

    public List<Integer> getUnitElementIds() {
        return unitElementIds;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }
}
