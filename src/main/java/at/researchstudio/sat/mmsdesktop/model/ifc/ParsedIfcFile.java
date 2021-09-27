package at.researchstudio.sat.mmsdesktop.model.ifc;

import at.researchstudio.sat.merkmalservice.model.Feature;
import at.researchstudio.sat.merkmalservice.model.ifc.IfcProperty;
import at.researchstudio.sat.merkmalservice.vocab.ifc.IfcPropertyType;
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
                    lines.stream()
                            .filter(line -> Objects.nonNull(line) && line.getId() != 0)
                            .collect(Collectors.toMap(IfcLine::getId, line -> line));
            this.dataLinesByClass =
                    lines.stream()
                            .filter(line -> Objects.nonNull(line) && line.getId() != 0)
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
}
