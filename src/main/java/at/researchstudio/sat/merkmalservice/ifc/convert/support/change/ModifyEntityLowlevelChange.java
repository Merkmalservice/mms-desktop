package at.researchstudio.sat.merkmalservice.ifc.convert.support.change;

public class ModifyEntityLowlevelChange extends EntityChange {
    private final String oldLine;
    private final String newLine;

    public ModifyEntityLowlevelChange(Integer entityId, Class<?> entityType, String oldLine, String newLine) {
        super(entityId, entityType);
        this.oldLine = oldLine;
        this.newLine = newLine;
    }
}
