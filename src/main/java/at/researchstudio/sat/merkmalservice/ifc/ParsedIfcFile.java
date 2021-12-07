package at.researchstudio.sat.merkmalservice.ifc;

import static at.researchstudio.sat.merkmalservice.ifc.model.TypeConverter.castTo;
import static at.researchstudio.sat.merkmalservice.ifc.model.TypeConverter.castToOptAndLogFailure;
import static java.util.function.Predicate.not;
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
import java.util.concurrent.atomic.AtomicInteger;
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
    private final Map<Integer, List<Integer>> reverseLookupRelDefinesByProperties;
    private final Map<Integer, List<Integer>> reverseLookupReferencingLines;
    private AtomicInteger nextFreeLineId = new AtomicInteger(1);

    private Integer nextFreeLineId() {
        return nextFreeLineId.getAndIncrement();
    }

    /*
     * Sets the nextFreeLineId to the specified <code>usedId + 1<code> if that value is higher than its current value.
     */
    private void markLineIdUsed(Integer usedId) {
        nextFreeLineId.accumulateAndGet(
                usedId,
                (currentValue, candidateValue) -> Math.max(candidateValue + 1, currentValue));
    }

    public <T extends IfcLine> List<? extends IfcLine> getProperties(
            T element, Predicate<IfcLine> predicate) {
        List<IfcLine> properties =
                getRelDefinesByPropertiesLinesReferencing(element).stream()
                        .map(this::getPropertySet)
                        .filter(Optional::isPresent)
                        .flatMap(ps -> getPropertySetChildLines(ps.get()).stream())
                        .filter(predicate)
                        .collect(toList());
        return properties;
    }

    public <T extends IfcLine> List<? extends IfcLine> getPropertiesViaType(
            T element, Predicate<IfcLine> predicate) {
        List<IfcLine> properties =
                getRelDefinesByTypeReferencing(element).stream()
                        .flatMap(rel -> getPropertySets(rel).stream())
                        .flatMap(ps -> getPropertySetChildLines(ps).stream())
                        .filter(predicate)
                        .collect(toList());
        return properties;
    }

    public <T extends IfcLine> void removeProperty(T element, Predicate<IfcLine> predicate) {
        getRelDefinesByPropertiesLinesReferencing(element).stream()
                .filter(IfcRelDefinesByPropertiesLine::isSharedPropertySet)
                .forEach(ps -> splitSharedPropertySet(element, ps, predicate));
        List<Pair<IfcPropertySetLine, IfcSinglePropertyValueLine>> toDelete =
                getRelDefinesByPropertiesLinesReferencing(element).stream()
                        .filter(not(IfcRelDefinesByPropertiesLine::isSharedPropertySet))
                        .map(this::getPropertySet)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .flatMap(
                                ps ->
                                        getPropertySetChildLines(ps).stream()
                                                .filter(predicate)
                                                .map(prop -> Pair.of(ps, prop)))
                        .collect(Collectors.toList());
        toDelete.forEach(
                pSetAndProp ->
                        removeSinglePropertyFromPropertySet(
                                pSetAndProp.getLeft(), pSetAndProp.getRight()));
    }

    private <T extends IfcLine> void splitSharedPropertySet(
            T element, IfcRelDefinesByPropertiesLine relDefByProps, Predicate<IfcLine> predicate) {
        IfcPropertySetLine pSet =
                castTo(
                        dataLines.get(relDefByProps.getRelatingPropertySetId()),
                        IfcPropertySetLine.class);
        IfcPropertySetLine newPSet = new IfcPropertySetLine(pSet.getModifiedLine());
        registerNewIfcLine(newPSet);
        removeFromLookupTables(relDefByProps);
        relDefByProps.removeReferenceTo(element.getId());
        addToLookupTables(relDefByProps);
        IfcRelDefinesByPropertiesLine newRelDefByProps =
                new IfcRelDefinesByPropertiesLine(relDefByProps.getLine());
        List<Integer> toRemove =
                newRelDefByProps.getRelatedObjectIds().stream()
                        .filter(id -> id != element.getId())
                        .collect(toList());
        toRemove.forEach(newRelDefByProps::removeReferenceTo);
        newRelDefByProps.changeRelatingPropertySetIdTo(newPSet.getId());
        registerNewIfcLine(newRelDefByProps);
    }

    private void registerNewIfcLine(IfcLine newLine) {
        newLine.changeIdTo(nextFreeLineId());
        if (dataLines.put(newLine.getId(), newLine) != null) {
            throw new IllegalStateException(
                    String.format(
                            "Tried to use line id %d for new line %s but that Id is not available",
                            newLine.getId(), newLine.getModifiedLine()));
        }
        int lineNo = lines.size() - 1;
        while (lineNo > 0 && !lines.get(lineNo).getModifiedLine().startsWith("#")) {
            lineNo--;
        }
        lines.add(lineNo + 1, newLine);
        addToLookupTables(newLine);
    }

    private void removeFromLookupTables(IfcLine newLine) {
        if (newLine instanceof IfcRelDefinesByPropertiesLine) {
            for (Integer objectId :
                    ((IfcRelDefinesByPropertiesLine) newLine).getRelatedObjectIds()) {
                List<Integer> existingRefs = this.reverseLookupRelDefinesByProperties.get(objectId);
                if (existingRefs != null) {
                    existingRefs.remove((Object) newLine.getId());
                }
            }
        }
        for (Integer objectId : newLine.getReferences()) {
            List<Integer> existingRefs = this.reverseLookupReferencingLines.get(objectId);
            if (existingRefs != null) {
                existingRefs.remove((Object) newLine.getId());
            }
        }
    }

    private void addToLookupTables(IfcLine newLine) {
        if (newLine instanceof IfcRelDefinesByPropertiesLine) {
            for (Integer objectId :
                    ((IfcRelDefinesByPropertiesLine) newLine).getRelatedObjectIds()) {
                List<Integer> existingRefs =
                        this.reverseLookupRelDefinesByProperties.putIfAbsent(
                                objectId, List.of(newLine.getId()));
                if (existingRefs != null) {
                    existingRefs.add(newLine.getId());
                }
            }
        }
        for (Integer objectId : newLine.getReferences()) {
            List<Integer> existingRefs =
                    this.reverseLookupReferencingLines.putIfAbsent(
                            objectId, List.of(newLine.getId()));
            if (existingRefs != null) {
                existingRefs.add(newLine.getId());
            }
        }
    }

    public <T extends IfcLine> void removePropertyViaType(T element, Predicate<IfcLine> predicate) {
        List<Pair<IfcPropertySetLine, IfcSinglePropertyValueLine>> toDelete =
                getRelDefinesByTypeReferencing(element).stream()
                        .flatMap(rel -> getPropertySets(rel).stream())
                        .flatMap(
                                ps ->
                                        getPropertySetChildLines(ps).stream()
                                                .filter(predicate)
                                                .map(prop -> Pair.of(ps, prop)))
                        .collect(toList());
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
        toDelete.forEach(this::removeLine);
    }

    private void removeSinglePropertyFromPropertySet(
            IfcPropertySetLine propertySetLine, IfcSinglePropertyValueLine propertyValueLine) {
        removeFromLookupTables(propertySetLine);
        removeFromLookupTables(propertyValueLine);
        propertySetLine.removeReferenceTo(propertyValueLine);
        if (propertySetLine.getPropertyIds().isEmpty()) {
            removeLine(propertySetLine);
        } else {
            addToLookupTables(propertySetLine);
        }
        if (getReferencingLines(propertyValueLine).isEmpty()) {
            removeLine(propertyValueLine);
        } else {
            addToLookupTables(propertyValueLine);
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
        cascadingDeletes.forEach(this::removeLine);
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
                                                .filter(Objects::nonNull)
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
            this.reverseLookupRelDefinesByProperties =
                    this.dataLinesByClass.get(IfcRelDefinesByPropertiesLine.class).parallelStream()
                            .map(line -> (IfcRelDefinesByPropertiesLine) line)
                            .flatMap(
                                    line ->
                                            line.getRelatedObjectIds().stream()
                                                    .map(o -> Map.entry(o, line.getId())))
                            .collect(
                                    groupingBy(
                                            e -> e.getKey(), mapping(e -> e.getValue(), toList())));
            this.reverseLookupReferencingLines =
                    this.dataLines.values().parallelStream()
                            .flatMap(
                                    line ->
                                            line.getReferences().stream()
                                                    .map(ref -> Map.entry(ref, line.getId())))
                            .collect(
                                    groupingBy(
                                            e -> e.getKey(), mapping(e -> e.getValue(), toList())));
            if (!dataLines.isEmpty()) {
                this.markLineIdUsed(dataLines.keySet().stream().max(Integer::compareTo).get());
            }
        } else {
            this.dataLines = Collections.emptyMap();
            this.dataLinesByClass = Collections.emptyMap();
            this.featureSets = Collections.emptySet();
            this.features = Collections.emptyList();
            this.reverseLookupRelDefinesByProperties = Collections.emptyMap();
            this.reverseLookupReferencingLines = Collections.emptyMap();
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
        this.reverseLookupRelDefinesByProperties =
                new HashMap<>(toCopy.reverseLookupRelDefinesByProperties);
        this.reverseLookupReferencingLines = new HashMap<>(toCopy.reverseLookupReferencingLines);
        this.markLineIdUsed(toCopy.nextFreeLineId());
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
        return reverseLookupReferencingLines
                .getOrDefault(ifcLine.getId(), Collections.emptyList())
                .stream()
                .map(dataLines::get)
                .filter(Objects::nonNull)
                // lazy check required because we may have deleted the reference
                .filter(ref -> ref.references(ifcLine.getId()))
                .collect(Collectors.toList());
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
                                l.getPropertyIds().parallelStream()
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
        List<Integer> lookup =
                this.reverseLookupRelDefinesByProperties.getOrDefault(
                        ifcLine.getId(), Collections.emptyList());
        return lookup.stream()
                .map(dataLines::get)
                .filter(Objects::nonNull)
                .map(l -> (IfcRelDefinesByPropertiesLine) l)
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
