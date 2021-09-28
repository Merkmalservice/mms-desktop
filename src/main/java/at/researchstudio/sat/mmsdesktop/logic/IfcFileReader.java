package at.researchstudio.sat.mmsdesktop.logic;

import at.researchstudio.sat.merkmalservice.model.BooleanFeature;
import at.researchstudio.sat.merkmalservice.model.EnumFeature;
import at.researchstudio.sat.merkmalservice.model.Feature;
import at.researchstudio.sat.merkmalservice.model.StringFeature;
import at.researchstudio.sat.merkmalservice.model.ifc.IfcDerivedUnit;
import at.researchstudio.sat.merkmalservice.model.ifc.IfcProperty;
import at.researchstudio.sat.merkmalservice.model.ifc.IfcSIUnit;
import at.researchstudio.sat.merkmalservice.model.ifc.IfcUnit;
import at.researchstudio.sat.merkmalservice.vocab.ifc.IfcPropertyType;
import at.researchstudio.sat.merkmalservice.vocab.ifc.IfcUnitType;
import at.researchstudio.sat.merkmalservice.vocab.qudt.QudtQuantityKind;
import at.researchstudio.sat.merkmalservice.vocab.qudt.QudtUnit;
import at.researchstudio.sat.mmsdesktop.model.ifc.*;
import at.researchstudio.sat.mmsdesktop.model.ifc.element.*;
import at.researchstudio.sat.mmsdesktop.util.IfcFileWrapper;
import at.researchstudio.sat.mmsdesktop.util.IfcPropertyBuilder;
import at.researchstudio.sat.mmsdesktop.util.Utils;
import be.ugent.progress.TaskProgressListener;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;

public class IfcFileReader {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static ParsedIfcFile readIfcFile(IfcFileWrapper ifcFile) throws IOException {
        return readIfcFile(ifcFile, null);
    }

    public static ParsedIfcFile readIfcFile(
            IfcFileWrapper ifcFile, TaskProgressListener taskProgressListener) throws IOException {
        List<IfcLine> lines = new ArrayList<>();
        Set<IfcUnit> projectUnits = new HashSet<>();

        boolean updateProgress = Objects.nonNull(taskProgressListener);

        try (LineIterator it =
                FileUtils.lineIterator(ifcFile.getFile(), StandardCharsets.UTF_8.toString())) {
            StringBuilder sb = new StringBuilder();
            while (it.hasNext()) {
                String line = it.nextLine();
                try {
                    // TODO: MAYBE IMPLEMENT A DYNAMIC APPROACH IN ORDER
                    if (line.contains(IfcSinglePropertyValueLine.IDENTIFIER)) {
                        lines.add(new IfcSinglePropertyValueLine(line));
                    } else if (line.contains("IFCQUANTITY")) {
                        if (line.contains(IfcQuantityLengthLine.IDENTIFIER)) {
                            lines.add(new IfcQuantityLengthLine(line));
                        } else if (line.contains(IfcQuantityAreaLine.IDENTIFIER)) {
                            lines.add(new IfcQuantityAreaLine(line));
                        } else if (line.contains(IfcQuantityVolumeLine.IDENTIFIER)) {
                            lines.add(new IfcQuantityVolumeLine(line));
                        } else if (line.contains(IfcQuantityCountLine.IDENTIFIER)) {
                            lines.add(new IfcQuantityCountLine(line));
                        } else {
                            sb.append("Couldnt parse Line: ")
                                    .append(line)
                                    .append(" adding it as IfcQuantityLine")
                                    .append(System.lineSeparator());
                            lines.add(new IfcQuantityLine(line));
                        }
                    } else if (line.contains(IfcBuildingElementProxyLine.IDENTIFIER)) {
                        lines.add(new IfcBuildingElementProxyLine(line));
                    } else if (line.contains(IfcRelDefinesByPropertiesLine.IDENTIFIER)) {
                        lines.add(new IfcRelDefinesByPropertiesLine(line));
                    } else if (line.contains(IfcSIUnitLine.IDENTIFIER)) {
                        lines.add(new IfcSIUnitLine(line));
                    } else if (line.contains(IfcPropertyEnumerationLine.IDENTIFIER)) {
                        lines.add(new IfcPropertyEnumerationLine(line));
                    } else if (line.contains(IfcPropertyEnumeratedValueLine.IDENTIFIER)) {
                        lines.add(new IfcPropertyEnumeratedValueLine(line));
                    } else if (line.contains(IfcDerivedUnitElementLine.IDENTIFIER)) {
                        lines.add(new IfcDerivedUnitElementLine(line));
                    } else if (line.contains(IfcDerivedUnitLine.IDENTIFIER)) {
                        lines.add(new IfcDerivedUnitLine(line));
                    } else if (line.contains(IfcPropertySetLine.IDENTIFIER)) {
                        lines.add(new IfcPropertySetLine(line));
                    } else if (line.contains(IfcUnitAssignmentLine.IDENTIFIER)) {
                        lines.add(new IfcUnitAssignmentLine(line));
                    } else if (line.contains(IfcProjectLine.IDENTIFIER)) {
                        lines.add(new IfcProjectLine(line));
                    } else if (line.contains(IfcBeamStandardCaseLine.IDENTIFIER)) {
                        lines.add(new IfcBeamStandardCaseLine(line));
                    } else if (line.contains(IfcBearingLine.IDENTIFIER)) {
                        lines.add(new IfcBearingLine(line));
                    } else if (line.contains(IfcCaissonFoundationLine.IDENTIFIER)) {
                        lines.add(new IfcCaissonFoundationLine(line));
                    } else if (line.contains(IfcChimneyLine.IDENTIFIER)) {
                        lines.add(new IfcChimneyLine(line));
                    } else if (line.contains(IfcColumnStandardCaseLine.IDENTIFIER)) {
                        lines.add(new IfcColumnStandardCaseLine(line));
                    } else if (line.contains(IfcCourseLine.IDENTIFIER)) {
                        lines.add(new IfcCourseLine(line));
                    } else if (line.contains(IfcCoveringLine.IDENTIFIER)) {
                        lines.add(new IfcCoveringLine(line));
                    } else if (line.contains(IfcCurtainWallLine.IDENTIFIER)) {
                        lines.add(new IfcCurtainWallLine(line));
                    } else if (line.contains(IfcDoorStandardCaseLine.IDENTIFIER)) {
                        lines.add(new IfcDoorStandardCaseLine(line));
                    } else if (line.contains(IfcEarthworksFillLine.IDENTIFIER)) {
                        lines.add(new IfcEarthworksFillLine(line));
                    } else if (line.contains(IfcFootingLine.IDENTIFIER)) {
                        lines.add(new IfcFootingLine(line));
                    } else if (line.contains(IfcKerbLine.IDENTIFIER)) {
                        lines.add(new IfcKerbLine(line));
                    } else if (line.contains(IfcMemberStandardCaseLine.IDENTIFIER)) {
                        lines.add(new IfcMemberStandardCaseLine(line));
                    } else if (line.contains(IfcMooringDeviceLine.IDENTIFIER)) {
                        lines.add(new IfcMooringDeviceLine(line));
                    } else if (line.contains(IfcNavigatorElementLine.IDENTIFIER)) {
                        lines.add(new IfcNavigatorElementLine(line));
                    } else if (line.contains(IfcPileLine.IDENTIFIER)) {
                        lines.add(new IfcPileLine(line));
                    } else if (line.contains(IfcPlateStandardCaseLine.IDENTIFIER)) {
                        lines.add(new IfcPlateStandardCaseLine(line));
                    } else if (line.contains(IfcRailingLine.IDENTIFIER)) {
                        lines.add(new IfcRailingLine(line));
                    } else if (line.contains(IfcRampFlightLine.IDENTIFIER)) {
                        lines.add(new IfcRampFlightLine(line));
                    } else if (line.contains(IfcRampLine.IDENTIFIER)) {
                        lines.add(new IfcRampLine(line));
                    } else if (line.contains(IfcReinforcedSoilLine.IDENTIFIER)) {
                        lines.add(new IfcReinforcedSoilLine(line));
                    } else if (line.contains(IfcRoofLine.IDENTIFIER)) {
                        lines.add(new IfcRoofLine(line));
                    } else if (line.contains(IfcShadingDeviceLine.IDENTIFIER)) {
                        lines.add(new IfcShadingDeviceLine(line));
                    } else if (line.contains(IfcSlabElementedCaseLine.IDENTIFIER)) {
                        lines.add(new IfcSlabElementedCaseLine(line));
                    } else if (line.contains(IfcSlabStandardCaseLine.IDENTIFIER)) {
                        lines.add(new IfcSlabStandardCaseLine(line));
                    } else if (line.contains(IfcStairFlightLine.IDENTIFIER)) {
                        lines.add(new IfcStairFlightLine(line));
                    } else if (line.contains(IfcStairLine.IDENTIFIER)) {
                        lines.add(new IfcStairLine(line));
                    } else if (line.contains(IfcTrackElementLine.IDENTIFIER)) {
                        lines.add(new IfcTrackElementLine(line));
                    } else if (line.contains(IfcWallElementedCaseLine.IDENTIFIER)) {
                        lines.add(new IfcWallElementedCaseLine(line));
                    } else if (line.contains(IfcWallStandardCaseLine.IDENTIFIER)) {
                        lines.add(new IfcWallStandardCaseLine(line));
                    } else if (line.contains(IfcWindowStandardCaseLine.IDENTIFIER)) {
                        lines.add(new IfcWindowStandardCaseLine(line));
                    } else {
                        lines.add(new IfcLine(line));
                    }
                } catch (IllegalArgumentException e) {
                    // TODO: FIX PARSING OR IGNORE
                    taskProgressListener.notifyProgress(
                            null, "Couldnt parse Line: " + line + " adding it as IfcLine", 0);
                    lines.add(new IfcLine(line));
                }
            }
            if (sb.length() > 0) {
                taskProgressListener.notifyProgress(null, sb.toString(), 0);
            }
        }

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

        for (IfcLine line :
                ifcLinesGrouped.getOrDefault(IfcSIUnitLine.class, Collections.emptyList())) {
            IfcSIUnitLine unitLine = (IfcSIUnitLine) line;
            projectUnits.add(
                    new IfcSIUnit(
                            unitLine.getStringId(),
                            unitLine.getType(),
                            unitLine.getMeasure(),
                            unitLine.getPrefix(),
                            defaultProjectUnitIds.contains(unitLine.getId())));
        }

        List<IfcUnit> projectSIUnits =
                projectUnits.stream()
                        .filter(unit -> unit instanceof IfcSIUnit)
                        .collect(Collectors.toList());

        for (IfcLine line :
                ifcLinesGrouped.getOrDefault(IfcDerivedUnitLine.class, Collections.emptyList())) {
            IfcDerivedUnitLine unitLine = (IfcDerivedUnitLine) line;
            IfcDerivedUnit tempDerivedUnit =
                    new IfcDerivedUnit(
                            unitLine.getStringId(),
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
                        tempDerivedUnit.addDerivedUnitElement(
                                (IfcSIUnit) unit, derivedUnitElementLine.getExponent());
                    }
                }
            }

            projectUnits.add(tempDerivedUnit);
        }

        if (updateProgress) {
            taskProgressListener.notifyProgress(
                    null, "Extracted " + projectUnits.size() + " IfcUnits", 0);
        }

        Map<IfcUnitType, List<IfcUnit>> projectUnitsMap =
                projectUnits.stream().collect(Collectors.groupingBy(IfcUnit::getType));

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

        ParsedIfcFile parsedIfcFile = new ParsedIfcFile(lines, extractedProperties);
        StringBuilder extractLog = new StringBuilder();
        parsedIfcFile.setFeatures(
                extractFeaturesFromProperties(parsedIfcFile.getExtractedPropertyMap(), extractLog));

        if (updateProgress) {
            taskProgressListener.notifyProgress(null, extractLog.toString(), 0);
        }

        return parsedIfcFile;
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
                                                    Utils.parseNumericFeature(
                                                            ifcProperty, QudtQuantityKind.VOLUME))
                                    .collect(Collectors.toList()));
                    break;
                case AREA_MEASURE:
                    fullLog.append(logString).append(System.getProperty("line.separator"));
                    extractedFeatures.addAll(
                            entry.getValue().stream()
                                    .map(
                                            ifcProperty ->
                                                    Utils.parseNumericFeature(
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
                                                    Utils.parseNumericFeature(
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
                                                    Utils.parseNumericFeature(
                                                            ifcProperty, QudtQuantityKind.ANGLE))
                                    .collect(Collectors.toList()));
                    break;
                case THERMAL_TRANSMITTANCE_MEASURE:
                    fullLog.append(logString).append(System.getProperty("line.separator"));
                    extractedFeatures.addAll(
                            entry.getValue().stream()
                                    .map(
                                            ifcProperty ->
                                                    Utils.parseNumericFeature(
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
                                                    Utils.parseNumericFeature(
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
                                                    Utils.parseNumericFeature(
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
                                                    Utils.parseNumericFeature(
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
                                                    Utils.parseNumericFeature(
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
                                                    Utils.parseNumericFeature(
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
                                                    Utils.parseNumericFeature(
                                                            ifcProperty, QudtQuantityKind.FORCE))
                                    .collect(Collectors.toList()));
                    break;
                case MOMENT_OF_INERTIA_MEASURE:
                    fullLog.append(logString).append(System.getProperty("line.separator"));
                    extractedFeatures.addAll(
                            entry.getValue().stream()
                                    .map(
                                            ifcProperty ->
                                                    Utils.parseNumericFeature(
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
                                                    Utils.parseNumericFeature(
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
                                                    Utils.parseNumericFeature(
                                                            ifcProperty, QudtQuantityKind.MASS))
                                    .collect(Collectors.toList()));
                    break;
                case PRESSURE_MEASURE:
                    fullLog.append(logString).append(System.getProperty("line.separator"));
                    extractedFeatures.addAll(
                            entry.getValue().stream()
                                    .map(
                                            ifcProperty ->
                                                    Utils.parseNumericFeature(
                                                            ifcProperty, QudtQuantityKind.PRESSURE))
                                    .collect(Collectors.toList()));
                    break;
                case LUMINOUS_FLUX_MEASURE:
                    fullLog.append(logString).append(System.getProperty("line.separator"));
                    extractedFeatures.addAll(
                            entry.getValue().stream()
                                    .map(
                                            ifcProperty ->
                                                    Utils.parseNumericFeature(
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
                                                    Utils.parseNumericFeature(
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
                                                    Utils.parseNumericFeature(
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
                                                    Utils.parseNumericFeature(
                                                            ifcProperty, QudtQuantityKind.POWER))
                                    .collect(Collectors.toList()));
                    break;
                case LENGTH_MEASURE:
                case POSITIVE_LENGTH_MEASURE:
                    fullLog.append(logString).append(System.getProperty("line.separator"));
                    extractedFeatures.addAll(
                            entry.getValue().stream()
                                    .map(Utils::parseNumericFeature)
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
