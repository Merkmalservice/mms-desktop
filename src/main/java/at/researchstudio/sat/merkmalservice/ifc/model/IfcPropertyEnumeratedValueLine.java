package at.researchstudio.sat.merkmalservice.ifc.model;

import static at.researchstudio.sat.merkmalservice.ifc.model.IfcPropertyEnumerationLine.ENUM_VALUE_PATTERN;

import at.researchstudio.sat.merkmalservice.ifc.support.IfcUtils;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

public class IfcPropertyEnumeratedValueLine extends IfcLine
        implements IfcNamedPropertyLineInterface {
    public static final String IDENTIFIER = "IFCPROPERTYENUMERATEDVALUE";
    private static final Pattern extractPattern =
            Pattern.compile(
                    "(?>#[0-9]*= IFCPROPERTYENUMERATEDVALUE\\('(?<name>.*)',((?<description>[^,]*)|\\$),\\((?<values>.*)\\),(.(?<enumId>.*)|\\$)\\))");

    private String name;
    private int enumId;
    private List<String> values;

    public IfcPropertyEnumeratedValueLine(String line) {
        super(line);

        Matcher matcher = extractPattern.matcher(line);
        if (matcher.find()) {
            name = StringUtils.trim(matcher.group("name"));
            String valuesString = matcher.group("values");
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
                                                    "IfcPropertyEnumeratedValue with invalid enum: "
                                                            + value);
                                        }
                                    })
                            .collect(Collectors.toList());
            String _enumId = StringUtils.trimToNull(matcher.group("enumId"));
            if (Objects.nonNull(_enumId)) {
                enumId = IfcUtils.executeOrDefaultOnException(() -> Integer.parseInt(_enumId), 0);
            }
        } else {
            throw new IllegalArgumentException("IfcPropertyEnumeratedValue invalid: " + line);
        }
    }

    public String getName() {
        return name;
    }

    public int getEnumId() {
        return enumId;
    }

    public List<String> getValues() {
        return values;
    }
}
