package at.researchstudio.sat.mmsdesktop.model.ifc;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

public class IfcPropertySetLine extends IfcLine {
    private static final Pattern extractPattern =
            Pattern.compile(
                    "(?>#[0-9]*= IFCPROPERTYSET\\('(?<globalId>.*)',(?<historyId>[^,]*),'(?<name>.*)',((?<description>[^$]*)|\\$),\\((?<propertyIds>.*)\\)\\))");
    public String name;
    public String globalId;
    public String historyId;
    public String description;
    public List<String> propertyIds;

    public IfcPropertySetLine(String line) {
        super(line);

        Matcher matcher = extractPattern.matcher(line);

        if (matcher.find()) {
            globalId = matcher.group("globalId");
            historyId = matcher.group("historyId");
            name = StringUtils.trim(matcher.group("name"));
            description = StringUtils.trim(matcher.group("description"));
            String propertyIdsString = matcher.group("propertyIds");
            propertyIds = Arrays.asList(propertyIdsString.split(","));
        } else {
            throw new IllegalArgumentException("IfcPropertySetLine invalid: " + line);
        }
    }

    public String getName() {
        return name;
    }

    public String getHistoryId() {
        return historyId;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getPropertyIds() {
        return propertyIds;
    }

    public String getGlobalId() {
        return globalId;
    }
}
