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

    private IfcLine ifcLine;
    private ObservableMap<String, IfcLine> ifcDataLines;
    private ObservableList<Feature> extractedFeatures;

    private static final Font pt16Font = new Font(16);
    private static final Font pt16SystemBoldFont = new Font("System Bold", 16);

    public void setIfcLine(IfcLine ifcLine) {
        this.ifcLine = ifcLine;
        processDataChange();
    }

    private void processDataChange() {
        getChildren().clear();

        if (Objects.nonNull(ifcDataLines)
                && Objects.nonNull(ifcLine)
                && Objects.nonNull(extractedFeatures)) {
            Label l1 = new Label("TODO: SELECTED LINE LABEL");
            l1.setFont(pt16SystemBoldFont);
            l1.setWrapText(true);
            getChildren().add(l1); //TODO: SELECTED LINE LABEL
            Label l2 = new Label(ifcLine.toString());
            l2.setWrapText(true);
            getChildren().add(l2);

            if (ifcLine instanceof IfcSinglePropertyValueLine
                    || ifcLine instanceof IfcQuantityLine
                    || ifcLine instanceof IfcPropertyEnumeratedValueLine) {
                List<IfcPropertySetLine> relatedPropertySets = getRelatedPropertySetLines(ifcLine);

                if(!relatedPropertySets.isEmpty()) {
                    Label l3 = new Label("TODO: MEMBER OF PROPSETS LABEL");
                    l3.setFont(pt16SystemBoldFont);
                    l3.setWrapText(true);
                    getChildren().add(l3); //TODO: PROPERTY SET LABEL
                    for(IfcPropertySetLine l : relatedPropertySets) {
                        Label l4 = new Label(l.toString());
                        l4.setWrapText(true);
                        getChildren().add(l4); //TODO: IFCLINE VIEW

                        Label l5 = new Label("TODO: SIBLINGS OF PROPERTYSET LINES");
                        l5.setFont(pt16SystemBoldFont);
                        l5.setWrapText(true);
                        getChildren().add(l5); //TODO: Siblings of propertyset

                        List<IfcLine> propertySetChildLines = getPropertySetChildLines(l);

                        if (!propertySetChildLines.isEmpty()) {
                            for(IfcLine childLine : propertySetChildLines) {
                                Label l6 = new Label(childLine.toString());
                                l6.setWrapText(true);
                                getChildren().add(l6); //TODO: Child Line View (IFCLINE VIEW)
                            }
                        }
                    }
                }

                Feature relatedFeature = getRelatedFeature(ifcLine);

                if (Objects.nonNull(relatedFeature)) {
                    Label l8 = new Label("FEATURE REFERENCED IN THIS LINE: "); //TODO: LABEL
                    l8.setFont(pt16SystemBoldFont);
                    l8.setWrapText(true);
                    getChildren().add(l8);
                    FeatureView featureView = new FeatureView();
                    featureView.setFeature(relatedFeature);
                    getChildren().add(featureView);
                }
            }
            Label l8 = new Label("LINES REFERENCING THE LINE LABEL: ");
            l8.setFont(pt16SystemBoldFont);
            l8.setWrapText(true);
            getChildren().add(l8); //TODO: ALL RELATED LINES LABEL
            List<IfcLine> referencingLines = getAllLinesReferencing(ifcLine);

            if (!referencingLines.isEmpty()) {
                for(IfcLine relatedLine : referencingLines) {
                    Label l7 = new Label(relatedLine.toString());
                    l7.setWrapText(true);
                    getChildren().add(l7); //TODO: Related Line View (IFCLINE VIEW)
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
                                                return entryIfcLine.getLine().matches("(.*)"+ifcLine.getId()+"(,|\\))(.*)");
                                            }
                                            return false;
                                        })
                                .map(Map.Entry::getValue)
                                .collect(Collectors.toList());
    }

    private List<IfcPropertySetLine> getRelatedPropertySetLines(IfcLine ifcLine) {
        return this.ifcDataLines.entrySet().parallelStream()
                                .filter(
                                        entry -> {
                                            IfcLine entryIfcLine = entry.getValue();
                                            if (Objects.nonNull(entryIfcLine)
                                                    && entryIfcLine instanceof IfcPropertySetLine) {
                                                return ((IfcPropertySetLine) entryIfcLine)
                                                        .getPropertyIds()
                                                        .contains(ifcLine.getId());
                                            }
                                            return false;
                                        })
                                .map(Map.Entry::getValue)
                                .map(l -> (IfcPropertySetLine) l)
                                .collect(Collectors.toList());
    }

    private List<IfcLine> getPropertySetChildLines(IfcPropertySetLine ifcLine) {
        return this.ifcDataLines.entrySet().parallelStream()
                                .filter(
                                        entry -> {
                                            IfcLine entryIfcLine = entry.getValue();
                                            if (Objects.nonNull(entryIfcLine)) {
                                                return ifcLine
                                                        .getPropertyIds()
                                                        .contains(entryIfcLine.getId());
                                            }
                                            return false;
                                        })
                                .map(Map.Entry::getValue)
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
}
