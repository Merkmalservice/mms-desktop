package at.researchstudio.sat.merkmalservice.ifc.model;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

public class IfcRelDefinesByTypeLine extends IfcRelationLine {
    public static final String IDENTIFIER = "IFCRELDEFINESBYTYPE";
    private static final Pattern extractPattern =
            Pattern.compile(
                    "(?>#[0-9]*= IFCRELDEFINESBYTYPE\\('(?<globalId>.*)',(?<historyId>[^,]*),(('(?<name>.*)')|\\$),((?<description>[^$]*)|\\$),\\((?<relatedObjectIds>.*)\\),(?<relatingTypeId>[^,]*)\\))");
    private int relatingTypeId;

    public IfcRelDefinesByTypeLine(String line) {
        super(line);

        Matcher matcher = extractPattern.matcher(line);

        if (matcher.find()) {
            globalId = StringUtils.trim(matcher.group("globalId"));
            historyId = Integer.parseInt(matcher.group("historyId").substring(1));
            relatingTypeId = Integer.parseInt(matcher.group("relatingTypeId").substring(1));
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

    public int getRelatingTypeId() {
        return relatingTypeId;
    }

    @Override
    public boolean removeReferenceTo(Integer itemId) {
        super.removeReferenceTo(itemId);
        this.relatedObjectIds.remove(itemId);
        return this.relatingTypeId == itemId || relatedObjectIds.isEmpty();
    }
}
