package at.researchstudio.sat.mmsdesktop.model.ifc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

public class IfcDerivedUnitElementLine extends IfcLine {
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
