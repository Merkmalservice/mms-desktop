package at.researchstudio.sat.mmsdesktop.model.ifc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

public class IfcDerivedUnitElementLine extends IfcLine {
    private String unitId;
    private int exponent;

    private static final Pattern derivedUnitElementLinePattern =
            Pattern.compile(
                    "(?>#[0-9]*= IFCDERIVEDUNITELEMENT\\((?<unitId>.*),(?<exponent>.*)\\))");

    public IfcDerivedUnitElementLine(String line) {
        super(line);

        Matcher matcher = derivedUnitElementLinePattern.matcher(line);

        if (matcher.find()) {
            unitId = StringUtils.trim(matcher.group("unitId"));
            String exponentString = StringUtils.trim(matcher.group("exponent"));
            exponent = Integer.parseInt(exponentString);
        } else {
            throw new IllegalArgumentException("IfcDerivedUnitElementLine invalid: " + line);
        }
    }

    public String getUnitId() {
        return unitId;
    }

    public int getExponent() {
        return exponent;
    }
}
