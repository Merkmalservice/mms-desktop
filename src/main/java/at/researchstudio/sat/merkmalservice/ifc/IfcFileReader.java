package at.researchstudio.sat.merkmalservice.ifc;

import static at.researchstudio.sat.merkmalservice.ifc.support.FileUtils.countLines;

import at.researchstudio.sat.merkmalservice.ifc.model.*;
import at.researchstudio.sat.merkmalservice.ifc.parser.IfcLineParser;
import at.researchstudio.sat.merkmalservice.ifc.parser.IfcLineParserImpl;
import at.researchstudio.sat.merkmalservice.ifc.support.IfcPropertyBuilder;
import at.researchstudio.sat.merkmalservice.ifc.support.IfcUtils;
import at.researchstudio.sat.merkmalservice.ifc.support.ProjectUnits;
import at.researchstudio.sat.merkmalservice.model.BooleanFeature;
import at.researchstudio.sat.merkmalservice.model.EnumFeature;
import at.researchstudio.sat.merkmalservice.model.Feature;
import at.researchstudio.sat.merkmalservice.model.StringFeature;
import at.researchstudio.sat.merkmalservice.model.ifc.IfcDerivedUnit;
import at.researchstudio.sat.merkmalservice.model.ifc.IfcProperty;
import at.researchstudio.sat.merkmalservice.model.ifc.IfcSIUnit;
import at.researchstudio.sat.merkmalservice.model.ifc.IfcUnit;
import at.researchstudio.sat.merkmalservice.support.progress.TaskProgressListener;
import at.researchstudio.sat.merkmalservice.vocab.ifc.IfcPropertyType;
import at.researchstudio.sat.merkmalservice.vocab.ifc.IfcUnitType;
import at.researchstudio.sat.merkmalservice.vocab.qudt.QudtQuantityKind;
import at.researchstudio.sat.merkmalservice.vocab.qudt.QudtUnit;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;

public class IfcFileReader {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final Map<String, IfcLineParser<?>> ifcLineParsers;
    public static final int PROGRESS_UPDATES = 200;

    static {
        synchronized (IfcFileReader.class) {
            Set<Class> ifcModelClasses = new HashSet<>();
            try (ScanResult scanResult =
                    new ClassGraph()
                            .enableClassInfo()
                            .acceptPackages("at.researchstudio.sat.merkmalservice.ifc.model")
                            .scan()) { // Start the scan
                scanResult.getSubclasses(IfcLine.class).stream()
                        .map(ClassInfo::loadClass)
                        .forEach(ifcModelClasses::add);
            }
            ifcLineParsers = new HashMap<>();
            for (Class<? extends IfcLine> ifcModelClass : ifcModelClasses) {
                instantiateLineParser(ifcLineParsers, ifcModelClass);
            }
        }
    }

    private static <T extends IfcLine> void instantiateLineParser(
            Map<String, IfcLineParser<? extends IfcLine>> parsers, Class<T> ifcModelClass) {
        try {
            if (Modifier.isAbstract(ifcModelClass.getModifiers())) {
                return;
            }
            String type = (String) ifcModelClass.getDeclaredField("IDENTIFIER").get(ifcModelClass);
            logger.debug(
                    "registering ifc line parser "
                            + ifcModelClass.getSimpleName()
                            + " for identifier "
                            + type);
            parsers.put(
                    type,
                    new IfcLineParserImpl<>(
                            s -> {
                                try {
                                    return ifcModelClass
                                            .getConstructor(String.class)
                                            .newInstance(s);
                                } catch (Exception e) {
                                    logger.info(
                                            "Could not load line parser for type "
                                                    + ifcModelClass.getName(),
                                            e);
                                }
                                return null;
                            }));
        } catch (Exception e) {
            logger.info(
                    "Could not load determine IDENTIFIER for type " + ifcModelClass.getName(), e);
        }
    }

    public static ParsedIfcFile readIfcFile(IfcFileWrapper ifcFile) throws IOException {
        return readIfcFile(ifcFile, null);
    }

    public static ParsedIfcFile readIfcFile(
            IfcFileWrapper ifcFile, TaskProgressListener taskProgressListener) throws IOException {
        long totalLineCount = countLines(ifcFile.getFile().getAbsolutePath());
        List<IfcLine> lines = new ArrayList<>();
        Set<IfcUnit> projectUnits = new HashSet<>();
        int updateIncrement = (int) (totalLineCount / PROGRESS_UPDATES);
        boolean updateProgress = Objects.nonNull(taskProgressListener);
        Pattern lineTypePattern = Pattern.compile("^#\\d+= ([A-Z0-9]+)\\(");
        try (LineIterator it =
                FileUtils.lineIterator(ifcFile.getFile(), StandardCharsets.UTF_8.toString())) {
            StringBuilder sb = new StringBuilder();
            int lineCount = 0;
            while (it.hasNext()) {
                String line = it.nextLine();
                lineCount++;
                float progress = (float) lineCount / (float) totalLineCount * 100;
                if (updateProgress && (lineCount % updateIncrement == 0 || sb.length() > 0)) {
                    taskProgressListener.notifyProgress(null, sb.toString(), progress);
                    sb = new StringBuilder();
                }
                Matcher identifierMatcher = lineTypePattern.matcher(line);
                boolean isIfcLine = identifierMatcher.lookingAt();
                if (!isIfcLine) {
                    handleUnparseableLine(
                            taskProgressListener, lines, updateProgress, progress, line);
                } else {
                    String identifier = identifierMatcher.group(1);
                    IfcLineParser<?> parser = ifcLineParsers.get(identifier);
                    if (parser == null) {
                        if (identifier.contains("IFCQUANTITY")) {
                            sb.append("Couldnt parse Line: ")
                                    .append(line)
                                    .append(" adding it as IfcQuantityLine")
                                    .append(System.lineSeparator());
                            lines.add(new IfcQuantityLine(line));
                        } else {
                            lines.add(new IfcLine(line));
                        }
                    } else {
                        try {
                            lines.add(parser.parse(line));
                        } catch (IllegalArgumentException e) {
                            handleUnparseableLine(
                                    taskProgressListener, lines, updateProgress, progress, line);
                        }
                    }
                }
            }
        }
        Map<Integer, IfcUnit> projectUnitsById = new HashMap<>();
        Map<Class<? extends IfcLine>, List<IfcLine>> ifcLinesGrouped =
                lines.parallelStream().collect(Collectors.groupingBy(IfcLine::getClass));
        int projectUnitLineId = 0;
        for (IfcLine line :
                ifcLinesGrouped.getOrDefault(IfcProjectLine.class, Collections.emptyList())) {
            IfcProjectLine projectLine = (IfcProjectLine) line;
            if (updateProgress) {
                taskProgressListener.notifyProgress(
                        null, "Extracted ProjectLine: " + projectLine, 0);
            }
            projectUnitLineId = projectLine.getUnitAssignmentId();
        }
        if (projectUnitLineId == 0) {
            throw new IOException("File: " + ifcFile.getPath() + " is not a valid ifc step file");
        }
        // collect project default units
        List<Integer> defaultProjectUnitIds = Collections.emptyList();
        for (IfcLine line :
                ifcLinesGrouped.getOrDefault(
                        IfcUnitAssignmentLine.class, Collections.emptyList())) {
            if (line.getId() == projectUnitLineId) {
                defaultProjectUnitIds = ((IfcUnitAssignmentLine) line).getUnitIds();
            }
        }
        if (updateProgress) {
            taskProgressListener.notifyProgress(
                    null,
                    "Extracted " + defaultProjectUnitIds.size() + " IfcProjectUnit Assignments",
                    0);
        }
        // collect SI units
        for (IfcLine line :
                ifcLinesGrouped.getOrDefault(IfcSIUnitLine.class, Collections.emptyList())) {
            IfcSIUnitLine unitLine = (IfcSIUnitLine) line;
            IfcUnit unit =
                    new IfcSIUnit(
                            unitLine.getId(),
                            unitLine.getType(),
                            unitLine.getMeasure(),
                            unitLine.getPrefix(),
                            defaultProjectUnitIds.contains(unitLine.getId()));
            projectUnits.add(unit); // TODO CONTINUE HERE!
            projectUnitsById.put(line.getId(), unit);
        }
        List<IfcUnit> projectSIUnits =
                projectUnits.stream()
                        .filter(unit -> unit instanceof IfcSIUnit)
                        .collect(Collectors.toList());
        // collect derived units
        for (IfcLine line :
                ifcLinesGrouped.getOrDefault(IfcDerivedUnitLine.class, Collections.emptyList())) {
            IfcDerivedUnitLine unitLine = (IfcDerivedUnitLine) line;
            IfcDerivedUnit derivedUnit =
                    new IfcDerivedUnit(
                            unitLine.getId(),
                            unitLine.getType(),
                            unitLine.getName(),
                            defaultProjectUnitIds.contains(unitLine.getId()));
            List<IfcLine> relevantDerivedUnitElements =
                    ifcLinesGrouped
                            .getOrDefault(IfcDerivedUnitElementLine.class, Collections.emptyList())
                            .stream()
                            .filter(l -> unitLine.getUnitElementIds().contains(l.getId()))
                            .collect(Collectors.toList());
            for (IfcLine l : relevantDerivedUnitElements) {
                IfcDerivedUnitElementLine derivedUnitElementLine = (IfcDerivedUnitElementLine) l;
                for (IfcUnit unit : projectSIUnits) {
                    if (unit.getId().equals(derivedUnitElementLine.getUnitIdString())) {
                        derivedUnit.addDerivedUnitElement(
                                (IfcSIUnit) unit, derivedUnitElementLine.getExponent());
                    }
                }
            }
            projectUnits.add(derivedUnit);
            projectUnitsById.put(unitLine.getId(), derivedUnit);
        }
        if (updateProgress) {
            taskProgressListener.notifyProgress(
                    null, "Extracted " + projectUnits.size() + " IfcUnits", 0);
        }
        // map unit type -> unit
        Map<IfcUnitType, List<IfcUnit>> projectUnitsMap =
                projectUnits.stream().collect(Collectors.groupingBy(IfcUnit::getType));
        // extract properties
        Set<IfcProperty> extractedProperties = new HashSet<>();
        for (IfcLine line :
                ifcLinesGrouped.getOrDefault(
                        IfcSinglePropertyValueLine.class, Collections.emptyList())) {
            IfcSinglePropertyValueLine propertyLine = (IfcSinglePropertyValueLine) line;
            IfcProperty tempProp = new IfcPropertyBuilder(propertyLine, projectUnitsMap).build();
            IfcProperty prop =
                    extractedProperties.stream()
                            .filter(tempProp::equals)
                            .findAny()
                            .orElse(tempProp);
            extractedProperties.add(prop);
            if (Objects.nonNull(propertyLine.getValue())) {
                prop.addExtractedValue(propertyLine.getValue());
            }
        }
        for (IfcLine line :
                ifcLinesGrouped.getOrDefault(
                        IfcPropertyEnumeratedValueLine.class, Collections.emptyList())) {
            IfcPropertyEnumeratedValueLine propertyLine = (IfcPropertyEnumeratedValueLine) line;
            IfcPropertyEnumerationLine enumLine = null;
            for (IfcLine el :
                    ifcLinesGrouped.getOrDefault(
                            IfcPropertyEnumerationLine.class, Collections.emptyList())) {
                if (el.getId() == propertyLine.getEnumId()) {
                    enumLine = (IfcPropertyEnumerationLine) el;
                    break;
                }
            }
            IfcProperty tempProp =
                    new IfcProperty(propertyLine.getName(), IfcPropertyType.VALUELIST);
            IfcProperty prop =
                    extractedProperties.stream()
                            .filter(tempProp::equals)
                            .findAny()
                            .orElse(tempProp);
            extractedProperties.add(prop);
            if (Objects.nonNull(propertyLine.getValues())) {
                for (String value : propertyLine.getValues()) {
                    prop.addExtractedValue(value);
                }
            }
            if (Objects.nonNull(enumLine)) {
                for (String enumValue : enumLine.getValues()) {
                    prop.addEnumOptionValue(enumValue);
                }
            }
        }
        for (IfcLine line :
                ifcLinesGrouped.getOrDefault(IfcQuantityCountLine.class, Collections.emptyList())) {
            extractFromIfcQuantityLine(
                    projectUnitsMap, extractedProperties, (IfcQuantityLine) line);
        }
        for (IfcLine line :
                ifcLinesGrouped.getOrDefault(IfcQuantityAreaLine.class, Collections.emptyList())) {
            extractFromIfcQuantityLine(
                    projectUnitsMap, extractedProperties, (IfcQuantityLine) line);
        }
        for (IfcLine line :
                ifcLinesGrouped.getOrDefault(
                        IfcQuantityLengthLine.class, Collections.emptyList())) {
            extractFromIfcQuantityLine(
                    projectUnitsMap, extractedProperties, (IfcQuantityLine) line);
        }
        for (IfcLine line :
                ifcLinesGrouped.getOrDefault(
                        IfcQuantityVolumeLine.class, Collections.emptyList())) {
            extractFromIfcQuantityLine(
                    projectUnitsMap, extractedProperties, (IfcQuantityLine) line);
        }
        if (updateProgress) {
            taskProgressListener.notifyProgress(
                    null, "Extracted " + extractedProperties.size() + " IfcProperties", 0);
        }
        StringBuilder extractLog = new StringBuilder();
        ProjectUnits projectUnitsObject = new ProjectUnits(projectUnitsMap, projectUnitsById);

        ParsedIfcFile parsedIfcFile =
                new ParsedIfcFile(
                        lines, extractedProperties, projectUnitsObject, ifcFile, extractLog);
        if (updateProgress) {
            taskProgressListener.notifyProgress(null, extractLog.toString(), 0);
        }
        return parsedIfcFile;
    }

    private static void handleUnparseableLine(
            TaskProgressListener taskProgressListener,
            List<IfcLine> lines,
            boolean updateProgress,
            float progress,
            String line) {
        if (updateProgress) {
            taskProgressListener.notifyProgress(
                    null, "Couldnt parse Line: " + line + " adding it as IfcLine", progress);
        }
        lines.add(new IfcLine(line));
    }

    private static void extractFromIfcQuantityLine(
            Map<IfcUnitType, List<IfcUnit>> projectUnitsMap,
            Set<IfcProperty> extractedProperties,
            IfcQuantityLine propertyLine) {
        IfcProperty tempProp = new IfcPropertyBuilder(propertyLine, projectUnitsMap).build();
        IfcProperty prop =
                extractedProperties.stream().filter(tempProp::equals).findAny().orElse(tempProp);
        extractedProperties.add(prop);
        if (Objects.nonNull(propertyLine.getValue())) {
            prop.addExtractedValue(String.valueOf(propertyLine.getValue()));
        }
    }

    public static List<Feature> extractFeaturesFromProperties(
            Map<IfcPropertyType, List<IfcProperty>> extractedProperties,
            @NonNull final StringBuilder fullLog) {
        List<Feature> extractedFeatures = new ArrayList<>();
        for (Map.Entry<IfcPropertyType, List<IfcProperty>> entry : extractedProperties.entrySet()) {
            IfcPropertyType ifcPropertyType = entry.getKey();
            String logString = entry.getValue().size() + " " + ifcPropertyType + " Properties";
            switch (ifcPropertyType) {
                case LOGICAL:
                case EXPRESS_BOOL:
                case BOOL:
                    fullLog.append(logString).append(System.getProperty("line.separator"));
                    extractedFeatures.addAll(
                            entry.getValue().stream()
                                    .map(
                                            ifcProperty -> {
                                                BooleanFeature f =
                                                        new BooleanFeature(ifcProperty.getName());
                                                f.setUniqueValues(
                                                        ifcProperty.getExtractedUniqueValues());
                                                f.setDescriptionFromUniqueValues(
                                                        ifcProperty.getExtractedUniqueValues());
                                                return f;
                                            })
                                    .collect(Collectors.toList()));
                    break;
                case IDENTIFIER:
                case TEXT:
                case LABEL:
                    fullLog.append(logString).append(System.getProperty("line.separator"));
                    extractedFeatures.addAll(
                            entry.getValue().stream()
                                    .map(
                                            ifcProperty -> {
                                                StringFeature f =
                                                        new StringFeature(ifcProperty.getName());
                                                f.setUniqueValues(
                                                        ifcProperty.getExtractedUniqueValues());
                                                f.setDescriptionFromUniqueValues(
                                                        ifcProperty.getExtractedUniqueValues());
                                                return f;
                                            })
                                    .collect(Collectors.toList()));
                    break;
                case VOLUME_MEASURE:
                    fullLog.append(logString).append(System.getProperty("line.separator"));
                    extractedFeatures.addAll(
                            entry.getValue().stream()
                                    .map(
                                            ifcProperty ->
                                                    IfcUtils.parseNumericFeature(
                                                            ifcProperty, QudtQuantityKind.VOLUME))
                                    .collect(Collectors.toList()));
                    break;
                case AREA_MEASURE:
                    fullLog.append(logString).append(System.getProperty("line.separator"));
                    extractedFeatures.addAll(
                            entry.getValue().stream()
                                    .map(
                                            ifcProperty ->
                                                    IfcUtils.parseNumericFeature(
                                                            ifcProperty, QudtQuantityKind.AREA))
                                    .collect(Collectors.toList()));
                    break;
                case DIMENSION_COUNT:
                case REAL:
                case EXPRESS_INTEGER:
                case POSITIVE_INTEGER:
                case INTEGER:
                case COUNT_MEASURE:
                    fullLog.append(logString).append(System.getProperty("line.separator"));
                    extractedFeatures.addAll(
                            entry.getValue().stream()
                                    .map(
                                            ifcProperty ->
                                                    IfcUtils.parseNumericFeature(
                                                            ifcProperty,
                                                            QudtQuantityKind.DIMENSIONLESS,
                                                            QudtUnit.UNITLESS))
                                    .collect(Collectors.toList()));
                    break;
                case PLANE_ANGLE_MEASURE:
                    fullLog.append(logString).append(System.getProperty("line.separator"));
                    extractedFeatures.addAll(
                            entry.getValue().stream()
                                    .map(
                                            ifcProperty ->
                                                    IfcUtils.parseNumericFeature(
                                                            ifcProperty, QudtQuantityKind.ANGLE))
                                    .collect(Collectors.toList()));
                    break;
                case THERMAL_TRANSMITTANCE_MEASURE:
                    fullLog.append(logString).append(System.getProperty("line.separator"));
                    extractedFeatures.addAll(
                            entry.getValue().stream()
                                    .map(
                                            ifcProperty ->
                                                    IfcUtils.parseNumericFeature(
                                                            ifcProperty,
                                                            QudtQuantityKind.DIMENSIONLESS))
                                    // TODO: Figure out what THERMAL_TRANSMITTANCE_MEASURE is in
                                    // QUDT.QuantityKind
                                    .collect(Collectors.toList()));
                    break;
                case LUMINOUS_INTESITY_MEASURE:
                    fullLog.append(logString).append(System.getProperty("line.separator"));
                    extractedFeatures.addAll(
                            entry.getValue().stream()
                                    .map(
                                            ifcProperty ->
                                                    IfcUtils.parseNumericFeature(
                                                            ifcProperty,
                                                            QudtQuantityKind.LUMINOUS_INTENSITY))
                                    .collect(Collectors.toList()));
                    break;
                case ELECTRIC_CURRENT_MEASURE:
                    fullLog.append(logString).append(System.getProperty("line.separator"));
                    extractedFeatures.addAll(
                            entry.getValue().stream()
                                    .map(
                                            ifcProperty ->
                                                    IfcUtils.parseNumericFeature(
                                                            ifcProperty,
                                                            QudtQuantityKind.ELECTRIC_CURRENT))
                                    .collect(Collectors.toList()));
                    break;
                case MASS_DENSITY_MEASURE:
                    fullLog.append(logString).append(System.getProperty("line.separator"));
                    extractedFeatures.addAll(
                            entry.getValue().stream()
                                    .map(
                                            ifcProperty ->
                                                    IfcUtils.parseNumericFeature(
                                                            ifcProperty,
                                                            QudtQuantityKind.MASS_DENSITY))
                                    .collect(Collectors.toList()));
                    break;
                case ILLUMINANCE_MEASURE:
                    fullLog.append(logString).append(System.getProperty("line.separator"));
                    extractedFeatures.addAll(
                            entry.getValue().stream()
                                    .map(
                                            ifcProperty ->
                                                    IfcUtils.parseNumericFeature(
                                                            ifcProperty,
                                                            QudtQuantityKind.ILLUMINANCE))
                                    .collect(Collectors.toList()));
                    break;
                case PLANAR_FORCE_MEASURE:
                    fullLog.append(logString).append(System.getProperty("line.separator"));
                    extractedFeatures.addAll(
                            entry.getValue().stream()
                                    .map(
                                            ifcProperty ->
                                                    IfcUtils.parseNumericFeature(
                                                            ifcProperty,
                                                            QudtQuantityKind.DIMENSIONLESS))
                                    // TODO: Figure out what PLANAR_FORCE_MEASURE is in
                                    // QUDT.QuantityKind
                                    .collect(Collectors.toList()));
                    break;
                case FORCE_MEASURE:
                    fullLog.append(logString).append(System.getProperty("line.separator"));
                    extractedFeatures.addAll(
                            entry.getValue().stream()
                                    .map(
                                            ifcProperty ->
                                                    IfcUtils.parseNumericFeature(
                                                            ifcProperty, QudtQuantityKind.FORCE))
                                    .collect(Collectors.toList()));
                    break;
                case MOMENT_OF_INERTIA_MEASURE:
                    fullLog.append(logString).append(System.getProperty("line.separator"));
                    extractedFeatures.addAll(
                            entry.getValue().stream()
                                    .map(
                                            ifcProperty ->
                                                    IfcUtils.parseNumericFeature(
                                                            ifcProperty,
                                                            QudtQuantityKind.MOMENT_OF_INERTIA))
                                    .collect(Collectors.toList()));
                    break;
                case THERMODYNAMIC_TEMPERATURE_MEASURE:
                    fullLog.append(logString).append(System.getProperty("line.separator"));
                    extractedFeatures.addAll(
                            entry.getValue().stream()
                                    .map(
                                            ifcProperty ->
                                                    IfcUtils.parseNumericFeature(
                                                            ifcProperty,
                                                            QudtQuantityKind
                                                                    .THERMODYNAMIC_TEMPERATURE))
                                    .collect(Collectors.toList()));
                    break;
                case MASS_MEASURE:
                    fullLog.append(logString).append(System.getProperty("line.separator"));
                    extractedFeatures.addAll(
                            entry.getValue().stream()
                                    .map(
                                            ifcProperty ->
                                                    IfcUtils.parseNumericFeature(
                                                            ifcProperty, QudtQuantityKind.MASS))
                                    .collect(Collectors.toList()));
                    break;
                case PRESSURE_MEASURE:
                    fullLog.append(logString).append(System.getProperty("line.separator"));
                    extractedFeatures.addAll(
                            entry.getValue().stream()
                                    .map(
                                            ifcProperty ->
                                                    IfcUtils.parseNumericFeature(
                                                            ifcProperty, QudtQuantityKind.PRESSURE))
                                    .collect(Collectors.toList()));
                    break;
                case LUMINOUS_FLUX_MEASURE:
                    fullLog.append(logString).append(System.getProperty("line.separator"));
                    extractedFeatures.addAll(
                            entry.getValue().stream()
                                    .map(
                                            ifcProperty ->
                                                    IfcUtils.parseNumericFeature(
                                                            ifcProperty,
                                                            QudtQuantityKind.LUMINOUS_FLUX))
                                    .collect(Collectors.toList()));
                    break;
                case VOLUMETRIC_FLOW_RATE_MEASURE:
                    fullLog.append(logString).append(System.getProperty("line.separator"));
                    extractedFeatures.addAll(
                            entry.getValue().stream()
                                    .map(
                                            ifcProperty ->
                                                    IfcUtils.parseNumericFeature(
                                                            ifcProperty,
                                                            QudtQuantityKind.VOLUME_FLOW_RATE))
                                    .collect(Collectors.toList()));
                    break;
                case LINEAR_FORCE_MEASURE:
                    fullLog.append(logString).append(System.getProperty("line.separator"));
                    extractedFeatures.addAll(
                            entry.getValue().stream()
                                    .map(
                                            ifcProperty ->
                                                    IfcUtils.parseNumericFeature(
                                                            ifcProperty,
                                                            QudtQuantityKind.DIMENSIONLESS))
                                    // TODO: Figure out what LINEAR_FORCE_MEASURE is in
                                    // QUDT.QuantityKind
                                    .collect(Collectors.toList()));
                    break;
                case POWER_MEASURE:
                    fullLog.append(logString).append(System.getProperty("line.separator"));
                    extractedFeatures.addAll(
                            entry.getValue().stream()
                                    .map(
                                            ifcProperty ->
                                                    IfcUtils.parseNumericFeature(
                                                            ifcProperty, QudtQuantityKind.POWER))
                                    .collect(Collectors.toList()));
                    break;
                case LENGTH_MEASURE:
                case POSITIVE_LENGTH_MEASURE:
                    fullLog.append(logString).append(System.getProperty("line.separator"));
                    extractedFeatures.addAll(
                            entry.getValue().stream()
                                    .map(
                                            f ->
                                                    IfcUtils.parseNumericFeature(
                                                            f, QudtQuantityKind.LENGTH))
                                    .collect(Collectors.toList()));
                    break;
                case VALUELIST:
                    fullLog.append(logString).append(System.getProperty("line.separator"));
                    extractedFeatures.addAll(
                            entry.getValue().stream()
                                    .map(
                                            ifcProperty -> {
                                                EnumFeature f =
                                                        new EnumFeature(
                                                                ifcProperty.getName(),
                                                                ifcProperty
                                                                        .getEnumOptionValues()
                                                                        .stream()
                                                                        .map(
                                                                                option ->
                                                                                        new EnumFeature
                                                                                                .MEStringValue(
                                                                                                option,
                                                                                                null))
                                                                        .collect(
                                                                                Collectors
                                                                                        .toList()),
                                                                false);
                                                f.setUniqueValues(
                                                        ifcProperty.getExtractedUniqueValues());
                                                f.setDescriptionFromUniqueValues(
                                                        ifcProperty.getExtractedUniqueValues());
                                                return f;
                                            })
                                    .collect(Collectors.toList()));
                    break;
                default:
                    fullLog.append(logString)
                            .append(
                                    ", will be ignored, no matching Feature-Type determined yet for:")
                            .append(System.getProperty("line.separator"));
                    entry.getValue()
                            .forEach(
                                    property ->
                                            fullLog.append(property.toString())
                                                    .append(System.getProperty("line.separator")));
                    fullLog.append(
                                    "-------------------------------------------------------------------------")
                            .append(System.getProperty("line.separator"));
                    break;
            }
        }
        return extractedFeatures;
    }
}
