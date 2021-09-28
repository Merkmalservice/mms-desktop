package at.researchstudio.sat.mmsdesktop.controller.components;

import at.researchstudio.sat.mmsdesktop.model.ifc.IfcLine;
import javafx.scene.control.Label;

public class BuiltElementLabel extends Label {
    private Class<? extends IfcLine> builtElementClass;

    public BuiltElementLabel() {}

    public BuiltElementLabel(Class<? extends IfcLine> builtElementClass) {
        this();
        setBuiltElementClass(builtElementClass);
    }

    public void setBuiltElementClass(Class<? extends IfcLine> builtElementClass) {
        this.builtElementClass = builtElementClass;
        this.setText(builtElementClass.getName());
    }

    public Class<? extends IfcLine> getBuiltElementClass() {
        return builtElementClass;
    }
}
