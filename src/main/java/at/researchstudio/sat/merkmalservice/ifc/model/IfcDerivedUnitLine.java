package at.researchstudio.sat.merkmalservice.ifc.model;

import static at.researchstudio.sat.merkmalservice.ifc.support.IfcUtils.*;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

public class IfcDerivedUnitLine extends IfcLine {
    public static final String IDENTIFIER = "IFCDERIVEDUNIT";
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
                    Arrays.stream(unitIdsString.split(","))
                            .map(unitIdString -> Integer.parseInt(unitIdString.substring(1)))
                            .collect(Collectors.toList());
        } else {
            throw new IllegalArgumentException("IfcDerivedUnitLine invalid: " + line);
        }
    }

    public IfcDerivedUnitLine(Integer id, List<Integer> unitElementIds, String type, String name) {
        super(makeLine(id, unitElementIds, type, name));
        this.unitElementIds = unitElementIds;
        this.type = type;
        this.name = name;
    }

    private static String makeLine(
            Integer id, List<Integer> unitElementIds, String type, String name) {
        return new StringBuilder()
                .append(toStepId(id))
                .append("= ")
                .append(IDENTIFIER)
                .append("(")
                .append(toOptionalStepIds(unitElementIds))
                .append(",")
                .append(toStepConstant(type))
                .append(",")
                .append(toOptionalStepConstant(name))
                .append(");")
                .toString();
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
