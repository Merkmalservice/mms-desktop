package at.researchstudio.sat.mmsdesktop.model.ifc;

import at.researchstudio.sat.merkmalservice.vocab.ifc.IfcPropertyType;
import at.researchstudio.sat.merkmalservice.vocab.ifc.IfcUnitMeasure;
import at.researchstudio.sat.merkmalservice.vocab.ifc.IfcUnitMeasurePrefix;
import at.researchstudio.sat.merkmalservice.vocab.ifc.IfcUnitType;
import at.researchstudio.sat.mmsdesktop.util.Utils;
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Literal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

public class IfcProperty {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final String name;
    private final IfcPropertyType type;

    private IfcUnit unit;

    private Set<String> extractedUniqueValues;

    private Set<String> enumOptionValues;

    public IfcProperty(IfcProperty ifc, Map<IfcUnitType, List<IfcUnit>> projectUnits) {
        this.name = Utils.convertIFCStringToUtf8(ifc.name);
        this.type = ifc.type;

        if (this.type.isMeasureType()) {
            this.unit = getIfcUnitFromProjectUnits(ifc.type, projectUnits);
        }
    }

    public IfcProperty(QuerySolution qs, Map<IfcUnitType, List<IfcUnit>> projectUnits) {
        this.name = Utils.convertIFCStringToUtf8(qs.getLiteral("propName").toString());

        this.type =
                Utils.executeOrDefaultOnException(
                        () -> IfcPropertyType.fromString(qs.getResource("propType").getURI()),
                        IfcPropertyType.UNKNOWN,
                        NullPointerException.class,
                        IllegalArgumentException.class);

        if (this.type.isMeasureType()) {
            // TODO: update query and add optional propMeasure and propMeasurePrefix to add the ifc
            // unit to the query
            // output
            // (not yet possible since our ifc-files do not have a specific unit attached to the
            // properties)
            // Resource unitMeasure = qs.getResource("propMeasure");
            // Resource unitMeasurePrefix = qs.getResource("propMeasurePrefix");

            this.unit = getIfcUnitFromProjectUnits(this.type, projectUnits);
        }
    }

    public IfcProperty(String name, String type) {
        this.name = Utils.convertIFCStringToUtf8(name);

        IfcPropertyType tempType = IfcPropertyType.UNKNOWN;
        try {
            tempType = IfcPropertyType.fromString(type);
        } catch (IllegalArgumentException e) {
            logger.error(e.getMessage());
        }

        this.type = tempType;
    }

    private static IfcUnit getIfcUnitFromProjectUnits(
            IfcPropertyType type, Map<IfcUnitType, List<IfcUnit>> projectUnits) {
        if (Objects.nonNull(projectUnits)) {
            IfcUnitType tempUnitType = type.getUnitType();
            List<IfcUnit> units = projectUnits.get(tempUnitType);

            if (Objects.nonNull(units)) {
                if (units.size() == 1) {
                    IfcUnit ifcUnit = units.get(0);
                    return ifcUnit;
                } else {
                    logger.warn(
                            "More than one unit present for IfcPropertyType<{}>, leaving it empty",
                            type);
                    units.forEach(unit -> logger.debug(unit.toString()));
                }
            }
        }
        logger.warn("Could not find Unit for IfcPropertyType<{}>", type);
        logger.warn("within ProjectUnits:");
        projectUnits.forEach(
                (key, value) -> {
                    logger.warn(key.toString());
                    Objects.requireNonNullElse(value, Collections.emptyList())
                            .forEach(unit -> logger.warn("\t{}", unit));
                });
        return null;
    }

    public String getName() {
        return name;
    }

    public IfcPropertyType getType() {
        return type;
    }

    public IfcUnitMeasure getMeasure() {
        if (Objects.nonNull(unit)) {
            if (unit instanceof IfcSIUnit) {
                return ((IfcSIUnit) unit).getMeasure();
            } else {
                logger.warn("unit not IfcSIUnit, leaving it empty", type);
            }
        }
        return IfcUnitMeasure.UNKNOWN;
    }

    public IfcUnitMeasurePrefix getMeasurePrefix() {
        if (Objects.nonNull(unit)) {
            if (unit instanceof IfcSIUnit) {
                return ((IfcSIUnit) unit).getPrefix();
            } else {
                logger.warn("unit not IfcSIUnit, leaving it empty", type);
            }
        }
        return IfcUnitMeasurePrefix.NONE;
    }

    public Set<String> getExtractedUniqueValues() {
        return extractedUniqueValues;
    }

    public Set<String> getEnumOptionValues() {
        return enumOptionValues;
    }

    public void addExtractedValue(String value) {
        if (extractedUniqueValues == null) {
            extractedUniqueValues = new HashSet<>();
        }
        extractedUniqueValues.add(value);
    }

    public void addEnumOptionValue(String value) {
        if (enumOptionValues == null) {
            enumOptionValues = new HashSet<>();
        }
        enumOptionValues.add(value);
    }

    public void addEnumOptionValue(QuerySolution qs) {
        Literal enumOptionValue = qs.getLiteral("enumOptionValue");
        if (enumOptionValue != null) {
            addEnumOptionValue(Utils.convertIFCStringToUtf8(enumOptionValue.toString()));
        }
    }

    public void addExtractedValue(QuerySolution qs) {
        Literal propValue = qs.getLiteral("propValue");
        if (propValue != null) {
            addExtractedValue(Utils.convertIFCStringToUtf8(propValue.getValue().toString()));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IfcProperty that = (IfcProperty) o;
        return Objects.equals(name, that.name) && type == that.type && unit == that.unit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, unit);
    }

    @Override
    public String toString() {
        String extractedUniqueValuesString = "NO VALUES";
        if (!CollectionUtils.isEmpty(extractedUniqueValues)) {
            extractedUniqueValuesString =
                    extractedUniqueValues.stream()
                            .collect(Collectors.joining("\n\t", "{\n\t", "\n}"));
        }
        String optionValues = "";
        if (IfcPropertyType.VALUELIST.equals(type) && !CollectionUtils.isEmpty(enumOptionValues)) {
            optionValues =
                    ", optionValues="
                            + enumOptionValues.stream()
                                    .collect(Collectors.joining("\n\t", "{\n\t", "\n}"));
        }

        return "IfcProperty{"
                + "name='"
                + name
                + '\''
                + ", type="
                + type
                + ", ifcUnit="
                + unit
                + ", extractedUniqueValues="
                + extractedUniqueValuesString
                + optionValues
                + '}';
    }
}
