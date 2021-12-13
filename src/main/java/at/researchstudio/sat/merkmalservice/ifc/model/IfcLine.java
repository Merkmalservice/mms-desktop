package at.researchstudio.sat.merkmalservice.ifc.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

public class IfcLine {
    public static final String IDENTIFIER = "NOT-AN-IDENTIFIER-IL";
    private static final Pattern REF_ID_PATTERN = Pattern.compile("#[0-9]+");
    private int id;
    private final String line;
    private String modifiedLine = null;
    private String stringId;
    private String type;
    private List<Integer> references;

    public IfcLine(String line) {
        this.line = line;
        if (line.startsWith("#")) {
            stringId = line.split("=")[0];
            id = Integer.parseInt(stringId.substring(1));
            int assignmentIndex = line.indexOf("=");
            Matcher m = REF_ID_PATTERN.matcher(line.substring(assignmentIndex));
            int bracketIndex = line.indexOf('(', assignmentIndex);
            type = StringUtils.trim(line.substring(assignmentIndex, bracketIndex));
            references = new ArrayList<>();
            while (m.find()) {
                try {
                    references.add(Integer.parseInt(m.group().substring(1)));
                } catch (Exception e) {
                    System.err.println(m.group() + " could not be parsed to integer");
                }
            }
        }
    }

    public String getLine() {
        return line;
    }

    public String getModifiedLine() {
        return modifiedLine == null ? line : modifiedLine;
    }

    public int getId() {
        return id;
    }

    public void changeIdTo(Integer newId) {
        int oldId = this.id;
        this.id = newId;
        this.modifiedLine =
                (this.modifiedLine == null ? line : modifiedLine)
                        .replaceAll("#" + oldId + "\\b", "#" + this.id);
    }

    public String getType() {
        return type;
    }

    public boolean hasId() {
        return id != 0;
    }

    public String getStringId() {
        return stringId;
    }

    public List<Integer> getReferences() {
        return references;
    }

    public boolean references(IfcLine ifcLine) {
        return references(ifcLine.getId());
    }

    public boolean references(Integer id) {
        return references.contains(id);
    }

    @Override
    public String toString() {
        return line;
    }

    public boolean removeReferenceTo(IfcLine item) {
        return removeReferenceTo(item.getId());
    }

    public boolean removeReferenceTo(Integer itemId) {
        this.references.remove(itemId);
        this.modifiedLine =
                ReferenceRemover.removeReferenceTo(
                        modifiedLine == null ? line : modifiedLine, itemId);
        return false;
    }

    public void replaceReference(Integer oldValue, Integer newValue) {
        Objects.requireNonNull(oldValue);
        Objects.requireNonNull(newValue);
        if (!this.references.remove(oldValue)) {
            throw new IllegalArgumentException(
                    String.format(
                            "Cannot replace reference to #%d with reference to #%d in line %s: no such reference",
                            oldValue, newValue, modifiedLine));
        }
        this.references.add(newValue);
        this.modifiedLine =
                (modifiedLine == null ? line : modifiedLine)
                        .replaceAll("#" + oldValue + "\\b", "#" + newValue);
    }

    protected void modifyLine(Function<String, String> lineModifier) {
        this.modifiedLine =
                lineModifier.apply(this.modifiedLine == null ? this.line : this.modifiedLine);
    }
}
