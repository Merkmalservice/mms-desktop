package at.researchstudio.sat.mmsdesktop.model.ifc;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

public class IfcSinglePropertyValueLine extends IfcLine implements IfcNamedPropertyLineInterface {
    private static final Pattern extractPattern =
            Pattern.compile(
                    "(?>#[0-9]*= IFCPROPERTYSINGLEVALUE\\(')(?<name>.*)',((?<description>[^$]*)|\\$),(((?<type>[A-Z]*)\\(('?)(?<value>[^']*)('?)(\\))|\\$),(.(?<unitId>.*)|\\$)\\))");
    // <- warning might contain a . or ' at the end of the value
    private String name;
    private String description;
    private String type;
    private String value;
    private String unitId;

    public IfcSinglePropertyValueLine(String line) {
        super(line);

        Matcher matcher = extractPattern.matcher(line);
        if (matcher.find()) {
            name = StringUtils.trim(matcher.group("name"));
            type = StringUtils.trimToNull(matcher.group("type"));
            description = StringUtils.trimToNull(matcher.group("description"));
            value = StringUtils.trimToNull(matcher.group("value"));
            unitId = StringUtils.trimToNull(matcher.group("unitId"));
            if (Objects.nonNull(unitId)) {
                unitId = "#" + unitId;
            }
        } else {
            throw new IllegalArgumentException("IfcPropertySingleValue invalid: " + line);
        }
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public String getUnitId() {
        return unitId;
    }
}
