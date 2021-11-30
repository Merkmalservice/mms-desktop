package at.researchstudio.sat.mmsdesktop.gui.component.ifc;

import at.researchstudio.sat.merkmalservice.ifc.ParsedIfcFile;
import at.researchstudio.sat.merkmalservice.ifc.model.IfcLine;
import at.researchstudio.sat.merkmalservice.ifc.model.IfcPropertySetLine;
import at.researchstudio.sat.merkmalservice.ifc.model.IfcRelDefinesByPropertiesLine;
import at.researchstudio.sat.mmsdesktop.constants.ViewConstants;
import com.jfoenix.controls.JFXSpinner;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IfcRelDefinesByPropertiesBox extends VBox {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ResourceBundle resourceBundle;

    public IfcRelDefinesByPropertiesBox(
            final IfcPropertySetLine propertySet, final ParsedIfcFile parsedIfcFile) {
        this.resourceBundle = ResourceBundle.getBundle("messages", Locale.getDefault());

        this.setSpacing(10);
        this.setPadding(new Insets(10, 10, 10, 10));
        this.getChildren().add(new JFXSpinner());

        Task<List<Node>> task =
                new Task<>() {
                    @Override
                    protected List<Node> call() {
                        List<Node> propSetNodes = new ArrayList<>();

                        propSetNodes.add(new IfcLineBox(propertySet));

                        Label relDefinesLabel =
                                new Label(resourceBundle.getString("label.line.relDefinesLines"));
                        relDefinesLabel.setFont(ViewConstants.FONT_PT16_SYSTEM_BOLD);
                        relDefinesLabel.setWrapText(true);
                        propSetNodes.add(relDefinesLabel);

                        List<IfcRelDefinesByPropertiesLine> relDefinesByPropertiesLines =
                                parsedIfcFile.getRelDefinesByPropertiesLinesForPSet(propertySet);

                        if (!relDefinesByPropertiesLines.isEmpty()) {
                            for (IfcRelDefinesByPropertiesLine relDefinesByPropertiesLine :
                                    relDefinesByPropertiesLines) {
                                propSetNodes.add(new IfcLineBox(relDefinesByPropertiesLine));

                                List<IfcLine> relatedObjectLines =
                                        parsedIfcFile.getRelatedObjectLines(
                                                relDefinesByPropertiesLine);

                                Label relatedObjectsLabel =
                                        new Label(
                                                resourceBundle.getString(
                                                        "label.line.correspondingObjects"));
                                relatedObjectsLabel.setFont(ViewConstants.FONT_PT16_SYSTEM_BOLD);
                                relatedObjectsLabel.setWrapText(true);
                                propSetNodes.add(relatedObjectsLabel);

                                if (!relatedObjectLines.isEmpty()) {
                                    for (IfcLine relatedObjectLine : relatedObjectLines) {
                                        propSetNodes.add(new IfcLineBox(relatedObjectLine));
                                    }
                                }
                            }
                        }

                        Label siblingPropertiesLabel =
                                new Label(resourceBundle.getString("label.line.siblingsOfLine"));
                        siblingPropertiesLabel.setFont(ViewConstants.FONT_PT16_SYSTEM_BOLD);
                        siblingPropertiesLabel.setWrapText(true);
                        propSetNodes.add(siblingPropertiesLabel);

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
                    logger.error(
                            "IfcRelDefinesByPropertiesComponent task failed:", task.getException());
                    // TODO: MAYBE SHOW DIALOG INSTEAD
                });
        new Thread(task).start();
    }
}
