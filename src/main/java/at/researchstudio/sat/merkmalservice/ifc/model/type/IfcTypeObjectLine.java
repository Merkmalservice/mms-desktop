package at.researchstudio.sat.merkmalservice.ifc.model.type;

import at.researchstudio.sat.merkmalservice.ifc.model.IfcLine;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

public abstract class IfcTypeObjectLine extends IfcLine {
    private static final Pattern extractPattern =
            Pattern.compile(
                    "#[0-9]+= (?<identifier>[A-Z0-9]+)\\('(?<globalId>([^']|')*)',(?<historyId>[^,]*),('(?<name>([^']|')*)'|\\$),('(?<description>([^']|')*)'|\\$),((?<applicableOccurrence>[^,]+)|\\$),(\\((?<propertySetIds>[^)]+)\\)|\\$)");

    protected String globalId;
    protected String name;
    protected int historyId;
    protected String description;
    protected String applicableOccurrence;
    public List<Integer> propertySetIds;

    public IfcTypeObjectLine(String line) {
        super(line);

        Matcher matcher = extractPattern.matcher(line);

        if (matcher.find()) {
            globalId = StringUtils.trim(matcher.group("globalId"));
            historyId = Integer.parseInt(matcher.group("historyId").substring(1));
            name = StringUtils.trim(matcher.group("name"));
            description = StringUtils.trim(matcher.group("description"));
            applicableOccurrence = StringUtils.trim(matcher.group("applicableOccurrence"));
            String propertySetIdsStr = matcher.group("propertySetIds");
            propertySetIds =
                    propertySetIdsStr == null
                            ? Collections.emptyList()
                            : Arrays.stream(propertySetIdsStr.split(","))
                                    .map(
                                            propertyIdString ->
                                                    Integer.parseInt(propertyIdString.substring(1)))
                                    .collect(Collectors.toList());
        } else {
            throw new IllegalArgumentException("IfcPropertySetLine invalid: " + line);
        }
    }

    public List<Integer> getPropertySetIds() {
        return propertySetIds;
    }
}
