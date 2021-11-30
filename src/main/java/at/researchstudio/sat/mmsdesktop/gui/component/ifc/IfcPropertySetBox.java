package at.researchstudio.sat.mmsdesktop.gui.component.ifc;

import at.researchstudio.sat.merkmalservice.ifc.ParsedIfcFile;
import at.researchstudio.sat.merkmalservice.ifc.model.IfcLine;
import at.researchstudio.sat.merkmalservice.ifc.model.IfcPropertySetLine;
import at.researchstudio.sat.merkmalservice.utils.Utils;
import at.researchstudio.sat.mmsdesktop.constants.ViewConstants;
import com.jfoenix.controls.JFXSpinner;
import java.lang.invoke.MethodHandles;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IfcPropertySetBox extends VBox {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public IfcPropertySetBox(
            final IfcPropertySetLine propertySet, final ParsedIfcFile parsedIfcFile) {
        this.setSpacing(10);
        this.setPadding(new Insets(10, 10, 10, 10));
        this.getChildren().add(new JFXSpinner());

        this.setBorder(
                new Border(
                        new BorderStroke(
                                Color.BLACK,
                                BorderStrokeStyle.SOLID,
                                ViewConstants.DEFAULT_CORNER_RADIUS,
                                BorderWidths.DEFAULT)));
        this.setBackground(
                new Background(
                        new BackgroundFill(
                                ViewConstants.PROPERTYSET_COMPONENT_BG,
                                ViewConstants.DEFAULT_CORNER_RADIUS,
                                new Insets(0.0))));

        Task<List<Node>> task =
                new Task<>() {
                    @Override
                    protected List<Node> call() {
                        List<Node> propSetNodes = new ArrayList<>();

                        String name = propertySet.getName();
                        String convertedPropSetName =
                                Objects.nonNull(name)
                                        ? Utils.convertIFCStringToUtf8(name)
                                        : "NO NAME";
                        Label titleLabel = new Label(convertedPropSetName);
                        titleLabel.setFont(ViewConstants.FONT_PT16_SYSTEM_BOLD);
                        titleLabel.setTooltip(new Tooltip(propertySet.getLine()));
                        propSetNodes.add(titleLabel);

                        List<IfcLine> propertySetChildLines =
                                parsedIfcFile.getPropertySetChildLines(propertySet);

                        if (!propertySetChildLines.isEmpty()) {
                            for (IfcLine childLine : propertySetChildLines) {
                                propSetNodes.add(new IfcLineBox(childLine));
                            }
                        }

                        return propSetNodes;
                    }
                };

        task.setOnSucceeded(
                t -> {
                    this.getChildren().clear();
                    this.getChildren().addAll(task.getValue());
                });
        task.setOnFailed(
                event -> {
                    logger.error("IfcPropertySetComponent task failed:", task.getException());
                    // TODO: MAYBE SHOW DIALOG INSTEAD
                });
        new Thread(task).start();
    }
}
