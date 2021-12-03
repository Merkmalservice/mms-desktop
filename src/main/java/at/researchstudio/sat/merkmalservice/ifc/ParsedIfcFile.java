package at.researchstudio.sat.merkmalservice.ifc;

import static at.researchstudio.sat.merkmalservice.ifc.model.TypeConverter.castToOptAndLogFailure;
import static java.util.stream.Collectors.*;

import at.researchstudio.sat.merkmalservice.ifc.model.*;
import at.researchstudio.sat.merkmalservice.ifc.model.element.IfcBuiltElementLine;
import at.researchstudio.sat.merkmalservice.ifc.model.type.IfcTypeObjectLine;
import at.researchstudio.sat.merkmalservice.model.Feature;
import at.researchstudio.sat.merkmalservice.model.ifc.IfcProperty;
import at.researchstudio.sat.merkmalservice.utils.Utils;
import at.researchstudio.sat.merkmalservice.vocab.ifc.IfcPropertyType;
import at.researchstudio.sat.mmsdesktop.model.helper.FeatureSet;
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;

public class ParsedIfcFile {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final List<IfcLine> lines;
    private final Map<Integer, IfcLine> dataLines;
    private final Map<Class<? extends IfcLine>, List<IfcLine>> dataLinesByClass;
    private final Set<IfcProperty> extractedProperties;
    private final Map<IfcPropertyType, List<IfcProperty>> extractedPropertyMap;
    private final List<Feature> features;
    private final Set<FeatureSet> featureSets;

    public <T extends IfcLine> List<? extends IfcLine> getProperties(
            T element, Predicate<IfcLine> predicate) {
        List<IfcLine> properties = new ArrayList<>();
        properties.addAll(
                getRelDefinesByPropertiesLinesReferencing(element).stream()
                        .map(this::getPropertySet)
                        .filter(Optional::isPresent)
                        .flatMap(ps -> getPropertySetChildLines(ps.get()).stream())
                        .filter(predicate)
                        .collect(toList()));
        properties.addAll(
                getRelDefinesByTypeReferencing(element).stream()
                        .flatMap(rel -> getPropertySets(rel).stream())
                        .flatMap(ps -> getPropertySetChildLines(ps).stream())
                        .filter(predicate)
                        .collect(toList()));
        return properties;
    }

    public <T extends IfcLine> void removeProperty(T element, Predicate<IfcLine> predicate) {
        List<Pair<IfcPropertySetLine, IfcSinglePropertyValueLine>> toDelete = new ArrayList<>();
        toDelete.addAll(
                getRelDefinesByPropertiesLinesReferencing(element).stream()
                        .map(this::getPropertySet)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .flatMap(
                                ps ->
                                        getPropertySetChildLines(ps).stream()
                                                .filter(predicate)
                                                .map(prop -> Pair.of(ps, prop)))
                        .collect(Collectors.toList()));
        toDelete.addAll(
                getRelDefinesByTypeReferencing(element).stream()
                        .flatMap(rel -> getPropertySets(rel).stream())
                        .flatMap(
                                ps ->
                                        getPropertySetChildLines(ps).stream()
                                                .filter(predicate)
                                                .map(prop -> Pair.of(ps, prop)))
                        .collect(toList()));
        toDelete.forEach(
                pSetAndProp ->
                        removeSinglePropertyFromPropertySet(
                                pSetAndProp.getLeft(), pSetAndProp.getRight()));
    }

    public <T extends IfcBuiltElementLine> void removeQuantity(
            T element, Predicate<IfcLine> predicate) {
        List<IfcElementQuantityLine> elementQuantities = getRelatedElementQuantityLines(element);
        List<IfcLine> toDelete = new ArrayList<>();
        for (IfcElementQuantityLine elementQuantity : elementQuantities) {
            List<IfcLine> quantities = getElementQuantityChildLines(elementQuantity);
            toDelete.addAll(quantities.stream().filter(predicate).collect(Collectors.toList()));
        }
        toDelete.forEach(d -> removeLine(d));
    }

    private void removeSinglePropertyFromPropertySet(
            IfcPropertySetLine propertySetLine, IfcSinglePropertyValueLine propertyValueLine) {
        propertySetLine.removeReferenceTo(propertyValueLine);
        if (propertySetLine.getPropertyIds().isEmpty()) {
            removeLine(propertySetLine);
        }
        if (getReferencingLines(propertyValueLine).isEmpty()) {
            removeLine(propertyValueLine);
        }
    }

    private void removeLine(IfcLine line) {
        List<IfcLine> cascadingDeletes = new ArrayList<>();
        List<IfcLine> referencing = this.getReferencingLines(line);
        for (IfcLine referencingLine : referencing) {
            if (referencingLine.removeReferenceTo(line)) {
                cascadingDeletes.add(referencingLine);
            }
        }
        this.dataLines.remove(line.getId());
        getTypeInstancePairStream(line)
                .forEach(tip -> this.dataLinesByClass.get(tip.type).remove(tip.instance));
        this.lines.remove(line);
        cascadingDeletes.forEach(toRemoveCascading -> removeLine(toRemoveCascading));
    }

    public Optional<IfcPropertySetLine> getPropertySet(
            IfcRelDefinesByPropertiesLine propertySetRel) {
        IfcLine related = getDataLines().get(propertySetRel.getRelatingPropertySetId());
        return TypeConverter.castToOptAndLogFailure(
                related, IfcPropertySetLine.class, "find reference to a property set");
    }

    public List<IfcPropertySetLine> getPropertySets(IfcRelDefinesByTypeLine propertySetRel) {
        Optional<IfcTypeObjectLine> related =
                TypeConverter.castToOptAndLogFailure(
                        getDataLines().get(propertySetRel.getRelatingTypeId()),
                        IfcTypeObjectLine.class,
                        "find references to property sets");
        return related.stream()
                .flatMap(r -> r.getPropertySetIds().stream())
                .map(dataLines::get)
                .filter(Objects::nonNull)
                .map(
                        line ->
                                TypeConverter.castToOptAndLogFailure(
                                        line,
                                        IfcPropertySetLine.class,
                                        "get a referenced property set"))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
    }

    private static class TypeInstancePair {
        final Class<? extends IfcLine> type;
        final IfcLine instance;

        public TypeInstancePair(Class<? extends IfcLine> type, IfcLine instance) {
            this.type = type;
            this.instance = instance;
        }
    }

    public ParsedIfcFile(
            List<IfcLine> lines,
            @NonNull Set<IfcProperty> extractedProperties,
            StringBuilder extractLog) {
        this.lines = lines;
        this.extractedProperties = extractedProperties;
        this.extractedPropertyMap =
                extractedProperties.stream().collect(groupingBy(IfcProperty::getType));
        if (Objects.nonNull(lines) && lines.size() > 0) {
            this.dataLines =
                    lines.parallelStream()
                            .filter(Objects::nonNull)
                            .filter(IfcLine::hasId)
                            .collect(toMap(IfcLine::getId, line -> line));
            this.dataLinesByClass =
                    lines.parallelStream()
                            .filter(Objects::nonNull)
                            .filter(IfcLine::hasId)
                            .flatMap(this::getTypeInstancePairStream)
                            .collect(groupingBy(t -> t.type, mapping(t -> t.instance, toList())));
            this.features =
                    IfcFileReader.extractFeaturesFromProperties(
                            this.extractedPropertyMap, extractLog);
            HashMap<String, Set<String>> featureSetFeatureNameMap = new HashMap<>();
            this.dataLinesByClass
                    .getOrDefault(IfcPropertySetLine.class, Collections.emptyList())
                    .stream()
                    .map(l -> (IfcPropertySetLine) l)
                    .forEach(
                            line -> {
                                String convertedName = Utils.convertIFCStringToUtf8(line.getName());
                                Set<String> features;
                                if (featureSetFeatureNameMap.containsKey(convertedName)) {
                                    features = featureSetFeatureNameMap.get(convertedName);
                                } else {
                                    features = new HashSet<>();
                                    featureSetFeatureNameMap.put(convertedName, features);
                                }
                                features.addAll(
                                        getPropertySetChildLines(line).parallelStream()
                                                .filter(
                                                        l ->
                                                                l
                                                                        instanceof
                                                                        IfcNamedPropertyLineInterface)
                                                .map(
                                                        childLine ->
                                                                Utils.convertIFCStringToUtf8(
                                                                        ((IfcNamedPropertyLineInterface)
                                                                                        childLine)
                                                                                .getName()))
                                                .collect(toList()));
                            });
            this.dataLinesByClass
                    .getOrDefault(IfcElementQuantityLine.class, Collections.emptyList())
                    .stream()
                    .map(l -> (IfcElementQuantityLine) l)
                    .forEach(
                            line -> {
                                String convertedName = Utils.convertIFCStringToUtf8(line.getName());
                                Set<String> features;
                                if (featureSetFeatureNameMap.containsKey(convertedName)) {
                                    features = featureSetFeatureNameMap.get(convertedName);
                                } else {
                                    features = new HashSet<>();
                                    featureSetFeatureNameMap.put(convertedName, features);
                                }
                                features.addAll(
                                        getElementQuantityChildLines(line).parallelStream()
                                                .filter(
                                                        l ->
                                                                l
                                                                        instanceof
                                                                        IfcNamedPropertyLineInterface)
                                                .map(
                                                        childLine ->
                                                                Utils.convertIFCStringToUtf8(
                                                                        ((IfcNamedPropertyLineInterface)
                                                                                        childLine)
                                                                                .getName()))
                                                .collect(toList()));
                            });
            featureSets =
                    new HashSet<>(); // TODO: FEATURESETS DONT CONTAIN FEATURES YET IMPL: LATER
            featureSets.addAll(
                    this.dataLinesByClass
                            .getOrDefault(IfcPropertySetLine.class, Collections.emptyList())
                            .parallelStream()
                            .map(l -> (IfcPropertySetLine) l)
                            .map(
                                    l ->
                                            StringUtils.isEmpty(l.getDescription())
                                                    ? new FeatureSet(l.getName())
                                                    : new FeatureSet(
                                                            l.getName(), l.getDescription()))
                            .peek(
                                    featureSet ->
                                            featureSet.setFeatures(
                                                    featureSetFeatureNameMap
                                                            .getOrDefault(
                                                                    featureSet.getName(),
                                                                    Collections.emptySet())
                                                            .stream()
                                                            .map(
                                                                    featureName -> {
                                                                        for (Feature f :
                                                                                this.features) {
                                                                            if (featureName.equals(
                                                                                    f.getName())) {
                                                                                return f;
                                                                            }
                                                                        }
                                                                        return null;
                                                                    })
                                                            .filter(Objects::nonNull)
                                                            .collect(toList())))
                            .collect(Collectors.toSet()));
            featureSets.addAll(
                    this.dataLinesByClass
                            .getOrDefault(IfcElementQuantityLine.class, Collections.emptyList())
                            .parallelStream()
                            .map(l -> (IfcElementQuantityLine) l)
                            .map(
                                    l ->
                                            StringUtils.isEmpty(l.getDescription())
                                                    ? new FeatureSet(l.getName())
                                                    : new FeatureSet(
                                                            l.getName(), l.getDescription()))
                            .peek(
                                    featureSet ->
                                            featureSet.setFeatures(
                                                    featureSetFeatureNameMap
                                                            .getOrDefault(
                                                                    featureSet.getName(),
                                                                    Collections.emptySet())
                                                            .stream()
                                                            .map(
                                                                    featureName -> {
                                                                        for (Feature f :
                                                                                this.features) {
                                                                            if (featureName.equals(
                                                                                    f.getName())) {
                                                                                return f;
                                                                            }
                                                                        }
                                                                        return null;
                                                                    })
                                                            .filter(Objects::nonNull)
                                                            .collect(toList())))
                            .collect(Collectors.toSet()));
        } else {
            this.dataLines = Collections.emptyMap();
            this.dataLinesByClass = Collections.emptyMap();
            this.featureSets = Collections.emptySet();
            this.features = Collections.emptyList();
        }
    }

    private Stream<TypeInstancePair> getTypeInstancePairStream(IfcLine line) {
        Set<Class<? extends IfcLine>> classes = new HashSet<>();
        Class<? extends IfcLine> clazz = line.getClass();
        Class<IfcLine> top = IfcLine.class;
        while (top.isAssignableFrom(clazz) && !top.equals(clazz)) {
            classes.add(clazz);
            clazz = (Class<? extends IfcLine>) clazz.getSuperclass();
        }
        return classes.stream().map(c -> new TypeInstancePair(c, line));
    }

    public ParsedIfcFile(ParsedIfcFile toCopy) {
        this.dataLines = new HashMap<>(toCopy.dataLines);
        this.dataLinesByClass = new HashMap<>(toCopy.dataLinesByClass);
        this.extractedProperties = new HashSet<>(toCopy.extractedProperties);
        this.features = new ArrayList<>(toCopy.features);
        this.extractedPropertyMap = new HashMap<>(toCopy.extractedPropertyMap);
        this.featureSets = new HashSet<>(toCopy.featureSets);
        this.lines = new ArrayList<>(toCopy.lines);
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

    public List<Feature> getFeatures() {
        return features;
    }

    public Set<FeatureSet> getFeatureSets() {
        return featureSets;
    }

    public Map<Integer, IfcLine> getDataLines() {
        return dataLines;
    }

    public Map<Class<? extends IfcLine>, List<IfcLine>> getDataLinesByClass() {
        return dataLinesByClass;
    }

    public Feature getFeatureForNamedPropertyLine(IfcNamedPropertyLineInterface ifcLine) {
        String name = ifcLine.getName();
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

    public List<IfcPropertySetLine> getPropertySetsForProperty(IfcLine ifcLine) {
        return getPropertySetLines().parallelStream()
                .filter(
                        entryIfcLine -> {
                            if (Objects.nonNull(entryIfcLine)) {
                                return entryIfcLine.getPropertyIds().contains(ifcLine.getId());
                            }
                            return false;
                        })
                .collect(toList());
    }

    public List<IfcElementQuantityLine> getRelatedElementQuantityLines(IfcLine ifcLine) {
        return getElementQuantityLines().stream()
                .filter(
                        entryIfcLine -> {
                            if (Objects.nonNull(entryIfcLine)) {
                                return entryIfcLine.getPropertyIds().contains(ifcLine.getId());
                            }
                            return false;
                        })
                .collect(toList());
    }

    public List<IfcPropertySetLine> getPropertySetLines() {
        return dataLinesByClass
                .getOrDefault(IfcPropertySetLine.class, Collections.emptyList())
                .stream()
                .map(l -> (IfcPropertySetLine) l)
                .collect(toList());
    }

    public <T> List<T> getDataLinesByClass(Class<T> clazz) {
        return (List<T>) dataLinesByClass.getOrDefault(clazz, Collections.emptyList());
    }

    public List<IfcBuiltElementLine> getBuiltElementLines() {
        return getDataLinesByClass(IfcBuiltElementLine.class);
    }

    public List<IfcElementQuantityLine> getElementQuantityLines() {
        return getDataLinesByClass(IfcElementQuantityLine.class);
    }

    public List<IfcLine> getReferencingLines(IfcLine ifcLine) {
        return dataLines.entrySet().parallelStream()
                .map(Map.Entry::getValue)
                .filter(Objects::nonNull)
                .filter(dataLine -> dataLine.references(ifcLine))
                .collect(toList());
    }

    public List<IfcLine> getReferencedLines(IfcLine ifcLine) {
        return ifcLine.getReferences().parallelStream().map(dataLines::get).collect(toList());
    }

    public List<IfcLine> getRelatedObjectLines(IfcRelDefinesByPropertiesLine ifcLine) {
        return ifcLine.getRelatedObjectIds().parallelStream().map(dataLines::get).collect(toList());
    }

    public List<IfcSinglePropertyValueLine> getPropertySetChildLines(IfcPropertySetLine ifcLine) {
        return Optional.ofNullable(ifcLine)
                .map(
                        l ->
                                l.getPropertyIds().stream()
                                        .map(dataLines::get)
                                        .map(
                                                line ->
                                                        castToOptAndLogFailure(
                                                                line,
                                                                IfcSinglePropertyValueLine.class,
                                                                "get properties of property set"))
                                        .filter(Optional::isPresent)
                                        .map(Optional::get)
                                        .collect(toList()))
                .orElse(Collections.emptyList());
    }

    public List<IfcLine> getElementQuantityChildLines(IfcElementQuantityLine ifcLine) {
        return ifcLine.getPropertyIds().stream().map(dataLines::get).collect(toList());
    }

    public List<IfcRelDefinesByPropertiesLine> getRelDefinesByPropertiesLinesForPSet(
            IfcPropertySetLine ifcLine) {
        return getDataLinesByClass(IfcRelDefinesByPropertiesLine.class).stream()
                .filter(
                        entryIfcLine ->
                                Objects.nonNull(entryIfcLine)
                                        && entryIfcLine.getRelatingPropertySetId()
                                                == ifcLine.getId())
                .collect(toList());
    }

    public List<IfcRelDefinesByPropertiesLine> getRelDefinesByPropertiesLinesReferencing(
            IfcLine ifcLine) {
        return getDataLinesByClass(IfcRelDefinesByPropertiesLine.class).stream()
                .filter(
                        entryIfcLine ->
                                Objects.nonNull(entryIfcLine)
                                        && entryIfcLine
                                                .getRelatedObjectIds()
                                                .contains(ifcLine.getId()))
                .collect(toList());
    }

    public List<IfcRelAssociatesMaterialLine> getRelAsscoiatesMaterialReferencing(
            IfcBuiltElementLine ifcLine) {
        return getDataLinesByClass(IfcRelAssociatesMaterialLine.class).stream()
                .filter(
                        entryIfcLine ->
                                Objects.nonNull(entryIfcLine)
                                        && entryIfcLine
                                                .getRelatedObjectIds()
                                                .contains(ifcLine.getId()))
                .collect(toList());
    }

    public List<IfcRelDefinesByTypeLine> getRelDefinesByTypeReferencing(IfcLine ifcLine) {
        return getDataLinesByClass(IfcRelDefinesByTypeLine.class).stream()
                .filter(
                        entryIfcLine ->
                                Objects.nonNull(entryIfcLine)
                                        && entryIfcLine
                                                .getRelatedObjectIds()
                                                .contains(ifcLine.getId()))
                .collect(toList());
    }

    public void deleteItem(Integer itemId) {
        IfcLine item = this.dataLines.remove(itemId);
        List<IfcLine> referencing = this.getReferencingLines(item);
        referencing.forEach(l -> l.removeReferenceTo(item));
    }
}
