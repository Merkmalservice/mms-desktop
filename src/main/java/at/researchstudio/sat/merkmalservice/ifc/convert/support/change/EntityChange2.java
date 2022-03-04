package at.researchstudio.sat.merkmalservice.ifc.convert.support.change;

public abstract class EntityChange2 implements LowlevelChange{
    private final Integer leftEntityId;
    private final Class<?> leftEntityType;
    private final Integer rightEntityId;
    private final Class<?> rightEntityType;

    public EntityChange2(Integer leftEntityId, Class<?> leftEntityType, Integer rightEntityId,
                    Class<?> rightEntityType) {
        this.leftEntityId = leftEntityId;
        this.leftEntityType = leftEntityType;
        this.rightEntityId = rightEntityId;
        this.rightEntityType = rightEntityType;
    }

    public Integer getLeftEntityId() {
        return leftEntityId;
    }

    public Class<?> getLeftEntityType() {
        return leftEntityType;
    }

    public Integer getRightEntityId() {
        return rightEntityId;
    }

    public Class<?> getRightEntityType() {
        return rightEntityType;
    }

    @Override public String toString() {
        return getClass().getSimpleName() +
                        "{ left=" + leftEntityType.getSimpleName() + '(' + leftEntityId + "), right=" +
                        rightEntityType.getSimpleName() +'(' + rightEntityId + ") }";
    }
}
