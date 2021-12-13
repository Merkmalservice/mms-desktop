package at.researchstudio.sat.merkmalservice.ifc.model;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

public class IfcRelDefinesByPropertiesLine extends IfcRelationLine {
    public static final String IDENTIFIER = "IFCRELDEFINESBYPROPERTIES";
    private static final Pattern extractPattern =
            Pattern.compile(
                    "(?>#[0-9]*= IFCRELDEFINESBYPROPERTIES\\('(?<globalId>.*)',(?<historyId>[^,]*),(('(?<name>.*)')|\\$),((?<description>[^$]*)|\\$),\\((?<relatedObjectIds>.*)\\),(?<propertySetId>[^,]*)\\))");

    private int relatingPropertySetId;

    public IfcRelDefinesByPropertiesLine(String line) {
        super(line);

        Matcher matcher = extractPattern.matcher(line);

        if (matcher.find()) {
            globalId = StringUtils.trim(matcher.group("globalId"));
            historyId = Integer.parseInt(matcher.group("historyId").substring(1));
            relatingPropertySetId = Integer.parseInt(matcher.group("propertySetId").substring(1));
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

    public boolean isSharedPropertySet() {
        return relatedObjectIds.size() > 1;
    }

    public int getRelatingPropertySetId() {
        return relatingPropertySetId;
    }

    public void changeRelatingPropertySetIdTo(Integer id) {
        replaceReference(this.relatingPropertySetId, id);
        this.relatingPropertySetId = id;
    }

    @Override
    public boolean removeReferenceTo(Integer itemId) {
        super.removeReferenceTo(itemId);
        this.relatedObjectIds.remove(itemId);
        return relatingPropertySetId == itemId || relatedObjectIds.isEmpty();
    }
}
