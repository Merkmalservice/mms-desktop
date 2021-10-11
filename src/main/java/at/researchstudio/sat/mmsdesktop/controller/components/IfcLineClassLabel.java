package at.researchstudio.sat.mmsdesktop.controller.components;

import at.researchstudio.sat.mmsdesktop.constants.ViewConstants;
import at.researchstudio.sat.mmsdesktop.model.ifc.IfcLine;
import java.util.List;
import java.util.Map;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class IfcLineClassLabel extends HBox {
    private Class<? extends IfcLine> ifcLineClass;
    private int count;
    private String name;

    private final Label className;
    private final Label classCount;

    public IfcLineClassLabel() {
        this.setPadding(new Insets(5, 0, 5, 0));
        this.setSpacing(10);
        className = new Label();
        className.setWrapText(true);
        className.setFont(new Font("System Bold", 12));
        classCount = new Label();
        classCount.setWrapText(true);
        classCount.setBackground(
                new Background(
                        new BackgroundFill(
                                Color.valueOf("#0000ff0c"),
                                ViewConstants.DEFAULT_CORNER_RADIUS,
                                new Insets(-5.0))));
    }

    public IfcLineClassLabel(Map.Entry<Class<? extends IfcLine>, List<IfcLine>> ifcLineClass) {
        this();
        setIfcLineClass(ifcLineClass.getKey());
        setCount(ifcLineClass.getValue().size());
        getChildren().add(className);
        getChildren().add(classCount);
    }

    public void setIfcLineClass(Class<? extends IfcLine> builtElementClass) {
        this.ifcLineClass = builtElementClass;
        this.name = builtElementClass.getSimpleName();
        className.setText(this.name);
    }

    public void setCount(int count) {
        this.count = count;
        classCount.setText("" + count);
    }

    public Class<? extends IfcLine> getIfcLineClass() {
        return ifcLineClass;
    }

    public int getCount() {
        return count;
    }

    public String getName() {
        return name;
    }
}
