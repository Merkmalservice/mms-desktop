package at.researchstudio.sat.mmsdesktop.controller.components;

import at.researchstudio.sat.merkmalservice.model.Feature;
import at.researchstudio.sat.merkmalservice.utils.Utils;
import at.researchstudio.sat.mmsdesktop.model.ifc.*;
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
import javafx.scene.text.Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IfcLineView extends VBox {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ResourceBundle resourceBundle;
    private IfcLine ifcLine;

    private ObjectProperty<ParsedIfcFile> parsedIfcFile;

    private static final Font pt16Font = new Font(16);
    private static final Font pt16SystemBoldFont = new Font("System Bold", 16);

    private final Label selectedLineLabel;

    private final Accordion accordion;
    private final TitledPane correspondingFeaturePane;
    private final TitledPane referencingLinesPane;
    private final TitledPane referencedLinesPane;

    public IfcLineView() {
        this.resourceBundle = ResourceBundle.getBundle("messages", Locale.getDefault());

        this.selectedLineLabel = new Label(resourceBundle.getString("label.line.selected"));
        this.selectedLineLabel.setFont(pt16SystemBoldFont);
        this.selectedLineLabel.setWrapText(true);

        accordion = new Accordion();

        correspondingFeaturePane =
                new TitledPane(
                        resourceBundle.getString("label.line.correspondingFeature"),
                        new Label(resourceBundle.getString("label.notPresent")));
        correspondingFeaturePane.setFont(pt16SystemBoldFont);
        referencingLinesPane =
                new TitledPane(
                        resourceBundle.getString("label.line.referencingLines"),
                        new Label(resourceBundle.getString("label.notPresent")));
        referencingLinesPane.setFont(pt16SystemBoldFont);
        referencedLinesPane =
                new TitledPane(
                        resourceBundle.getString("label.line.referencedLines"),
                        new Label(resourceBundle.getString("label.notPresent")));
        referencedLinesPane.setFont(pt16SystemBoldFont);
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

            if (ifcLine instanceof IfcNamedPropertyLineInterface) {
                accordion.getPanes().add(correspondingFeaturePane);
                List<IfcPropertySetLine> relatedPropertySets =
                        this.parsedIfcFile.get().getRelatedPropertySetLines(ifcLine);

                if (!relatedPropertySets.isEmpty()) {

                    for (IfcPropertySetLine l : relatedPropertySets) {
                        VBox propSetsBox = new VBox();
                        propSetsBox.setSpacing(10);
                        propSetsBox.setPadding(new Insets(10, 10, 10, 10));

                        String propSetName = l.getName();
                        String convertedPropSetName =
                                Objects.nonNull(propSetName)
                                        ? Utils.convertIFCStringToUtf8(propSetName)
                                        : "NO NAME";
                        TitledPane propSetPane =
                                new TitledPane(
                                        "'" + convertedPropSetName + "'/" + l.getId(),
                                        propSetsBox); // TODO: Better Key
                        propSetPane.setFont(pt16SystemBoldFont);
                        accordion.getPanes().add(propSetPane);

                        propSetsBox.getChildren().add(new JFXSpinner());

                        Task<List<Node>> propSetTask =
                                new Task<>() {
                                    @Override
                                    protected List<Node> call() {
                                        List<Node> propSetNodes = new ArrayList<>();

                                        propSetNodes.add(new IfcLineComponent(l));

                                        Label relDefinesLabel =
                                                new Label(
                                                        resourceBundle.getString(
                                                                "label.line.relDefinesLines"));
                                        relDefinesLabel.setFont(pt16SystemBoldFont);
                                        relDefinesLabel.setWrapText(true);
                                        propSetNodes.add(relDefinesLabel);

                                        List<IfcRelDefinesByPropertiesLine>
                                                relDefinesByPropertiesLines =
                                                        parsedIfcFile
                                                                .get()
                                                                .getRelDefinesByPropertiesLinesReferencing(
                                                                        l);

                                        parsedIfcFile
                                                .get()
                                                .getBuiltElementLines(); // TODO: FIGURE OUT

                                        if (!relDefinesByPropertiesLines.isEmpty()) {
                                            for (IfcRelDefinesByPropertiesLine
                                                    relDefinesByPropertiesLine :
                                                            relDefinesByPropertiesLines) {
                                                propSetNodes.add(
                                                        new IfcLineComponent(
                                                                relDefinesByPropertiesLine));

                                                List<IfcLine> relatedObjectLines =
                                                        parsedIfcFile
                                                                .get()
                                                                .getRelatedObjectLines(
                                                                        relDefinesByPropertiesLine);

                                                Label relatedObjectsLabel =
                                                        new Label(
                                                                resourceBundle.getString(
                                                                        "label.line.correspondingObjects"));
                                                relatedObjectsLabel.setFont(pt16SystemBoldFont);
                                                relatedObjectsLabel.setWrapText(true);
                                                propSetNodes.add(relatedObjectsLabel);

                                                if (!relatedObjectLines.isEmpty()) {
                                                    for (IfcLine relatedObjectLine :
                                                            relatedObjectLines) {
                                                        propSetNodes.add(
                                                                new IfcLineComponent(
                                                                        relatedObjectLine));
                                                    }
                                                }
                                            }
                                        }

                                        Label siblingPropertiesLabel =
                                                new Label(
                                                        resourceBundle.getString(
                                                                "label.line.siblingsOfLine"));
                                        siblingPropertiesLabel.setFont(pt16SystemBoldFont);
                                        siblingPropertiesLabel.setWrapText(true);
                                        propSetNodes.add(siblingPropertiesLabel);

                                        List<IfcLine> propertySetChildLines =
                                                parsedIfcFile.get().getPropertySetChildLines(l);

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
                                    propSetsBox.getChildren().clear();
                                    propSetsBox.getChildren().addAll(propSetTask.getValue());
                                });
                        new Thread(propSetTask).start();
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

            Task<List<Node>> refLineTask =
                    new Task<>() {
                        @Override
                        protected List<Node> call() {
                            List<IfcLine> lines =
                                    parsedIfcFile.get().getAllLinesReferencing(ifcLine);
                            List<Node> elements = new ArrayList<>();

                            if (!lines.isEmpty()) {
                                for (IfcLine relatedLine : lines) {
                                    elements.add(new IfcLineComponent(relatedLine));
                                }
                            } else {
                                elements.add(
                                        new Label(resourceBundle.getString("label.notPresent")));
                            }
                            return elements;
                        }
                    };

            referencingLinesPane.setContent(referencingLinesBox);
            refLineTask.setOnSucceeded(
                    t -> {
                        referencingLinesBox.getChildren().clear();
                        referencingLinesBox.getChildren().addAll(refLineTask.getValue());
                    });

            new Thread(refLineTask).start();

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
                            List<Node> elements = new ArrayList<>();

                            if (!lines.isEmpty()) {
                                for (IfcLine referencedLine : lines) {
                                    elements.add(new IfcLineComponent(referencedLine));
                                }
                            } else {
                                elements.add(
                                        new Label(resourceBundle.getString("label.notPresent")));
                            }
                            return elements;
                        }
                    };

            referencedLinesPane.setContent(referencedLinesBox);
            referencedLineTask.setOnSucceeded(
                    t -> {
                        referencedLinesBox.getChildren().clear();
                        referencedLinesBox.getChildren().addAll(referencedLineTask.getValue());
                    });

            new Thread(referencedLineTask).start();
        }
    }

    public IfcLine getIfcLine() {
        return ifcLine;
    }
}
