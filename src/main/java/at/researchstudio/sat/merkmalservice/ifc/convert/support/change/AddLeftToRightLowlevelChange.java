package at.researchstudio.sat.merkmalservice.ifc.convert.support.change;

public class AddLeftToRightLowlevelChange extends EntityChange2 {
    public AddLeftToRightLowlevelChange(Integer leftEntityId, Class<?> leftEntityType, Integer rightEntityId,
                    Class<?> rightEntityType) {
        super(leftEntityId, leftEntityType, rightEntityId, rightEntityType);
    }
}
