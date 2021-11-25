package at.researchstudio.sat.mmsdesktop.model.ifc;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

public class IfcRelDefinesByPropertiesLine extends IfcLine {
    public static final String IDENTIFIER = "IFCRELDEFINESBYPROPERTIES";
    private static final Pattern extractPattern =
            Pattern.compile(
                    "(?>#[0-9]*= IFCRELDEFINESBYPROPERTIES\\('(?<globalId>.*)',(?<historyId>[^,]*),(('(?<name>.*)')|\\$),((?<description>[^$]*)|\\$),\\((?<relatedObjectIds>.*)\\),(?<propertySetId>[^,]*)\\))");

    public String globalId;
    public int historyId;
    public int relatedSetId;
    // elementQuantity or propertySet
    public String name;
    public String description;
    public List<Integer> relatedObjectIds;

    public IfcRelDefinesByPropertiesLine(String line) {
        super(line);

        Matcher matcher = extractPattern.matcher(line);

        if (matcher.find()) {
            globalId = StringUtils.trim(matcher.group("globalId"));
            historyId = Integer.parseInt(matcher.group("historyId").substring(1));
            relatedSetId = Integer.parseInt(matcher.group("propertySetId").substring(1));
            name = StringUtils.trim(matcher.group("name"));
            description = StringUtils.trim(matcher.group("description"));
            String relatedObjectIdsString = matcher.group("relatedObjectIds");
            relatedObjectIds =
                    Arrays.stream(relatedObjectIdsString.split(","))
                            .map(relatedObjectId -> Integer.parseInt(relatedObjectId.substring(1)))
                            .collect(Collectors.toList());
        } else {
            throw new IllegalArgumentException("IfcPropertyEnumeration invalid: " + line);
        }
    }

    public String getGlobalId() {
        return globalId;
    }

    public int getHistoryId() {
        return historyId;
    }

    public int getRelatedSetId() {
        return relatedSetId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<Integer> getRelatedObjectIds() {
        return relatedObjectIds;
    }
}
