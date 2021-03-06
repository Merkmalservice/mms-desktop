package at.researchstudio.sat.merkmalservice.ifc.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

public class IfcQuantityLine extends IfcLine implements IfcNamedPropertyLineInterface {
    public static final String IDENTIFIER = "NOT-AN-IDENTIFIER-IQL";
    private static final Pattern extractPattern_v2x3 =
            Pattern.compile(
                    "(?>#[0-9]*= IFCQUANTITY[A-Z]*\\(')(?<name>[^']*)',('(?<description>[^']*)'|\\$),((?<unitId>.*)|\\$),(?<value>[^,)]*)\\)");

    private static final Pattern extractPattern_v4 =
            Pattern.compile(
                    "(?>#[0-9]*= IFCQUANTITY[A-Z]*\\(')(?<name>[^']*)',('(?<description>[^']*)'|\\$),((?<unitId>[^$]*)|\\$),(?<value>[^,]*),((?<formula>.*)|\\$)\\)");

    private String name;
    private String description;
    private Double value;
    private String unitIdString;
    private String formula;
    private int unitId;

    public IfcQuantityLine(String line) {
        super(line);

        Matcher matcher = extractPattern_v2x3.matcher(line);
        if (matcher.find() && !"$".equals(matcher.group("value"))) {
            name = StringUtils.trim(matcher.group("name"));
            description = StringUtils.trimToNull(matcher.group("description"));
            // type = StringUtils.trim(matcher.group("type")); //TODO TYPE
            String valueString = StringUtils.trim(matcher.group("value"));
            value = Double.parseDouble(valueString);
            // unitId = StringUtils.trimToNull(matcher.group("unitId")); //TODO UNITID
            // unitId = Objects.nonNull(unitId) ? "#" + unitId : unitId; //TODO UNITID
        } else {
            matcher = extractPattern_v4.matcher(line);
            if (matcher.find()) {
                name = StringUtils.trim(matcher.group("name"));
                // type = StringUtils.trim(matcher.group("type")); //TODO TYPE
                description = StringUtils.trimToNull(matcher.group("description"));
                String valueString = StringUtils.trim(matcher.group("value"));
                value = Double.parseDouble(valueString);
                // unitId = StringUtils.trimToNull(matcher.group("unitId")); //TODO UNITID
                // unitId = Objects.nonNull(unitId) ? "#" + unitId : unitId; //TODO UNITID
                formula = StringUtils.trim(matcher.group("formula"));
            } else {
                throw new IllegalArgumentException("IfcQuantityLine invalid: " + line);
            }
        }
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Double getValue() {
        return value;
    }

    public int getUnitId() {
        return unitId;
    }

    public String getUnitIdString() {
        return unitIdString;
    }
}
