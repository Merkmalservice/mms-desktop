package at.researchstudio.sat.merkmalservice.ifc;

import static at.researchstudio.sat.merkmalservice.ifc.model.TypeConverter.castTo;
import static at.researchstudio.sat.merkmalservice.ifc.model.TypeConverter.castToOptAndLogFailure;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.*;

import at.researchstudio.sat.merkmalservice.ifc.convert.support.propertyvalue.PropertyConverter;
import at.researchstudio.sat.merkmalservice.ifc.convert.support.propertyvalue.StepPropertyValueFactory;
import at.researchstudio.sat.merkmalservice.ifc.convert.support.propertyvalue.StepValueAndType;
import at.researchstudio.sat.merkmalservice.ifc.convert.support.propertyvalue.StepValueAndTypeAndIfcUnit;
import at.researchstudio.sat.merkmalservice.ifc.convert.support.unit.QudtUnitConverter;
import at.researchstudio.sat.merkmalservice.ifc.model.*;
import at.researchstudio.sat.merkmalservice.ifc.model.element.IfcBuiltElementLine;
import at.researchstudio.sat.merkmalservice.ifc.model.type.IfcTypeObjectLine;
import at.researchstudio.sat.merkmalservice.ifc.support.IfcLinePredicates;
import at.researchstudio.sat.merkmalservice.ifc.support.IfcPropertyBuilder;
import at.researchstudio.sat.merkmalservice.ifc.support.ProjectUnits;
import at.researchstudio.sat.merkmalservice.model.Feature;
import at.researchstudio.sat.merkmalservice.model.ifc.*;
import at.researchstudio.sat.merkmalservice.model.mapping.MappingExecutionValue;
import at.researchstudio.sat.merkmalservice.support.exception.IfcPropertyCardinalityException;
import at.researchstudio.sat.merkmalservice.support.exception.IfcPropertyTypeUnsupportedException;
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
    private final IfcFileWrapper ifcFileWrapper;
    private final List<IfcLine> lines;
    private final Map<Integer, IfcLine> dataLines;
    private final Map<Class<? extends IfcLine>, List<IfcLine>> dataLinesByClass;
    private final Set<IfcProperty> extractedProperties;
    private final Map<IfcPropertyType, List<IfcProperty>> extractedPropertyMap;
    private final List<Feature> features;
    private final Set<FeatureSet> featureSets;
    private final Map<Integer, List<Integer>> reverseLookupRelDefinesByProperties;
    private final Map<Integer, List<Integer>> reverseLookupReferencingLines;
    private final StepPropertyValueFactory stepPropertyValueFactory;
    private final ProjectUnits projectUnits;
    private final AtomicInteger nextFreeLineId = new AtomicInteger(1);

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
                Stream.concat(
                                getRelDefinesByPropertiesLinesReferencing(element).stream()
                                        .map(this::getPropertySet)
                                        .filter(Optional::isPresent)
                                        .flatMap(ps -> getPropertySetChildLines(ps.get()).stream())
                                        .filter(predicate),
                                getRelDefinesByPropertiesLinesReferencing(element).stream()
                                        .map(this::getElementQuantity)
                                        .filter(Optional::isPresent)
                                        .flatMap(
                                                ps ->
                                                        getElementQuantityChildLines(ps.get())
                                                                .stream())
                                        .filter(predicate))
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

    public <T extends IfcLine> void addProperty(
            T element,
            at.researchstudio.sat.merkmalservice.model.mapping.feature.Feature feature,
            MappingExecutionValue value,
            String propertySetName) {
        IfcPropertySetLine pSet = getOrSplitOrCreatePropertySet(element, propertySetName);
        addProperty(pSet, feature, value);
    }

    private <T extends IfcLine> IfcPropertySetLine getOrSplitOrCreatePropertySet(
            T element, String propertySetName) {
        splitSharedPropertySetsMatching(element, pset -> propertySetName.equals(pset.getName()));
        List<IfcPropertySetLine> propSets =
                getRelDefinesByPropertiesLinesReferencing(element).stream()
                        .filter(not(IfcRelDefinesByPropertiesLine::isSharedPropertySet))
                        .map(this::getPropertySet)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .filter(pset -> propertySetName.equals(pset.getName()))
                        .collect(toList());
        IfcPropertySetLine pSet = null;
        if (propSets.isEmpty()) {
            pSet = addPropertySet(propertySetName, element);
        } else {
            pSet = propSets.get(0);
        }
        return pSet;
    }

    private void addProperty(
            IfcPropertySetLine pSet,
            at.researchstudio.sat.merkmalservice.model.mapping.feature.Feature feature,
            MappingExecutionValue value) {
        StepValueAndType vat = stepPropertyValueFactory.toStepPropertyValue(feature, value);
        addProperty(pSet, feature, vat);
    }

    private void addProperty(
            IfcPropertySetLine pSet,
            at.researchstudio.sat.merkmalservice.model.mapping.feature.Feature feature,
            StepValueAndType vat) {
        IfcSinglePropertyValueLine prop =
                new IfcSinglePropertyValueLine(
                        nextFreeLineId(),
                        feature.getName(),
                        feature.getDescription(),
                        vat.getType(),
                        vat.getValue(),
                        getUnitId(feature));
        registerNewIfcLine(prop, false);
        removeFromLookupTables(pSet);
        pSet.addPropertyId(prop.getId());
        addToLookupTables(pSet);
        addToLookupTables(prop);
    }

    private void addProperty(
            IfcPropertySetLine pSet,
            at.researchstudio.sat.merkmalservice.model.mapping.feature.Feature feature,
            StepValueAndTypeAndIfcUnit valueAndTypeAndIfcUnit) {
        IfcSinglePropertyValueLine prop =
                new IfcSinglePropertyValueLine(
                        nextFreeLineId(),
                        feature.getName(),
                        feature.getDescription(),
                        valueAndTypeAndIfcUnit.getStepValueAndType().getType(),
                        valueAndTypeAndIfcUnit.getStepValueAndType().getValue(),
                        valueAndTypeAndIfcUnit.getIfcUnit() == null
                                ? null
                                : valueAndTypeAndIfcUnit.getIfcUnit().getId());
        registerNewIfcLine(prop, false);
        removeFromLookupTables(pSet);
        pSet.addPropertyId(prop.getId());
        addToLookupTables(pSet);
        addToLookupTables(prop);
    }

    private Integer getUnitId(
            at.researchstudio.sat.merkmalservice.model.mapping.feature.Feature feature) {
        logger.info("TODO: implement getUnitId");
        return null;
    }

    public <T extends IfcLine> void removeProperty(T element, Predicate<IfcLine> predicate) {
        splitSharedPropertySetsWithPropertyMatching(element, predicate);
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

    public <T extends IfcLine> void convertProperty(
            T element,
            at.researchstudio.sat.merkmalservice.model.mapping.feature.Feature inputFeature,
            at.researchstudio.sat.merkmalservice.model.mapping.feature.Feature outputFeature,
            String propertySetName,
            PropertyConverter converter,
            boolean deleteInputProperty) {
        List<? extends IfcLine> props =
                getProperties(
                        element,
                        IfcLinePredicates.isPropertyWithName(inputFeature.getName())
                                .or(IfcLinePredicates.isQuantityWithName(inputFeature.getName())));
        if (props.size() > 1 || props.isEmpty()) {
            throwCardinalityException(inputFeature, props);
        }
        IfcLine prop = props.get(0);
        StepValueAndTypeAndIfcUnit stepValueAndTypeAndIfcUnit = null;
        if (prop instanceof IfcSinglePropertyValueLine) {
            String propType = prop.getType();
            String propValue = ((IfcSinglePropertyValueLine) prop).getValue();
            IfcProperty ifcProperty =
                    new IfcPropertyBuilder(
                                    (IfcSinglePropertyValueLine) prop,
                                    projectUnits.getUnitsByUnitType())
                            .build();
            stepValueAndTypeAndIfcUnit =
                    new StepValueAndTypeAndIfcUnit(
                            StepValueAndType.fromStingValue(propValue, propType),
                            ifcProperty.getUnit());
        } else if (prop instanceof IfcQuantityLine) {
            String propType = prop.getType();
            Double propValue = ((IfcQuantityLine) prop).getValue();
            IfcProperty ifcProperty =
                    new IfcPropertyBuilder(
                                    (IfcQuantityLine) prop, projectUnits.getUnitsByUnitType())
                            .build();
            stepValueAndTypeAndIfcUnit =
                    new StepValueAndTypeAndIfcUnit(
                            new StepValueAndType(propValue, propType), ifcProperty.getUnit());
        } else {
            // TODO: obtain value and type from enums, lists, tables, etc.
            throw new IfcPropertyTypeUnsupportedException(
                    String.format(
                            "Cannot handle property of type %s in convert action",
                            prop.getClass().getSimpleName()));
        }
        if (propertySetName == null) {
            // TODO: we need a better solution: find out the actual property set name of the source
            // prop
            // and use that, or use a class-specific one.
            propertySetName = "CONVERTED";
        }
        IfcPropertySetLine targetPSet = getOrSplitOrCreatePropertySet(element, propertySetName);

        StepValueAndTypeAndIfcUnit convertedValue =
                converter.convert(stepValueAndTypeAndIfcUnit, this);
        if (deleteInputProperty) {
            removeProperty(element, IfcLinePredicates.isPropertyWithName(inputFeature.getName()));
        }
        addProperty(targetPSet, outputFeature, convertedValue);
    }

    private void throwCardinalityException(
            at.researchstudio.sat.merkmalservice.model.mapping.feature.Feature inputFeature,
            List<? extends IfcLine> props) {
        throw new IfcPropertyCardinalityException(
                String.format(
                        "Expected to find one property named' %s' as the input feature of a convert action, but found %d",
                        inputFeature.getName(), props.size()));
    }

    private <T extends IfcLine> void splitSharedPropertySetsWithPropertyMatching(
            T element, Predicate<IfcLine> predicate) {
        getRelDefinesByPropertiesLinesReferencing(element).stream()
                .filter(IfcRelDefinesByPropertiesLine::isSharedPropertySet)
                .filter(
                        (IfcRelDefinesByPropertiesLine psRel) -> {
                            Optional<IfcPropertySetLine> ps = getPropertySet(psRel);
                            return ps.map(
                                            pset ->
                                                    getPropertySetChildLines(pset).stream()
                                                            .anyMatch(predicate))
                                    .orElse(false);
                        })
                .filter(IfcRelDefinesByPropertiesLine::isSharedPropertySet)
                .forEach(ps -> splitSharedPropertySet(element, ps));
    }

    private <T extends IfcLine> void splitSharedPropertySetsMatching(
            T element, Predicate<IfcPropertySetLine> predicate) {
        getRelDefinesByPropertiesLinesReferencing(element).stream()
                .filter(IfcRelDefinesByPropertiesLine::isSharedPropertySet)
                .filter(
                        (IfcRelDefinesByPropertiesLine psRel) ->
                                getPropertySet(psRel).map(predicate::test).orElse(false))
                .forEach(ps -> splitSharedPropertySet(element, ps));
    }

    private <T extends IfcLine> IfcPropertySetLine addPropertySet(String name, T toElement) {
        Integer id = nextFreeLineId();
        IfcPropertySetLine pSet =
                new IfcPropertySetLine(id, name, UUID.randomUUID().toString(), 10101, null, null);
        registerNewIfcLine(pSet, false);
        IfcRelDefinesByPropertiesLine rel =
                new IfcRelDefinesByPropertiesLine(
                        nextFreeLineId(), pSet.getId(), toElement.getId());
        registerNewIfcLine(rel);
        addToLookupTables(rel);
        addToLookupTables(pSet);
        return pSet;
    }

    private <T extends IfcLine> void splitSharedPropertySet(
            T element, IfcRelDefinesByPropertiesLine relDefByProps) {
        IfcPropertySetLine pSet =
                castTo(
                        dataLines.get(relDefByProps.getRelatingPropertySetId()),
                        IfcPropertySetLine.class);
        IfcPropertySetLine newPSet =
                new IfcPropertySetLine(
                        -1,
                        pSet.getName(),
                        UUID.randomUUID().toString(),
                        pSet.getHistoryId(),
                        pSet.getDescription(),
                        pSet.getPropertyIds());
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

    public Integer addIfcUnit(IfcUnit ifcUnit) {
        if (ifcUnit instanceof IfcDerivedUnit) {
            IfcDerivedUnit ifcDerivedUnit = (IfcDerivedUnit) ifcUnit;
            List<Integer> derivedUnitElementIds = new ArrayList<>();
            for (IfcDerivedUnitElement derivedUnitElement :
                    ifcDerivedUnit.getDerivedUnitElements()) {
                Integer unitId = addIfcUnit(derivedUnitElement.getIfcUnit());
                IfcDerivedUnitElementLine ifcDerivedUnitElementLine =
                        new IfcDerivedUnitElementLine(-1, unitId, derivedUnitElement.getExponent());
                registerNewIfcLine(ifcDerivedUnitElementLine);
                Integer derivedUnitElementId = ifcDerivedUnitElementLine.getId();
                derivedUnitElementIds.add(derivedUnitElementId);
            }
            IfcDerivedUnitLine ifcDerivedUnitLine =
                    new IfcDerivedUnitLine(
                            -1,
                            derivedUnitElementIds,
                            ifcDerivedUnit.getType().toString(),
                            ifcDerivedUnit.getUserDefinedLabel());
            registerNewIfcLine(ifcDerivedUnitLine);
            ifcUnit.setId(ifcDerivedUnitLine.getId());
            return ifcDerivedUnitLine.getId();
        } else if (ifcUnit instanceof IfcSIUnit) {
            IfcSIUnit ifcSiUnit = (IfcSIUnit) ifcUnit;
            IfcSIUnitLine ifcSiUnitLine =
                    new IfcSIUnitLine(
                            -1,
                            ifcSiUnit.getType().toString(),
                            ifcSiUnit.getMeasure().toString(),
                            ifcSiUnit.getPrefix().toString());
            registerNewIfcLine(ifcSiUnitLine);
            ifcUnit.setId(ifcSiUnitLine.getId());
            return ifcSiUnitLine.getId();
        } else {
            throw new UnsupportedOperationException("TODO: implement add IfcUnit to ifc model");
        }
    }

    private Integer registerNewIfcLine(IfcLine newLine) {
        return registerNewIfcLine(newLine, true);
    }

    private Integer registerNewIfcLine(IfcLine newLine, boolean changeIdToNextFreeId) {
        if (changeIdToNextFreeId) {
            newLine.changeIdTo(nextFreeLineId());
        }
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
        return newLine.getId();
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
                    List<Integer> refs = new ArrayList<>(existingRefs);
                    refs.add(newLine.getId());
                    this.reverseLookupRelDefinesByProperties.put(objectId, refs);
                }
            }
        }
        for (Integer objectId : newLine.getReferences()) {
            List<Integer> existingRefs =
                    this.reverseLookupReferencingLines.putIfAbsent(
                            objectId, new ArrayList<>(List.of(newLine.getId())));
            if (existingRefs != null) {
                List<Integer> refs = new ArrayList<>(existingRefs);
                refs.add(newLine.getId());
                this.reverseLookupReferencingLines.put(objectId, refs);
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
        return TypeConverter.castToOpt(related, IfcPropertySetLine.class);
    }

    public Optional<IfcElementQuantityLine> getElementQuantity(
            IfcRelDefinesByPropertiesLine propertySetRel) {
        IfcLine related = getDataLines().get(propertySetRel.getRelatingPropertySetId());
        return TypeConverter.castToOpt(related, IfcElementQuantityLine.class);
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
            ProjectUnits projectUnits,
            IfcFileWrapper ifcFileWrapper,
            StringBuilder extractLog) {
        this.ifcFileWrapper = ifcFileWrapper;
        this.lines = lines;
        this.extractedProperties = extractedProperties;
        this.projectUnits = projectUnits;
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
            stepPropertyValueFactory =
                    new StepPropertyValueFactory(this, new QudtUnitConverter(projectUnits));
        } else {
            this.dataLines = Collections.emptyMap();
            this.dataLinesByClass = Collections.emptyMap();
            this.featureSets = Collections.emptySet();
            this.features = Collections.emptyList();
            this.reverseLookupRelDefinesByProperties = Collections.emptyMap();
            this.reverseLookupReferencingLines = Collections.emptyMap();
            stepPropertyValueFactory =
                    new StepPropertyValueFactory(this, new QudtUnitConverter(projectUnits));
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
        this.ifcFileWrapper = toCopy.ifcFileWrapper;
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
        this.projectUnits = new ProjectUnits(toCopy.projectUnits);
        this.stepPropertyValueFactory =
                new StepPropertyValueFactory(this, new QudtUnitConverter(this.projectUnits));
        this.markLineIdUsed(toCopy.nextFreeLineId());
    }

    public IfcFileWrapper getIfcFileWrapper() {
        return ifcFileWrapper;
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

    public ProjectUnits getProjectUnits() {
        return projectUnits;
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
