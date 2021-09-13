package at.researchstudio.sat.mmsdesktop.controller.components;

import at.researchstudio.sat.merkmalservice.model.Feature;
import at.researchstudio.sat.merkmalservice.utils.Utils;
import at.researchstudio.sat.mmsdesktop.model.ifc.*;
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.stream.Collectors;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.control.Label;
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
    private ObservableList<Feature> extractedFeatures;
    private ObservableMap<Class<? extends IfcLine>, List<IfcLine>> ifcDataLinesByClass;

    private static final Font pt16Font = new Font(16);
    private static final Font pt16SystemBoldFont = new Font("System Bold", 16);

    private final Label selectedLineLabel;
    private final Label memberOfPropertySetLabel;
    private final Label relDefinesLabel;
    private final Label refLineLabel;
    private final Label siblingPropertiesLabel;
    private final Label relatedObjectsLabel;
    private final Label correspondingFeatureLabel;

    public IfcLineView() {
        this.resourceBundle = ResourceBundle.getBundle("messages", Locale.getDefault());

        this.selectedLineLabel = new Label(resourceBundle.getString("label.line.selected"));
        this.selectedLineLabel.setFont(pt16SystemBoldFont);
        this.selectedLineLabel.setWrapText(true);

        this.memberOfPropertySetLabel =
                new Label(resourceBundle.getString("label.line.memberOfPropertySet"));
        this.memberOfPropertySetLabel.setFont(pt16SystemBoldFont);
        this.memberOfPropertySetLabel.setWrapText(true);

        this.relDefinesLabel = new Label(resourceBundle.getString("label.line.relDefinesLines"));
        this.relDefinesLabel.setFont(pt16SystemBoldFont);
        this.relDefinesLabel.setWrapText(true);

        siblingPropertiesLabel = new Label(resourceBundle.getString("label.line.siblingsOfLine"));
        siblingPropertiesLabel.setFont(pt16SystemBoldFont);
        siblingPropertiesLabel.setWrapText(true);

        refLineLabel = new Label(resourceBundle.getString("label.line.referencingLines"));
        refLineLabel.setFont(pt16SystemBoldFont);
        refLineLabel.setWrapText(true);

        correspondingFeatureLabel =
                new Label(resourceBundle.getString("label.line.correspondingFeature"));
        correspondingFeatureLabel.setFont(pt16SystemBoldFont);
        correspondingFeatureLabel.setWrapText(true);

        relatedObjectsLabel =
                new Label(resourceBundle.getString("label.line.correspondingObjects"));
        relatedObjectsLabel.setFont(pt16SystemBoldFont);
        relatedObjectsLabel.setWrapText(true);
    }

    public void setIfcLine(IfcLine ifcLine) {
        this.ifcLine = ifcLine;
        processDataChange();
    }

    private void addLineToView(IfcLine ifcLine) {
        Label l = new Label(ifcLine.toString()); // TODO: IFCLINE VIEW
        l.setWrapText(true);
        getChildren().add(l);
    }

    private void processDataChange() {
        getChildren().clear();

        if (Objects.nonNull(ifcDataLines)
                && Objects.nonNull(ifcDataLinesByClass)
                && Objects.nonNull(ifcLine)
                && Objects.nonNull(extractedFeatures)) {
            getChildren().add(selectedLineLabel);
            addLineToView(ifcLine);

            if (ifcLine instanceof IfcSinglePropertyValueLine
                    || ifcLine instanceof IfcQuantityLine
                    || ifcLine instanceof IfcPropertyEnumeratedValueLine) {
                List<IfcPropertySetLine> relatedPropertySets = getRelatedPropertySetLines(ifcLine);

                if (!relatedPropertySets.isEmpty()) {
                    getChildren().add(memberOfPropertySetLabel);
                    for (IfcPropertySetLine l : relatedPropertySets) {
                        addLineToView(l);

                        getChildren().add(relDefinesLabel);

                        List<IfcRelDefinesByPropertiesLine> relDefinesByPropertiesLines =
                                getRelDefinesByPropertiesLinesReferencing(l);

                        if (!relDefinesByPropertiesLines.isEmpty()) {
                            for (IfcRelDefinesByPropertiesLine relDefinesByPropertiesLine :
                                    relDefinesByPropertiesLines) {
                                addLineToView(relDefinesByPropertiesLine);

                                List<IfcLine> relatedObjectLines =
                                        getRelatedObjectLines(relDefinesByPropertiesLine);

                                getChildren().add(relatedObjectsLabel);

                                if (!relatedObjectLines.isEmpty()) {
                                    for (IfcLine relatedObjectLine : relatedObjectLines) {
                                        addLineToView(relatedObjectLine);
                                    }
                                }
                            }
                        }

                        getChildren().add(siblingPropertiesLabel);

                        List<IfcLine> propertySetChildLines = getPropertySetChildLines(l);

                        if (!propertySetChildLines.isEmpty()) {
                            for (IfcLine childLine : propertySetChildLines) {
                                addLineToView(childLine);
                            }
                        }
                    }
                }

                Feature relatedFeature = getRelatedFeature(ifcLine);

                if (Objects.nonNull(relatedFeature)) {

                    getChildren().add(correspondingFeatureLabel);
                    FeatureView featureView = new FeatureView();
                    featureView.setFeature(relatedFeature);
                    getChildren().add(featureView);
                }
            }

            getChildren().add(refLineLabel);
            List<IfcLine> referencingLines = getAllLinesReferencing(ifcLine);

            if (!referencingLines.isEmpty()) {
                for (IfcLine relatedLine : referencingLines) {
                    addLineToView(relatedLine);
                }
            }
        }
    }

    private Feature getRelatedFeature(IfcLine ifcLine) {
        String name;
        if (ifcLine instanceof IfcSinglePropertyValueLine) {
            name = ((IfcSinglePropertyValueLine) ifcLine).getName();
        } else if (ifcLine instanceof IfcQuantityLine) {
            name = ((IfcQuantityLine) ifcLine).getName();
        } else if (ifcLine instanceof IfcPropertyEnumeratedValueLine) {
            name = ((IfcPropertyEnumeratedValueLine) ifcLine).getName();
        } else {
            name = null;
        }

        if (Objects.nonNull(name)) {
            String convertedName = Utils.convertIFCStringToUtf8(name);
            Optional<Feature> optionalFeature =
                    this.extractedFeatures.stream()
                            .filter(f -> convertedName.equals(f.getName()))
                            .findFirst();

            if (optionalFeature.isPresent()) {
                return optionalFeature.get();
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
                                return ((IfcPropertySetLine) entryIfcLine)
                                        .getPropertyIds()
                                        .contains(ifcLine.getId());
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

    public ObservableList<Feature> getExtractedFeatures() {
        return extractedFeatures;
    }

    public void setExtractedFeatures(ObservableList<Feature> extractedFeatures) {
        this.extractedFeatures = extractedFeatures;
    }

    public void setIfcDataLinesByClass(
            ObservableMap<Class<? extends IfcLine>, List<IfcLine>> inputFileDataLinesByClass) {
        this.ifcDataLinesByClass = inputFileDataLinesByClass;
    }
}
