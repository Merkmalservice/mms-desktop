package at.researchstudio.sat.merkmalservice.ifc.convert.support.change;

import at.researchstudio.sat.merkmalservice.ifc.model.IfcLine;

import java.util.ArrayList;
import java.util.List;

public class HighlevelChangeBuilder {

    private HighlevelChange product;
    private List<LowlevelChange> lowlevelChanges = new ArrayList<>();
    private List<Error> errors = new ArrayList<>();

    public HighlevelChangeBuilder(Object originator, HighlevelChangeType type, Integer entityId) {
        this.product = new HighlevelChange(originator, type, entityId);
    }

    public HighlevelChange build(){
        product.setLowlevelChanges(lowlevelChanges);
        product.setErrors(errors);
        HighlevelChange built = product;
        product = null;
        return built;
    }

    public HighlevelChangeBuilder entityAdded(IfcLine ifcEntity){
        this.lowlevelChanges.add(new AddEntityLowlevelChange(ifcEntity.getId(), ifcEntity.getClass()));
        return this;
    }

    public HighlevelChangeBuilder entityDeleted(IfcLine ifcEntity){
        this.lowlevelChanges.add(new DeleteEntityLowlevelChange(ifcEntity.getId(), ifcEntity.getClass()));
        return this;
    }

    public HighlevelChangeBuilder entityModified(IfcLine ifcEntity, String oldLine, String newLine){
        this.lowlevelChanges.add(new ModifyEntityLowlevelChange(ifcEntity.getId(), ifcEntity.getClass(), oldLine, newLine));
        return this;
    }

    public HighlevelChangeBuilder leftEntityAddedToRight(IfcLine leftIfcEntity, IfcLine rightIfcEntity){
        this.lowlevelChanges.add(new AddLeftToRightLowlevelChange(leftIfcEntity.getId(), leftIfcEntity.getClass(), rightIfcEntity.getId(), rightIfcEntity.getClass()));
        return this;
    }

    public HighlevelChangeBuilder leftEntityRemovedFromRight(IfcLine leftIfcEntity, IfcLine rightIfcEntity){
        this.lowlevelChanges.add(new RemoveLeftFromRightLowlevelChange(leftIfcEntity.getId(), leftIfcEntity.getClass(), rightIfcEntity.getId(), rightIfcEntity.getClass()));
        return this;
    }

    public HighlevelChangeBuilder description(String description){
        product.setDescription(description);
        return this;
    }

    public HighlevelChangeBuilder error(String message){
        errors.add(new Error(message));
        return this;
    }

    public HighlevelChangeBuilder errorFmt(String format, Object... arguments){
        errors.add(new Error(String.format(format, arguments)));
        return this;
    }

    public HighlevelChangeBuilder errorFmt(Throwable throwable, String format, Object... arguments){
        errors.add(new Error(String.format(format, arguments),  throwable));
        return this;
    }

    public HighlevelChangeBuilder error(Throwable throwable){
        errors.add(new Error(throwable));
        return this;
    }

    public HighlevelChangeBuilder error(String message, Throwable throwable) {
        errors.add(new Error(message, throwable));
        return this;
    }

}

