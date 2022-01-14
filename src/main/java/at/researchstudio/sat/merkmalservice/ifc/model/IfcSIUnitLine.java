package at.researchstudio.sat.merkmalservice.ifc.model;

import static at.researchstudio.sat.merkmalservice.ifc.support.IfcUtils.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import at.researchstudio.sat.merkmalservice.vocab.ifc.IfcUnitMeasurePrefix;
import org.apache.commons.lang3.StringUtils;

public class IfcSIUnitLine extends IfcLine {
    public static final String IDENTIFIER = "IFCSIUNIT";
    private static final Pattern extractPattern =
            Pattern.compile(
                    "(?>#[0-9]*= IFCSIUNIT\\(\\*,.)(?<type>.*).,(.(?<prefix>.*).|\\$),.(?<measure>.*).\\)");
    // <- warning might contain whitespaces, trim needed

    private String type;
    private String measure;
    private String prefix;

    public IfcSIUnitLine(String line) {
        super(line);

        Matcher matcher = extractPattern.matcher(line);

        if (matcher.find()) {
            type = StringUtils.trim(matcher.group("type"));
            measure = StringUtils.trim(matcher.group("measure"));
            prefix = StringUtils.trimToNull(matcher.group("prefix"));
        } else {
            throw new IllegalArgumentException("IfcSIUnitLine invalid: " + line);
        }
    }

    public IfcSIUnitLine(Integer id, String type, String measure, String prefix) {
        super(makeLine(id, type, measure, prefix));
        this.type = type;
        this.measure = measure;
        this.prefix = prefix;
    }

    private static String makeLine(Integer id, String type, String measure, String prefix) {
        return new StringBuilder()
                .append(toStepId(id))
                .append("= ")
                .append(IDENTIFIER)
                .append("(*,")
                .append(toStepConstant(type))
                .append(",")
                .append(toOptionalStepConstant(prefix == null || prefix.equals(IfcUnitMeasurePrefix.NONE.toString()) ? null : prefix))
                .append(",")
                .append(toStepConstant(measure))
                .append(");")
                .toString();
    }

    public String getType() {
        return type;
    }

    public String getMeasure() {
        return measure;
    }

    public String getPrefix() {
        return prefix;
    }
}
