package at.researchstudio.sat.merkmalservice.ifc.convert.support.change;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class HighlevelChange {
    private Integer entityId;
    private Object originator;
    private HighlevelChangeType type;
    private List<LowlevelChange> lowlevelChanges;
    private List<Error> errors;
    private String description;

    HighlevelChange(Object originator, HighlevelChangeType type, Integer entityId) {
        Objects.requireNonNull(originator);
        Objects.requireNonNull(entityId);
        Objects.requireNonNull(type);
        this.originator = originator;
        this.type = type;
        this.entityId = entityId;
        this.errors = List.of();
        this.lowlevelChanges = List.of();
    }

    public HighlevelChange(HighlevelChangeType type, Integer entityId, String description,
                    List<LowlevelChange> lowlevelChanges,
                    List<Error> errors,
                    Object originator) {
        Objects.requireNonNull(originator);
        Objects.requireNonNull(entityId);
        Objects.requireNonNull(type);
        Objects.requireNonNull(lowlevelChanges);
        Objects.requireNonNull(errors);
        this.originator = originator;
        this.entityId = entityId;
        this.type = type;
        this.lowlevelChanges = lowlevelChanges.stream().collect(Collectors.toUnmodifiableList());
        this.errors = errors.stream().collect(Collectors.toUnmodifiableList());
        this.description = description;
    }

    public Object getOriginator() {
        return originator;
    }

    public Integer getEntityId(){
        return entityId;
    }

    public HighlevelChangeType getType() {
        return type;
    }

    public List<LowlevelChange> getLowlevelChanges() {
        return lowlevelChanges;
    }

    public Optional<String> getDescription() {
        return Optional.ofNullable(description);
    }

    void setDescription(String description) {
        this.description = description;
    }

    void setErrors(List<Error> errors){
        this.errors = errors.stream().collect(Collectors.toUnmodifiableList());
    }

    void setLowlevelChanges(List<LowlevelChange> lowlevelChanges) {
        this.lowlevelChanges = lowlevelChanges.stream().collect(Collectors.toUnmodifiableList());
    }

    public List<Error> getErrors() {
        return errors;
    }
}
