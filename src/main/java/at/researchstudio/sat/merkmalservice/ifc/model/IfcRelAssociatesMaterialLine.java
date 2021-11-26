package at.researchstudio.sat.merkmalservice.ifc.model;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

public class IfcRelAssociatesMaterialLine extends IfcRelationLine {
    public static final String IDENTIFIER = "IFCRELASSOCIATESMATERIAL";
    private static final Pattern extractPattern =
            Pattern.compile(
                    "(?>#[0-9]*= IFCRELASSOCIATESMATERIAL\\('(?<globalId>.*)',(?<historyId>[^,]*),(('(?<name>.*)')|\\$),((?<description>[^$]*)|\\$),\\((?<relatedObjectIds>.*)\\),(?<relatingMaterialId>[^,]*)\\))");

    private int relatingMaterialId;

    public IfcRelAssociatesMaterialLine(String line) {
        super(line);

        Matcher matcher = extractPattern.matcher(line);

        if (matcher.find()) {
            globalId = StringUtils.trim(matcher.group("globalId"));
            historyId = Integer.parseInt(matcher.group("historyId").substring(1));
            relatingMaterialId = Integer.parseInt(matcher.group("relatingMaterialId").substring(1));
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

    public int getRelatingMaterialId() {
        return relatingMaterialId;
    }
}
