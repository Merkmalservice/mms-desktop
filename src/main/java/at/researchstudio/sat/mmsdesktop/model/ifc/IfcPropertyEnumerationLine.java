package at.researchstudio.sat.mmsdesktop.model.ifc;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

public class IfcPropertyEnumerationLine extends IfcLine {
    private static final Pattern extractPattern =
            Pattern.compile(
                    "(?>#[0-9]*= IFCPROPERTYENUMERATION\\('(?<name>.*)',\\((?<values>.*)\\),((?<unitId>.*)|\\$)\\))");
    public static final Pattern ENUM_VALUE_PATTERN =
            Pattern.compile("(?<type>.*)\\('?(?<value>[^']*)'?\\)");

    private String name;
    private String unitId;
    private List<String> values;

    public IfcPropertyEnumerationLine(String line) {
        super(line);

        Matcher matcher = extractPattern.matcher(line);
        if (matcher.find()) {
            name = StringUtils.trim(matcher.group("name"));

            String valuesString = StringUtils.trim(matcher.group("values"));
            values =
                    Arrays.stream(valuesString.split(","))
                            .map(
                                    value -> {
                                        Matcher m = ENUM_VALUE_PATTERN.matcher(value);

                                        if (m.find()) {
                                            // m.group("type") -> ignore for now
                                            return m.group("value");
                                        } else {
                                            throw new IllegalArgumentException(
                                                    "IfcPropertyEnumeration with invalid enum: "
                                                            + value);
                                        }
                                    })
                            .collect(Collectors.toList());

            unitId = StringUtils.trimToNull(matcher.group("unitId"));
            unitId = Objects.nonNull(unitId) ? "#" + unitId : unitId;
        } else {
            throw new IllegalArgumentException("IfcPropertyEnumeration invalid: " + line);
        }
    }

    public String getUnitId() {
        return unitId;
    }

    public String getName() {
        return name;
    }

    public List<String> getValues() {
        return values;
    }
}
