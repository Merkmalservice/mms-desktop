package at.researchstudio.sat.mmsdesktop.model.ifc;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

public class IfcDerivedUnitLine extends IfcLine {
    // TODO: IMPL
    private static final Pattern derivedUnitLinePattern =
            Pattern.compile(
                    "(?>#[0-9]*= IFCDERIVEDUNIT\\(\\((?<unitIds>.*)\\),.(?<type>.*).,(('(?<name>.*)')|\\$)\\))");

    private List<String> unitElementIds;
    private String type;
    private String name;

    public IfcDerivedUnitLine(String line) {
        super(line);

        Matcher matcher = derivedUnitLinePattern.matcher(line);
        if (matcher.find()) {
            type = StringUtils.trim(matcher.group("type"));
            name = StringUtils.trim(matcher.group("name"));
            String unitIdsString = StringUtils.trim(matcher.group("unitIds"));
            unitElementIds = Arrays.asList(unitIdsString.split(","));
        } else {
            throw new IllegalArgumentException("IfcDerivedUnitLine invalid: " + line);
        }
    }

    public List<String> getUnitElementIds() {
        return unitElementIds;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }
}
