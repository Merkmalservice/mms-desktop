package at.researchstudio.sat.mmsdesktop.controller.components;

import at.researchstudio.sat.merkmalservice.model.Feature;
import at.researchstudio.sat.merkmalservice.utils.Utils;
import at.researchstudio.sat.mmsdesktop.model.ifc.*;
import com.jfoenix.controls.JFXSpinner;
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.stream.Collectors;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
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

    private ObservableMap<String, IfcLine> ifcDataLines;
    private ObservableList<FeatureLabel> extractedFeatures;
    private ObservableMap<Class<? extends IfcLine>, List<IfcLine>> ifcDataLinesByClass;

    private static final Font pt16Font = new Font(16);
    private static final Font pt16SystemBoldFont = new Font("System Bold", 16);

    private final Label selectedLineLabel;

    private final Accordion accordion;
    private final TitledPane correspondingFeaturePane;
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
        referencedLinesPane =
                new TitledPane(
                        resourceBundle.getString("label.line.referencingLines"),
                        new Label(resourceBundle.getString("label.notPresent")));
        referencedLinesPane.setFont(pt16SystemBoldFont);
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

        if (Objects.nonNull(ifcDataLines)
                && Objects.nonNull(ifcDataLinesByClass)
                && Objects.nonNull(ifcLine)
                && Objects.nonNull(extractedFeatures)) {
            getChildren().add(selectedLineLabel);
            addLineToView(ifcLine);

            if (ifcLine instanceof IfcNamedPropertyLineInterface) {
                accordion.getPanes().add(correspondingFeaturePane);
                List<IfcPropertySetLine> relatedPropertySets = getRelatedPropertySetLines(ifcLine);

                if (!relatedPropertySets.isEmpty()) {

                    for (IfcPropertySetLine l : relatedPropertySets) {
                        VBox propSetsBox = new VBox();
                        propSetsBox.setSpacing(10);
                        propSetsBox.setPadding(new Insets(10, 10, 10, 10));

                        String propSetName = l.getName();
                        String convertedPropSetName = Objects.nonNull(propSetName) ? Utils.convertIFCStringToUtf8(propSetName) : "NO NAME";
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
                                                        getRelDefinesByPropertiesLinesReferencing(
                                                                l);

                                        if (!relDefinesByPropertiesLines.isEmpty()) {
                                            for (IfcRelDefinesByPropertiesLine
                                                    relDefinesByPropertiesLine :
                                                            relDefinesByPropertiesLines) {
                                                propSetNodes.add(
                                                        new IfcLineComponent(
                                                                relDefinesByPropertiesLine));

                                                List<IfcLine> relatedObjectLines =
                                                        getRelatedObjectLines(
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
                                                getPropertySetChildLines(l);

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

                Feature relatedFeature = getRelatedFeature(ifcLine);

                if (Objects.nonNull(relatedFeature)) {
                    FeatureView featureView = new FeatureView();
                    featureView.setSpacing(10);
                    featureView.setPadding(new Insets(10, 10, 10, 10));
                    featureView.setFeature(relatedFeature);
                    getChildren().add(featureView);
                    correspondingFeaturePane.setContent(featureView);
                }
            }
            accordion.getPanes().add(referencedLinesPane);
            getChildren().add(accordion);

            // TODO: MAYBE ADD PROGRESS BAR FOR REF LINES
            VBox refLinesBox = new VBox();
            refLinesBox.setSpacing(10);
            refLinesBox.setPadding(new Insets(10, 10, 10, 10));
            refLinesBox.getChildren().add(new JFXSpinner());
            Task<List<Node>> refLineTask =
                    new Task<>() {
                        @Override
                        protected List<Node> call() {
                            List<IfcLine> lines = getAllLinesReferencing(ifcLine);
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

            referencedLinesPane.setContent(refLinesBox);
            refLineTask.setOnSucceeded(
                    t -> {
                        refLinesBox.getChildren().clear();
                        refLinesBox.getChildren().addAll(refLineTask.getValue());
                    });

            new Thread(refLineTask).start();
        }
    }

    private Feature getRelatedFeature(IfcLine ifcLine) {
        String name;
        if (ifcLine instanceof IfcNamedPropertyLineInterface) {
            name = ((IfcNamedPropertyLineInterface) ifcLine).getName();
        } else {
            name = null;
        }

        if (Objects.nonNull(name)) {
            String convertedName = Utils.convertIFCStringToUtf8(name);
            Optional<FeatureLabel> optionalFeature =
                    this.extractedFeatures.stream()
                            .filter(f -> convertedName.equals(f.getFeature().getName()))
                            .findFirst();

            if (optionalFeature.isPresent()) {
                return optionalFeature.get().getFeature();
            }
        }

        return null;
    }

    private List<IfcLine> getAllLinesReferencing(IfcLine ifcLine) {
        return this.ifcDataLines.entrySet().parallelStream()
                .filter(
                        entry -> {
                            IfcLine entryIfcLine = entry.getValue();
                            if (Objects.nonNull(entryIfcLine)) {
                                return entryIfcLine
                                        .getLine()
                                        .matches("(.*)" + ifcLine.getId() + "([,)])(.*)");
                            }
                            return false;
                        })
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    private List<IfcPropertySetLine> getRelatedPropertySetLines(IfcLine ifcLine) {
        return this.ifcDataLinesByClass.get(IfcPropertySetLine.class).parallelStream()
                .map(l -> (IfcPropertySetLine) l)
                .filter(
                        entryIfcLine -> {
                            if (Objects.nonNull(entryIfcLine)) {
                                return entryIfcLine.getPropertyIds().contains(ifcLine.getId());
                            }
                            return false;
                        })
                .collect(Collectors.toList());
    }

    private List<IfcLine> getRelatedObjectLines(IfcRelDefinesByPropertiesLine ifcLine) {
        return ifcLine.getRelatedObjectIds().stream()
                .map(objectId -> this.ifcDataLines.get(objectId))
                .collect(Collectors.toList());
    }

    private List<IfcLine> getPropertySetChildLines(IfcPropertySetLine ifcLine) {
        return ifcLine.getPropertyIds().stream()
                .map(propertyId -> this.ifcDataLines.get(propertyId))
                .collect(Collectors.toList());
    }

    private List<IfcRelDefinesByPropertiesLine> getRelDefinesByPropertiesLinesReferencing(
            IfcPropertySetLine ifcLine) {
        return this.ifcDataLinesByClass.get(IfcRelDefinesByPropertiesLine.class).parallelStream()
                .map(l -> (IfcRelDefinesByPropertiesLine) l)
                .filter(
                        entryIfcLine -> {
                            if (Objects.nonNull(entryIfcLine)) {
                                return entryIfcLine.getPropertySetId().equals(ifcLine.getId());
                            }
                            return false;
                        })
                .collect(Collectors.toList());
    }

    public IfcLine getIfcLine() {
        return ifcLine;
    }

    public ObservableMap<String, IfcLine> getIfcDataLines() {
        return ifcDataLines;
    }

    public void setIfcDataLines(ObservableMap<String, IfcLine> ifcDataLines) {
        this.ifcDataLines = ifcDataLines;
    }

    /*public ObservableList<Feature> getExtractedFeatures() {
        return extractedFeatures;
    }*/

    public void setExtractedFeatures(ObservableList<FeatureLabel> extractedFeatures) {
        this.extractedFeatures = extractedFeatures;
    }

    public void setIfcDataLinesByClass(
            ObservableMap<Class<? extends IfcLine>, List<IfcLine>> inputFileDataLinesByClass) {
        this.ifcDataLinesByClass = inputFileDataLinesByClass;
    }
}
