package at.researchstudio.sat.merkmalservice.ifc.model;

import static at.researchstudio.sat.merkmalservice.ifc.support.IfcUtils.*;
import static at.researchstudio.sat.merkmalservice.ifc.support.IfcUtils.toStepValue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

public class IfcDerivedUnitElementLine extends IfcLine {
    public static final String IDENTIFIER = "IFCDERIVEDUNITELEMENT";
    private int unitId;
    private String unitIdString;
    private int exponent;
    private static final Pattern extractPattern =
            Pattern.compile(
                    "(?>#[0-9]*= IFCDERIVEDUNITELEMENT\\((?<unitId>.*),(?<exponent>.*)\\))");

    public IfcDerivedUnitElementLine(String line) {
        super(line);
        Matcher matcher = extractPattern.matcher(line);
        if (matcher.find()) {
            unitIdString = StringUtils.trim(matcher.group("unitId"));
            unitId = Integer.parseInt(unitIdString.substring(1));
            String exponentString = StringUtils.trim(matcher.group("exponent"));
            exponent = Integer.parseInt(exponentString);
        } else {
            throw new IllegalArgumentException("IfcDerivedUnitElementLine invalid: " + line);
        }
    }

    public IfcDerivedUnitElementLine(Integer id, int unitId, int exponent) {
        super(makeLine(id, unitId, exponent));
        this.unitId = unitId;
        this.unitIdString = "#" + Integer.toString(unitId);
        this.exponent = exponent;
    }

    private static String makeLine(Integer id, int unitId, int exponent) {
        return new StringBuilder()
                .append(toStepId(id))
                .append("= ")
                .append(IDENTIFIER)
                .append("(")
                .append(toStepId(unitId))
                .append(",")
                .append(toStepValue(exponent))
                .append(");")
                .toString();
    }

    public int getUnitId() {
        return unitId;
    }

    public String getUnitIdString() {
        return unitIdString;
    }

    public int getExponent() {
        return exponent;
    }
}
