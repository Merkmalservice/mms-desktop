package at.researchstudio.sat.mmsdesktop.model.ifc;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

public class IfcRelDefinesByPropertiesLine extends IfcLine {
    private static final Pattern extractPattern =
            Pattern.compile(
                    "(?>#[0-9]*= IFCRELDEFINESBYPROPERTIES\\('(?<globalId>.*)',(?<historyId>[^,]*),(('(?<name>.*)')|\\$),((?<description>[^$]*)|\\$),\\((?<relatedObjectIds>.*)\\),(?<propertySetId>[^,]*)\\))");

    public String globalId;
    public String historyId;
    public String propertySetId;
    public String name;
    public String description;
    public List<String> relatedObjectIds;

    public IfcRelDefinesByPropertiesLine(String line) {
        super(line);

        Matcher matcher = extractPattern.matcher(line);

        if (matcher.find()) {
            globalId = matcher.group("globalId");
            historyId = matcher.group("historyId");
            propertySetId = matcher.group("propertySetId");
            name = StringUtils.trim(matcher.group("name"));
            description = StringUtils.trim(matcher.group("description"));
            String relatedObjectIdsString = matcher.group("relatedObjectIds");
            relatedObjectIds = Arrays.asList(relatedObjectIdsString.split(","));
        } else {
            throw new IllegalArgumentException("IfcPropertyEnumeration invalid: " + line);
        }
    }

    public String getGlobalId() {
        return globalId;
    }

    public String getHistoryId() {
        return historyId;
    }

    public String getPropertySetId() {
        return propertySetId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getRelatedObjectIds() {
        return relatedObjectIds;
    }
}
