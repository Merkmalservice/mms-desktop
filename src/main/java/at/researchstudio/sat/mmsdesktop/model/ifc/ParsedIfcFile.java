package at.researchstudio.sat.mmsdesktop.model.ifc;

import at.researchstudio.sat.merkmalservice.model.Feature;
import at.researchstudio.sat.merkmalservice.model.ifc.IfcProperty;
import at.researchstudio.sat.merkmalservice.utils.Utils;
import at.researchstudio.sat.merkmalservice.vocab.ifc.IfcPropertyType;
import at.researchstudio.sat.mmsdesktop.model.ifc.element.IfcBuiltElementLine;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.lang.NonNull;

public class ParsedIfcFile {
    private final List<IfcLine> lines;
    private final Map<Integer, IfcLine> dataLines;
    private final Map<Class<? extends IfcLine>, List<IfcLine>> dataLinesByClass;
    private Set<IfcProperty> extractedProperties;
    private Map<IfcPropertyType, List<IfcProperty>> extractedPropertyMap;
    private List<Feature> features;

    public ParsedIfcFile(List<IfcLine> lines) {
        this(lines, new HashSet<>());
    }

    public ParsedIfcFile(List<IfcLine> lines, @NonNull Set<IfcProperty> extractedProperties) {
        this.lines = lines;
        this.extractedProperties = extractedProperties;
        this.extractedPropertyMap =
                extractedProperties.stream().collect(Collectors.groupingBy(IfcProperty::getType));

        if (Objects.nonNull(lines) && lines.size() > 0) {
            this.dataLines =
                    lines.parallelStream()
                            .filter(Objects::nonNull)
                            .filter(IfcLine::hasId)
                            .collect(Collectors.toMap(IfcLine::getId, line -> line));
            this.dataLinesByClass =
                    lines.parallelStream()
                            .filter(Objects::nonNull)
                            .filter(IfcLine::hasId)
                            .collect(Collectors.groupingBy(IfcLine::getClass));
        } else {
            this.dataLines = Collections.emptyMap();
            this.dataLinesByClass = Collections.emptyMap();
        }
    }

    public List<IfcLine> getLines() {
        return lines;
    }

    public Set<IfcProperty> getExtractedProperties() {
        return extractedProperties;
    }

    public Map<IfcPropertyType, List<IfcProperty>> getExtractedPropertyMap() {
        return extractedPropertyMap;
    }

    public void setExtractedProperties(@NonNull Set<IfcProperty> extractedProperties) {
        this.extractedProperties = extractedProperties;
        this.extractedPropertyMap =
                extractedProperties.stream().collect(Collectors.groupingBy(IfcProperty::getType));
    }

    public List<Feature> getFeatures() {
        return features;
    }

    public void setFeatures(List<Feature> features) {
        this.features = features;
    }

    public Map<Integer, IfcLine> getDataLines() {
        return dataLines;
    }

    public Map<Class<? extends IfcLine>, List<IfcLine>> getDataLinesByClass() {
        return dataLinesByClass;
    }

    public Feature getRelatedFeature(IfcLine ifcLine) {
        String name;
        if (ifcLine instanceof IfcNamedPropertyLineInterface) {
            name = ((IfcNamedPropertyLineInterface) ifcLine).getName();
        } else {
            name = null;
        }

        if (Objects.nonNull(name)) {
            String convertedName = Utils.convertIFCStringToUtf8(name);
            Optional<Feature> optionalFeature =
                    features.stream().filter(f -> convertedName.equals(f.getName())).findFirst();

            if (optionalFeature.isPresent()) {
                return optionalFeature.get();
            }
        }

        return null;
    }

    public List<IfcPropertySetLine> getRelatedPropertySetLines(IfcLine ifcLine) {
        return getPropertySetLines().parallelStream()
                .filter(
                        entryIfcLine -> {
                            if (Objects.nonNull(entryIfcLine)) {
                                return entryIfcLine.getPropertyIds().contains(ifcLine.getId());
                            }
                            return false;
                        })
                .collect(Collectors.toList());
    }

    public List<IfcPropertySetLine> getPropertySetLines() {
        return dataLinesByClass.get(IfcPropertySetLine.class).parallelStream()
                .map(l -> (IfcPropertySetLine) l)
                .collect(Collectors.toList());
    }

    public List<IfcLine> getAllLinesReferencing(IfcLine ifcLine) {
        return dataLines.entrySet().parallelStream()
                .map(Map.Entry::getValue)
                .filter(Objects::nonNull)
                .filter(dataLine -> dataLine.isReferencing(ifcLine))
                .collect(Collectors.toList());
    }

    public List<IfcLine> getAllReferencedLines(IfcLine ifcLine) {
        return ifcLine.getReferencingIds().parallelStream()
                .map(dataLines::get)
                .collect(Collectors.toList());
    }

    public List<IfcLine> getRelatedObjectLines(IfcRelDefinesByPropertiesLine ifcLine) {
        return ifcLine.getRelatedObjectIds().parallelStream()
                .map(dataLines::get)
                .collect(Collectors.toList());
    }

    public List<IfcLine> getPropertySetChildLines(IfcPropertySetLine ifcLine) {
        return ifcLine.getPropertyIds().stream().map(dataLines::get).collect(Collectors.toList());
    }

    public List<IfcRelDefinesByPropertiesLine> getRelDefinesByPropertiesLinesReferencing(
            IfcPropertySetLine ifcLine) {
        return dataLinesByClass.get(IfcRelDefinesByPropertiesLine.class).parallelStream()
                .map(l -> (IfcRelDefinesByPropertiesLine) l)
                .filter(
                        entryIfcLine ->
                                Objects.nonNull(entryIfcLine)
                                        && entryIfcLine.getPropertySetId() == ifcLine.getId())
                .collect(Collectors.toList());
    }

    public List<IfcRelDefinesByPropertiesLine> getRelDefinesByPropertiesLinesReferencing(
            IfcBuiltElementLine ifcLine) {
        return dataLinesByClass.get(IfcRelDefinesByPropertiesLine.class).parallelStream()
                .map(l -> (IfcRelDefinesByPropertiesLine) l)
                .filter(
                        entryIfcLine ->
                                Objects.nonNull(entryIfcLine)
                                        && entryIfcLine
                                                .getRelatedObjectIds()
                                                .contains(ifcLine.getId()))
                .collect(Collectors.toList());
    }

    public Map<? extends Class<? extends IfcLine>, List<IfcLine>>
            getIfcLineClassesWithOccurences() {
        return dataLinesByClass.entrySet().stream()
                //                .filter(
                //                        entry -> {
                //                            return
                // IfcBuiltElementLine.class.isAssignableFrom(entry.getKey());
                //                        })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
