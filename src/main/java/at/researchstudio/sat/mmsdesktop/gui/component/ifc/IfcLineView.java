package at.researchstudio.sat.mmsdesktop.gui.component.ifc;

import at.researchstudio.sat.merkmalservice.ifc.ParsedIfcFile;
import at.researchstudio.sat.merkmalservice.ifc.model.*;
import at.researchstudio.sat.merkmalservice.ifc.model.element.IfcBuiltElementLine;
import at.researchstudio.sat.merkmalservice.model.Feature;
import at.researchstudio.sat.merkmalservice.utils.Utils;
import at.researchstudio.sat.mmsdesktop.constants.ViewConstants;
import at.researchstudio.sat.mmsdesktop.gui.component.feature.FeatureBox;
import com.jfoenix.controls.JFXSpinner;
import java.lang.invoke.MethodHandles;
import java.util.*;
import javafx.beans.property.ObjectProperty;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IfcLineView extends VBox {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ResourceBundle resourceBundle;
    private IfcLine ifcLine;

    private ObjectProperty<ParsedIfcFile> parsedIfcFile;

    private final Label selectedLineLabel;

    private final Accordion accordion;
    private final TitledPane correspondingFeaturePane;
    private final TitledPane referencingLinesPane;
    private final TitledPane referencedLinesPane;

    public IfcLineView() {
        this.resourceBundle = ResourceBundle.getBundle("messages", Locale.getDefault());

        this.selectedLineLabel = new Label(resourceBundle.getString("label.line.selected"));
        this.selectedLineLabel.setFont(ViewConstants.FONT_PT16_SYSTEM_BOLD);
        this.selectedLineLabel.setWrapText(true);

        accordion = new Accordion();

        correspondingFeaturePane =
                new TitledPane(
                        resourceBundle.getString("label.line.correspondingFeature"),
                        new Label(resourceBundle.getString("label.notPresent")));
        correspondingFeaturePane.setFont(ViewConstants.FONT_PT16_SYSTEM_BOLD);
        referencingLinesPane =
                new TitledPane(
                        resourceBundle.getString("label.line.referencingLines"),
                        new Label(resourceBundle.getString("label.notPresent")));
        referencingLinesPane.setFont(ViewConstants.FONT_PT16_SYSTEM_BOLD);
        referencedLinesPane =
                new TitledPane(
                        resourceBundle.getString("label.line.referencedLines"),
                        new Label(resourceBundle.getString("label.notPresent")));
        referencedLinesPane.setFont(ViewConstants.FONT_PT16_SYSTEM_BOLD);
    }

    public void setParsedIfcFile(ObjectProperty<ParsedIfcFile> parsedIfcFile) {
        this.parsedIfcFile = parsedIfcFile;
    }

    public void setIfcLine(IfcLine ifcLine) {
        this.ifcLine = ifcLine;
        processDataChange();
    }

    private void addLineToView(IfcLine ifcLine) {
        getChildren().add(new IfcLineBox(ifcLine));
    }

    private void processDataChange() {
        getChildren().clear();
        accordion.getPanes().clear();

        if (Objects.nonNull(parsedIfcFile)
                && Objects.nonNull(parsedIfcFile.get())
                && Objects.nonNull(ifcLine)) {
            getChildren().add(selectedLineLabel);
            addLineToView(ifcLine);
            if (ifcLine instanceof IfcBuiltElementLine) {
                List<IfcRelDefinesByPropertiesLine> relDefinesByPropertiesLines =
                        this.parsedIfcFile.get().getRelDefinesByPropertiesLinesReferencing(ifcLine);

                for (IfcRelDefinesByPropertiesLine relDefinesByPropertiesLine :
                        relDefinesByPropertiesLines) {
                    IfcLine propertySetLine =
                            this.parsedIfcFile
                                    .get()
                                    .getDataLines()
                                    .get(relDefinesByPropertiesLine.getRelatingPropertySetId());
                    if (propertySetLine instanceof IfcPropertySetLine) {
                        IfcPropertySetBox propSetsBox =
                                new IfcPropertySetBox(
                                        (IfcPropertySetLine) propertySetLine, parsedIfcFile.get());
                        getChildren().add(propSetsBox);
                    } else if (propertySetLine instanceof IfcElementQuantityLine) {
                        IfcElementQuantityBox elementQuantityBox =
                                new IfcElementQuantityBox(
                                        (IfcElementQuantityLine) propertySetLine,
                                        parsedIfcFile.get());
                        getChildren().add(elementQuantityBox);
                    } else {
                        getChildren().add(new IfcLineBox(propertySetLine));
                    }
                }
            }
            if (ifcLine instanceof IfcNamedPropertyLineInterface) {
                accordion.getPanes().add(correspondingFeaturePane);
                List<IfcPropertySetLine> relatedPropertySets =
                        this.parsedIfcFile.get().getRelatedPropertySetLines(ifcLine);

                if (!relatedPropertySets.isEmpty()) {

                    for (IfcPropertySetLine relatedPropertySet : relatedPropertySets) {
                        IfcPropertySetBox propSetsBox =
                                new IfcPropertySetBox(relatedPropertySet, parsedIfcFile.get());

                        String propSetName = relatedPropertySet.getName();
                        String convertedPropSetName =
                                Objects.nonNull(propSetName)
                                        ? Utils.convertIFCStringToUtf8(propSetName)
                                        : "NO NAME";
                        TitledPane propSetPane =
                                new TitledPane(
                                        "'"
                                                + convertedPropSetName
                                                + "'/"
                                                + relatedPropertySet.getId(),
                                        propSetsBox); // TODO: Better Key
                        propSetPane.setFont(ViewConstants.FONT_PT16_SYSTEM_BOLD);
                        accordion.getPanes().add(propSetPane);
                    }
                }

                List<IfcElementQuantityLine> relatedElementQuantityLines =
                        this.parsedIfcFile.get().getRelatedElementQuantityLines(ifcLine);

                if (!relatedElementQuantityLines.isEmpty()) {
                    for (IfcElementQuantityLine relatedElementQuantity :
                            relatedElementQuantityLines) {
                        IfcElementQuantityBox propSetsBox =
                                new IfcElementQuantityBox(
                                        relatedElementQuantity, parsedIfcFile.get());

                        String elementQuantityName = relatedElementQuantity.getName();
                        String convertedElementQuantityName =
                                Objects.nonNull(elementQuantityName)
                                        ? Utils.convertIFCStringToUtf8(elementQuantityName)
                                        : "NO NAME";
                        TitledPane elementQuantityPane =
                                new TitledPane(
                                        "'"
                                                + convertedElementQuantityName
                                                + "'/"
                                                + relatedElementQuantity.getId(),
                                        propSetsBox); // TODO: Better Key
                        elementQuantityPane.setFont(ViewConstants.FONT_PT16_SYSTEM_BOLD);
                        accordion.getPanes().add(elementQuantityPane);
                    }
                }

                Feature relatedFeature = this.parsedIfcFile.get().getRelatedFeature(ifcLine);

                if (Objects.nonNull(relatedFeature)) {
                    FeatureBox featureView = new FeatureBox();
                    featureView.setShowJson(false);
                    featureView.setSpacing(10);
                    featureView.setPadding(new Insets(10, 10, 10, 10));
                    featureView.setFeature(relatedFeature);
                    getChildren().add(featureView);
                    correspondingFeaturePane.setContent(featureView);
                }
            }
            accordion.getPanes().add(referencingLinesPane);

            // TODO: MAYBE ADD PROGRESS BAR FOR REFERENCING LINES
            VBox referencingLinesBox = new VBox();
            referencingLinesBox.setSpacing(10);
            referencingLinesBox.setPadding(new Insets(10, 10, 10, 10));
            referencingLinesBox.getChildren().add(new JFXSpinner());

            Task<List<Node>> referencingLinesTask =
                    new Task<>() {
                        @Override
                        protected List<Node> call() {
                            List<IfcLine> lines = parsedIfcFile.get().getReferencingLines(ifcLine);
                            return createLineComponents(lines);
                        }
                    };

            referencingLinesPane.setContent(referencingLinesBox);
            referencingLinesTask.setOnSucceeded(
                    t -> {
                        referencingLinesBox.getChildren().clear();
                        referencingLinesBox.getChildren().addAll(referencingLinesTask.getValue());
                    });
            referencingLinesTask.setOnFailed(
                    event -> {
                        logger.error(
                                "IfcLineView referencingLinesTask failed:",
                                referencingLinesTask.getException());
                        // TODO: MAYBE SHOW DIALOG INSTEAD
                    });

            new Thread(referencingLinesTask).start();

            // TODO: MAYBE ADD PROGRESS BAR FOR REFERENCED LINES
            VBox referencedLinesBox = new VBox();
            referencedLinesBox.setSpacing(10);
            referencedLinesBox.setPadding(new Insets(10, 10, 10, 10));
            referencedLinesBox.getChildren().add(new JFXSpinner());

            accordion.getPanes().add(referencedLinesPane);
            getChildren().add(accordion);

            Task<List<Node>> referencedLineTask =
                    new Task<>() {
                        @Override
                        protected List<Node> call() {
                            List<IfcLine> lines = parsedIfcFile.get().getReferencedLines(ifcLine);
                            return createLineComponents(lines);
                        }
                    };

            referencedLinesPane.setContent(referencedLinesBox);
            referencedLineTask.setOnSucceeded(
                    t -> {
                        referencedLinesBox.getChildren().clear();
                        referencedLinesBox.getChildren().addAll(referencedLineTask.getValue());
                    });
            referencedLineTask.setOnFailed(
                    event -> {
                        logger.error(
                                "IfcLineView referencedLineTask failed:",
                                referencedLineTask.getException());
                        // TODO: MAYBE DIALOG INSTEAD
                    });

            new Thread(referencedLineTask).start();
        }
    }

    private List<Node> createLineComponents(List<IfcLine> lines) {
        List<Node> elements = new ArrayList<>();

        if (!lines.isEmpty()) {
            for (IfcLine relatedLine : lines) {
                elements.add(new IfcLineBox(relatedLine));
            }
        } else {
            elements.add(new Label(resourceBundle.getString("label.notPresent")));
        }
        return elements;
    }

    public IfcLine getIfcLine() {
        return ifcLine;
    }
}
