package at.researchstudio.sat.merkmalservice.ifc.convert.support.change;

public abstract class EntityChange implements LowlevelChange {
    private final Integer entityId;
    private final Class<?> entityType;

    public EntityChange(Integer entityId, Class<?> entityType) {
        this.entityId = entityId;
        this.entityType = entityType;
    }

    public Integer getEntityId() {
        return entityId;
    }

    public Class<?> getEntityType() {
        return entityType;
    }

    @Override public String toString() {
        return getClass().getSimpleName() +
                        "{ " + entityType.getSimpleName() + '(' + entityId + ')' + " }";
    }
}
