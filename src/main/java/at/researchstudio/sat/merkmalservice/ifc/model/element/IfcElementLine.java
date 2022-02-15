package at.researchstudio.sat.merkmalservice.ifc.model.element;

import at.researchstudio.sat.merkmalservice.ifc.model.IfcLine;
import at.researchstudio.sat.merkmalservice.ifc.support.IfcUtils;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IfcElementLine extends IfcLine {
    public static final String IDENTIFIER = "NOT-AN-IDENTIFIER-IEL";
    private static final Pattern extractPattern =
            Pattern.compile(
                    "#\\d+= (?<type>[A-Z]+)\\('[^']+',#\\d+,(?<name>'[^']+'),(?<description>('[^']+')|\\$),.*\\)");
    private String name;
    private String description;

    public IfcElementLine(String line) {
        super(line);
        Matcher matcher = extractPattern.matcher(line);
        if (matcher.find()) {
            String nameStr = matcher.group("name");
            name = (String) IfcUtils.fromStepValue(nameStr);

            String descr = matcher.group("description");
            description = (String) IfcUtils.fromStepValue(descr);
        } else {
            throw new IllegalArgumentException("IfcElement invalid: " + line);
        }
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public static void main(String[] args) {
        extract(
                "#212= IFCWALLSTANDARDCASE('18HpJuQRTEb8CuwJYP_DCh',#41,'Basiswand:STB 200:2306800',$,'Basiswand:STB 200',#181,#208,'2306800');");
        extract(
                "#6218= IFCSTAIR('2jPaQ$WlL7fO5EymyjvXEv',#41,'Ortbetontreppe:Treppe:2326395',$,'Ortbetontreppe:STB Massiv - Stufen Naturstein - 2 Stockwerkstrennlinien',#6217,$,'2326395',.STRAIGHT_RUN_STAIR.);");
    }

    private static void extract(String line) {
        System.out.println("trying: " + line);
        Matcher matcher = extractPattern.matcher(line);
        if (matcher.find()) {
            String nameStr = matcher.group("name");
            String name = (String) IfcUtils.fromStepValue(nameStr);
            String descr = matcher.group("description");
            String description = (String) IfcUtils.fromStepValue(descr);
            System.out.println("name: " + name + ", descr: " + description);
        }
    }
}
