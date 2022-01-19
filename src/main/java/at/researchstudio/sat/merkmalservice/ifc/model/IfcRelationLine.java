package at.researchstudio.sat.merkmalservice.ifc.model;

import static java.util.stream.Collectors.joining;

import at.researchstudio.sat.merkmalservice.ifc.support.IfcUtils;
import java.util.List;
import java.util.regex.Pattern;

public class IfcRelationLine extends IfcLine {
    public static final String IDENTIFIER = "NOT_AN_IDENTIFIER_IRL";
    protected String globalId;
    protected int historyId;
    // elementQuantity or propertySet q
    protected String name;
    protected String description;
    protected List<Integer> relatedObjectIds;

    public IfcRelationLine(String line) {
        super(line);
    }

    public String getGlobalId() {
        return globalId;
    }

    public int getHistoryId() {
        return historyId;
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

    public void addRelatedObjectId(Integer itemId) {
        super.getReferences().add(itemId);
        String oldIdlist =
                this.relatedObjectIds.stream()
                        .map(IfcUtils::toStepId)
                        .collect(joining(",", "(", ")"));
        this.relatedObjectIds.add(itemId);
        String newIdList =
                this.relatedObjectIds.stream()
                        .map(IfcUtils::toStepId)
                        .collect(joining(",", "(", ")"));
        modifyLine(line -> line.replaceAll(Pattern.quote(oldIdlist), newIdList));
    }
}
