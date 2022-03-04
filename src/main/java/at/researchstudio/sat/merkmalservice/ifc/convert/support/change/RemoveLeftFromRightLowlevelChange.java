package at.researchstudio.sat.merkmalservice.ifc.convert.support.change;

public class RemoveLeftFromRightLowlevelChange extends EntityChange2 {
    public RemoveLeftFromRightLowlevelChange(Integer leftEntityId, Class<?> leftEntityType, Integer rightEntityId,
                    Class<?> rightEntityType) {
        super(leftEntityId, leftEntityType, rightEntityId, rightEntityType);
    }
}
