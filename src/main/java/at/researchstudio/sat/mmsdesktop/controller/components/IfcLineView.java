package at.researchstudio.sat.mmsdesktop.controller.components;

import at.researchstudio.sat.merkmalservice.model.Feature;
import at.researchstudio.sat.merkmalservice.utils.Utils;
import at.researchstudio.sat.mmsdesktop.constants.ViewConstants;
import at.researchstudio.sat.mmsdesktop.model.ifc.*;
import at.researchstudio.sat.mmsdesktop.model.ifc.element.IfcBuiltElementLine;
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
import org.apache.jena.ext.com.google.common.base.Throwables;
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
        getChildren().add(new IfcLineComponent(ifcLine));
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
                        this.parsedIfcFile
                                .get()
                                .getRelDefinesByPropertiesLinesReferencing(
                                        (IfcBuiltElementLine) ifcLine);

                for (IfcRelDefinesByPropertiesLine relDefinesByPropertiesLine :
                        relDefinesByPropertiesLines) {
                    IfcLine propertySetLine =
                            this.parsedIfcFile
                                    .get()
                                    .getDataLines()
                                    .get(relDefinesByPropertiesLine.getRelatedSetId());
                    if (propertySetLine instanceof IfcPropertySetLine) {
                        IfcPropertySetComponent propSetsBox =
                                new IfcPropertySetComponent(
                                        (IfcPropertySetLine) propertySetLine, parsedIfcFile.get());
                        getChildren().add(propSetsBox);
                    } else if (propertySetLine instanceof IfcElementQuantityLine) {
                        IfcElementQuantityComponent elementQuantityBox =
                                new IfcElementQuantityComponent(
                                        (IfcElementQuantityLine) propertySetLine,
                                        parsedIfcFile.get());
                        getChildren().add(elementQuantityBox);
                    } else {
                        getChildren().add(new IfcLineComponent(propertySetLine));
                    }
                }
            }
            if (ifcLine instanceof IfcNamedPropertyLineInterface) {
                accordion.getPanes().add(correspondingFeaturePane);
                List<IfcPropertySetLine> relatedPropertySets =
                        this.parsedIfcFile.get().getRelatedPropertySetLines(ifcLine);

                if (!relatedPropertySets.isEmpty()) {

                    for (IfcPropertySetLine relatedPropertySet : relatedPropertySets) {
                        IfcPropertySetComponent propSetsBox =
                                new IfcPropertySetComponent(
                                        relatedPropertySet, parsedIfcFile.get());

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
                        IfcElementQuantityComponent propSetsBox =
                                new IfcElementQuantityComponent(
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
                    FeatureView featureView = new FeatureView();
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
                            List<IfcLine> lines =
                                    parsedIfcFile.get().getAllLinesReferencing(ifcLine);
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
                        logger.error("IfcLineView referencingLinesTask failed:");
                        logger.error(
                                Throwables.getStackTraceAsString(
                                        referencingLinesTask.getException()));
                        // TODO: MAYBE DIALOG INSTEAD
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
                            List<IfcLine> lines =
                                    parsedIfcFile.get().getAllReferencedLines(ifcLine);
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
                        logger.error("IfcLineView referencedLineTask failed:");
                        logger.error(
                                Throwables.getStackTraceAsString(
                                        referencedLineTask.getException()));
                        // TODO: MAYBE DIALOG INSTEAD
                    });

            new Thread(referencedLineTask).start();
        }
    }

    private List<Node> createLineComponents(List<IfcLine> lines) {
        List<Node> elements = new ArrayList<>();

        if (!lines.isEmpty()) {
            for (IfcLine relatedLine : lines) {
                elements.add(new IfcLineComponent(relatedLine));
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
