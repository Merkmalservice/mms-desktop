package at.researchstudio.sat.mmsdesktop.model.ifc;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class IfcPropertySetLine extends IfcLine {
    public static final String IDENTIFIER = "IFCPROPERTYSET";
    private static final Pattern extractPattern =
            Pattern.compile(
                    "(?>#[0-9]*= IFCPROPERTYSET\\('(?<globalId>.*)',(?<historyId>[^,]*),'(?<name>.*)',((?<description>[^$]*)|\\$),\\((?<propertyIds>.*)\\)\\))");
    public String name;
    public String globalId;
    public int historyId;
    public String description;
    public List<Integer> propertyIds;

    public IfcPropertySetLine(String line) {
        super(line);

        Matcher matcher = extractPattern.matcher(line);

        if (matcher.find()) {
            globalId = StringUtils.trim(matcher.group("globalId"));
            historyId = Integer.parseInt(matcher.group("historyId").substring(1));
            name = StringUtils.trim(matcher.group("name"));
            description = StringUtils.trim(matcher.group("description"));
            String propertyIdsString = matcher.group("propertyIds");
            propertyIds =
                    Arrays.stream(propertyIdsString.split(","))
                            .map(
                                    propertyIdString ->
                                            Integer.parseInt(propertyIdString.substring(1)))
                            .collect(Collectors.toList());
        } else {
            throw new IllegalArgumentException("IfcPropertySetLine invalid: " + line);
        }
    }

    public String getName() {
        return name;
    }

    public int getHistoryId() {
        return historyId;
    }

    public String getDescription() {
        return description;
    }

    public List<Integer> getPropertyIds() {
        return propertyIds;
    }

    public String getGlobalId() {
        return globalId;
    }
}
