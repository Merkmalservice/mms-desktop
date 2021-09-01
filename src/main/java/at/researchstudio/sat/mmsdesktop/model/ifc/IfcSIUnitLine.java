package at.researchstudio.sat.mmsdesktop.model.ifc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

public class IfcSIUnitLine extends IfcLine {
    private static final Pattern unitExtractPattern =
            Pattern.compile(
                    "(?>#[0-9]*= IFCSIUNIT\\(\\*,.)(?<type>.*).,(.(?<prefix>.*).|\\$),.(?<measure>.*).\\)");
    // <- warning might contain whitespaces, trim needed

    private String type;
    private String measure;
    private String prefix;

    public IfcSIUnitLine(String line) {
        super(line);

        Matcher matcher = unitExtractPattern.matcher(line);

        if (matcher.find()) {
            type = StringUtils.trim(matcher.group("type"));
            measure = StringUtils.trim(matcher.group("measure"));
            prefix = StringUtils.trimToNull(matcher.group("prefix"));
            boolean projectDefault = false; // TODO: FIGURE OUT PROJECTDEFAULT
            // PARSER
        } else {
            throw new IllegalArgumentException("IfcSIUnitLine invalid: " + line);
        }
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
