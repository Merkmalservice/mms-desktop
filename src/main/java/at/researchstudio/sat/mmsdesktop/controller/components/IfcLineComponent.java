package at.researchstudio.sat.mmsdesktop.controller.components;

import at.researchstudio.sat.merkmalservice.utils.Utils;
import at.researchstudio.sat.mmsdesktop.constants.ViewConstants;
import at.researchstudio.sat.mmsdesktop.model.ifc.*;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.Objects;

public class IfcLineComponent extends HBox {
    private IfcLine ifcLine;

    private final Label lineNumber;
    private final Label propertyName;
    private final Label propertyValue;

    public IfcLineComponent() {
        this.setPadding(new Insets(5, 0, 5, 0));
        this.setSpacing(10);
        lineNumber = new Label();
        lineNumber.setTextFill(Color.DARKGRAY);
        propertyName = new Label();
        propertyName.setWrapText(true);
        propertyName.setFont(new Font("System Bold", 12));
        propertyValue = new Label();
        propertyValue.setWrapText(true);
        propertyValue.setBackground(
                new Background(
                        new BackgroundFill(
                                Color.valueOf("#0000ff0c"),
                                ViewConstants.DEFAULT_CORNER_RADIUS,
                                new Insets(-5.0))));
    }

    public IfcLineComponent(IfcLine ifcLine) {
        this();
        setIfcLine(ifcLine);
    }

    public void setIfcLine(IfcLine ifcLine) {
        this.ifcLine = ifcLine;
        this.getChildren().clear();

        lineNumber.setText(this.ifcLine.getStringId());
        getChildren().add(lineNumber);

        if (ifcLine instanceof IfcNamedPropertyLineInterface) {
            propertyName.setText(
                    Utils.convertIFCStringToUtf8(
                            ((IfcNamedPropertyLineInterface) ifcLine).getName()));
            propertyName.setTooltip(new Tooltip(ifcLine.toString()));
            getChildren().add(propertyName);

            if (ifcLine instanceof IfcSinglePropertyValueLine) {
                String value = ((IfcSinglePropertyValueLine) ifcLine).getValue();
                if (Objects.nonNull(value)) {
                    propertyValue.setText(Utils.convertIFCStringToUtf8(value));
                } else {
                    propertyValue.setText("");
                }
            } else if (ifcLine instanceof IfcQuantityLine) {
                propertyValue.setText(Double.toString(((IfcQuantityLine) ifcLine).getValue()));
            } else if (ifcLine instanceof IfcPropertyEnumeratedValueLine) {
                propertyValue.setText(
                        Utils.convertIFCStringToUtf8(
                                String.join(
                                        ", ",
                                        ((IfcPropertyEnumeratedValueLine) ifcLine).getValues())));
            }
            getChildren().add(propertyValue);
            // TODO: VIEW FOR PROP VALUE(S)
        } else {
            Label genericLabel = new Label(Utils.convertIFCStringToUtf8(ifcLine.toString()));
            genericLabel.setTooltip(new Tooltip(ifcLine.toString()));
            genericLabel.setWrapText(true);
            getChildren().add(genericLabel);
            // TODO: VIEW FOR OTHERS
        }
    }

    public IfcLine getIfcLine() {
        return ifcLine;
    }
}
