package at.researchstudio.sat.merkmalservice.ifc.model;

import java.util.ArrayList;
import java.util.List;
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
                (modifiedLine == null ? line : modifiedLine)
                        .replaceAll(("(\\(.+\\([^)]*?),?#" + itemId), "$1")
                        .replaceAll(("(\\(([^()]+(\\([^()]+\\))?)*)(,)?#" + itemId), "$1$4\\$")
                        .replaceAll("\\(\\)", "\\$");

        return false;
    }
}
