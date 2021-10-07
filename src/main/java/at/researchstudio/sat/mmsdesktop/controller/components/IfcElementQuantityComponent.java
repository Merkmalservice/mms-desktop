package at.researchstudio.sat.mmsdesktop.controller.components;

import at.researchstudio.sat.merkmalservice.utils.Utils;
import at.researchstudio.sat.mmsdesktop.model.ifc.IfcElementQuantityLine;
import at.researchstudio.sat.mmsdesktop.model.ifc.IfcLine;
import at.researchstudio.sat.mmsdesktop.model.ifc.ParsedIfcFile;
import com.jfoenix.controls.JFXSpinner;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class IfcElementQuantityComponent extends VBox {

    private static final Font pt16SystemBoldFont = new Font("System Bold", 16);

    // TODO: FIGURE OUT THIS VIEW TOO
    public IfcElementQuantityComponent(
            final IfcElementQuantityLine elementQuantity, final ParsedIfcFile parsedIfcFile) {
        this.setSpacing(10);
        this.setPadding(new Insets(10, 10, 10, 10));
        this.getChildren().add(new JFXSpinner());

        this.setBorder(
                new Border(
                        new BorderStroke(
                                Color.BLACK,
                                BorderStrokeStyle.SOLID,
                                new CornerRadii(5.0),
                                BorderWidths.DEFAULT)));
        this.setBackground(
                new Background(
                        new BackgroundFill(
                                Color.valueOf("#FFFFFF"), new CornerRadii(5.0), new Insets(0.0))));

        Task<List<Node>> propSetTask =
                new Task<>() {
                    @Override
                    protected List<Node> call() {
                        List<Node> propSetNodes = new ArrayList<>();

                        String name = elementQuantity.getName();
                        String convertedPropSetName =
                                Objects.nonNull(name)
                                        ? Utils.convertIFCStringToUtf8(name)
                                        : "NO NAME";
                        Label titleLabel = new Label(convertedPropSetName);
                        titleLabel.setFont(pt16SystemBoldFont);
                        titleLabel.setTooltip(new Tooltip(elementQuantity.getLine()));
                        propSetNodes.add(titleLabel);

                        List<IfcLine> propertySetChildLines =
                                parsedIfcFile.getElementQuantityChildLines(elementQuantity);

                        if (!propertySetChildLines.isEmpty()) {
                            for (IfcLine childLine : propertySetChildLines) {
                                propSetNodes.add(new IfcLineComponent(childLine));
                            }
                        }

                        return propSetNodes;
                    }
                };

        propSetTask.setOnSucceeded(
                t -> {
                    this.getChildren().clear();
                    this.getChildren().addAll(propSetTask.getValue());
                });
        new Thread(propSetTask).start();
    }
}
