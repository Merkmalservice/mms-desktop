package at.researchstudio.sat.mmsdesktop.controller.components;

import at.researchstudio.sat.merkmalservice.model.Feature;
import at.researchstudio.sat.merkmalservice.utils.Utils;
import at.researchstudio.sat.mmsdesktop.model.ifc.*;
import com.jfoenix.controls.JFXTextArea;
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.stream.Collectors;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IfcLineView extends VBox {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private IfcLine ifcLine;
    private ObservableMap<String, IfcLine> ifcDataLines;
    private ObservableList<Feature> extractedFeatures;

    private final JFXTextArea tempTextArea;

    public IfcLineView() {
        tempTextArea = new JFXTextArea();
        tempTextArea.setEditable(false);
        getChildren().add(tempTextArea);
    }

    public void setIfcLine(IfcLine ifcLine) {
        this.ifcLine = ifcLine;
        processDataChange();
    }

    private void processDataChange() {
        getChildren().clear();
        getChildren().add(tempTextArea);

        if (Objects.nonNull(ifcDataLines)
                && Objects.nonNull(ifcLine)
                && Objects.nonNull(extractedFeatures)) {
            tempTextArea.setText("");

            StringBuilder sb = new StringBuilder();
            if (ifcLine instanceof IfcSinglePropertyValueLine
                    || ifcLine instanceof IfcQuantityLine
                    || ifcLine instanceof IfcPropertyEnumeratedValueLine) {
                sb.append("Selected Line: ")
                        .append(System.lineSeparator())
                        .append(ifcLine)
                        .append(System.lineSeparator());
                List<IfcLine> relatedLines = getRelatedLines(ifcLine);

                sb.append("Related Lines: ").append(System.lineSeparator());
                // TODO: Related Properties
                Feature relatedFeature = getRelatedFeature(ifcLine);

                if (Objects.nonNull(relatedFeature)) {
                    FeatureView featureView = new FeatureView();
                    featureView.setFeature(relatedFeature);
                    getChildren().add(featureView);
                }

                for (IfcLine relatedIfcLine : relatedLines) {
                    sb.append(relatedIfcLine).append(System.lineSeparator());

                    // TODO: MAKE THIS SOMEHOW BETTER...
                    List<IfcLine> relatedIfcLines2 = getRelatedLines(relatedIfcLine);

                    sb.append("Sibling Lines: ").append(relatedIfcLines2);
                }
            } else if (ifcLine instanceof IfcSIUnitLine) {
                sb.append(ifcLine);
            }

            tempTextArea.setText(sb.toString());
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

    private List<IfcLine> getRelatedLines(IfcLine ifcLine) {
        // TODO: GET ALL LINES RELATED TO THE GIVEN ID
        if (ifcLine instanceof IfcSinglePropertyValueLine
                || ifcLine instanceof IfcQuantityLine
                || ifcLine instanceof IfcPropertyEnumeratedValueLine) {
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
                    .collect(Collectors.toList());
        } else if (ifcLine instanceof IfcPropertySetLine) {
            return this.ifcDataLines.entrySet().parallelStream()
                    .filter(
                            entry -> {
                                IfcLine entryIfcLine = entry.getValue();
                                if (Objects.nonNull(entryIfcLine)) {
                                    return ((IfcPropertySetLine) ifcLine)
                                            .getPropertyIds()
                                            .contains(entryIfcLine.getId());
                                }
                                return false;
                            })
                    .map(Map.Entry::getValue)
                    .collect(Collectors.toList());
        } else {
            logger.warn(
                    "COULD NOT FIND RELATED LINES LINE CLASS RELATION IS NOT YET DETERMINED: "
                            + ifcLine);
            return Collections.emptyList();
        }
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
