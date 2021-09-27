package at.researchstudio.sat.mmsdesktop.model.ifc;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IfcLine {
    private static final Pattern REF_ID_PATTERN = Pattern.compile("#[0-9]*[^,)]");
    private int id;
    private final String line;
    private String stringId;
    private List<Integer> referencingIds;

    public IfcLine(String line) {
        this.line = line;
        if (line.startsWith("#")) {
            stringId = line.split("=")[0];
            id = Integer.parseInt(stringId.substring(1));
            Matcher m = REF_ID_PATTERN.matcher(line.substring(line.indexOf("=")));

            referencingIds = new ArrayList<>();
            while (m.find()) {
                try {
                    referencingIds.add(Integer.parseInt(m.group().substring(1)));
                } catch (Exception e) {
                    System.err.println(m.group() + " could not be parsed to integer");
                }
            }
        }
    }

    public String getLine() {
        return line;
    }

    public int getId() {
        return id;
    }

    public String getStringId() {
        return stringId;
    }

    public List<Integer> getReferencingIds() {
        return referencingIds;
    }

    public boolean isReferencing(IfcLine ifcLine) {
        return referencingIds.contains(ifcLine.getId());
    }

    @Override
    public String toString() {
        return line;
    }
}
