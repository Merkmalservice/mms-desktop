package at.researchstudio.sat.merkmalservice.ifc.model;

import java.util.List;

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
}
