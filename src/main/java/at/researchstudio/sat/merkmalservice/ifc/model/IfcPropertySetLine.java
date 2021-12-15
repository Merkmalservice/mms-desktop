package at.researchstudio.sat.merkmalservice.ifc.model;

import static at.researchstudio.sat.merkmalservice.ifc.support.IfcUtils.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

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

    public IfcPropertySetLine(
            Integer id,
            String name,
            String globalId,
            int historyId,
            String description,
            List<Integer> propertyIds) {
        super(makeLine(id, globalId, historyId, name, description, propertyIds));
        this.name = name;
        this.globalId = globalId;
        this.historyId = historyId;
        this.description = description;
        this.propertyIds = propertyIds == null ? new ArrayList<>() : new ArrayList<>(propertyIds);
    }

    private static String makeLine(
            Integer id,
            String globalId,
            Integer historyId,
            String name,
            String description,
            List<Integer> propertyIds) {
        return toStepId(id)
                + "= "
                + IDENTIFIER
                + "("
                + toStepValue(globalId)
                + ","
                + toOptionalStepId(historyId)
                + ","
                + toOptionalStepValue(name)
                + ","
                + toOptionalStepValue(description)
                + ","
                + toOptionalStepIds(propertyIds)
                + ");";
    }

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

    @Override
    public boolean removeReferenceTo(IfcLine item) {
        return super.removeReferenceTo(item);
    }

    @Override
    public boolean removeReferenceTo(Integer itemId) {
        super.removeReferenceTo(itemId);
        this.propertyIds.remove(itemId);
        return propertyIds.isEmpty();
    }

    public void addPropertyId(Integer itemId) {
        super.getReferences().add(itemId);
        this.propertyIds.add(itemId);
        modifyLine(line -> makeLine(getId(), globalId, historyId, name, description, propertyIds));
    }
}
