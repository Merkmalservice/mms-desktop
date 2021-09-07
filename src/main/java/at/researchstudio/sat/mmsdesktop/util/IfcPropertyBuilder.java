package at.researchstudio.sat.mmsdesktop.util;

import at.researchstudio.sat.merkmalservice.model.ifc.IfcProperty;
import at.researchstudio.sat.merkmalservice.model.ifc.IfcUnit;
import at.researchstudio.sat.merkmalservice.utils.Utils;
import at.researchstudio.sat.merkmalservice.vocab.ifc.IfcPropertyType;
import at.researchstudio.sat.merkmalservice.vocab.ifc.IfcUnitType;
import at.researchstudio.sat.mmsdesktop.model.ifc.*;
import java.lang.invoke.MethodHandles;
import java.util.*;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IfcPropertyBuilder {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private String name;
    private IfcPropertyType type;
    private IfcUnit unit;

    public IfcPropertyBuilder(IfcQuantityLine line, Map<IfcUnitType, List<IfcUnit>> projectUnits) {
        this.name = Utils.convertIFCStringToUtf8(line.getName());

        this.type =
                at.researchstudio.sat.mmsdesktop.util.Utils.executeOrDefaultOnException(
                        () -> {
                            if (line instanceof IfcQuantityLengthLine) {
                                return IfcPropertyType.LENGTH_MEASURE;
                            } else if (line instanceof IfcQuantityAreaLine) {
                                return IfcPropertyType.AREA_MEASURE;
                            } else if (line instanceof IfcQuantityVolumeLine) {
                                return IfcPropertyType.VOLUME_MEASURE;
                            } else if (line instanceof IfcQuantityCountLine) {
                                return IfcPropertyType.COUNT_MEASURE;
                            } // TODO: FIGURE OUT HOW TO HANDLE UNKNOWNS
                            return IfcPropertyType.UNKNOWN;
                        },
                        IfcPropertyType.UNKNOWN,
                        NullPointerException.class,
                        IllegalArgumentException.class);

        if (Objects.nonNull(line.getUnitId())) {
            this.unit = getIfcUnitWithId(line.getUnitId(), projectUnits);
            if (Objects.isNull(this.unit)) {
                logger.warn(
                        "Could not find Unit for IfcUnit with Id<{}>, name<{}>, IfcPropertyType<{}>",
                        line.getUnitId(),
                        this.name,
                        this.type);
                logger.warn("within ProjectUnits:");
                projectUnits.forEach(
                        (key, value) -> {
                            logger.warn(key.toString());
                            Objects.requireNonNullElse(value, Collections.emptyList())
                                    .forEach(unit -> logger.warn("\t{}", unit));
                        });
                logger.warn("###");
            }
        } else if (this.type.isMeasureType()) {
            this.unit = getIfcUnitFromProjectUnits(this.type, projectUnits);
            if (Objects.isNull(this.unit)) {
                logger.warn(
                        "Could not find Unit for name<{}>, IfcPropertyType<{}>", this.name, type);
                logger.warn("within ProjectUnits:");
                projectUnits.forEach(
                        (key, value) -> {
                            logger.warn(key.toString());
                            Objects.requireNonNullElse(value, Collections.emptyList())
                                    .forEach(unit -> logger.warn("\t{}", unit));
                        });
                logger.warn("###");
            }
        }
    }

    public IfcPropertyBuilder(
            IfcSinglePropertyValueLine line, Map<IfcUnitType, List<IfcUnit>> projectUnits) {
        this.name = Utils.convertIFCStringToUtf8(line.getName());

        this.type =
                at.researchstudio.sat.mmsdesktop.util.Utils.executeOrDefaultOnException(
                        () -> IfcPropertyType.fromString(line.getType()),
                        IfcPropertyType.UNKNOWN,
                        NullPointerException.class,
                        IllegalArgumentException.class);

        if (Objects.nonNull(line.getUnitId())) {
            this.unit = getIfcUnitWithId(line.getUnitId(), projectUnits);
            if (Objects.isNull(this.unit)) {
                logger.warn(
                        "Could not find Unit for IfcUnit with Id<{}>, name<{}>, IfcPropertyType<{}>",
                        line.getUnitId(),
                        this.name,
                        this.type);
                logger.warn("within ProjectUnits:");
                projectUnits.forEach(
                        (key, value) -> {
                            logger.warn(key.toString());
                            Objects.requireNonNullElse(value, Collections.emptyList())
                                    .forEach(unit -> logger.warn("\t{}", unit));
                        });
                logger.warn("###");
            }
        } else if (this.type.isMeasureType()) {
            this.unit = getIfcUnitFromProjectUnits(this.type, projectUnits);
            if (Objects.isNull(this.unit)) {
                logger.warn(
                        "Could not find Unit for name<{}>, IfcPropertyType<{}>", this.name, type);
                logger.warn("within ProjectUnits:");
                projectUnits.forEach(
                        (key, value) -> {
                            logger.warn(key.toString());
                            Objects.requireNonNullElse(value, Collections.emptyList())
                                    .forEach(unit -> logger.warn("\t{}", unit));
                        });
                logger.warn("###");
            }
        }
    }

    public IfcPropertyBuilder(QuerySolution qs, Map<IfcUnitType, List<IfcUnit>> projectUnits) {
        this.name = Utils.convertIFCStringToUtf8(qs.getLiteral("propName").toString());

        this.type =
                at.researchstudio.sat.mmsdesktop.util.Utils.executeOrDefaultOnException(
                        () -> IfcPropertyType.fromString(qs.getResource("propType").getURI()),
                        IfcPropertyType.UNKNOWN,
                        NullPointerException.class,
                        IllegalArgumentException.class);

        Resource propUnitUriResource = qs.getResource("propUnitUri");

        if (Objects.nonNull(propUnitUriResource)) {
            this.unit = getIfcUnitWithId(propUnitUriResource.getURI(), projectUnits);
            if (Objects.isNull(this.unit)) {
                logger.warn(
                        "Could not find Unit for IfcUnit with Id<{}>, name<{}>, IfcPropertyType<{}>",
                        propUnitUriResource.getURI(),
                        this.name,
                        this.type);
                logger.warn("within ProjectUnits:");
                projectUnits.forEach(
                        (key, value) -> {
                            logger.warn(key.toString());
                            Objects.requireNonNullElse(value, Collections.emptyList())
                                    .forEach(unit -> logger.warn("\t{}", unit));
                        });
                logger.warn("###");
            }
        } else if (this.type.isMeasureType()) {
            this.unit = getIfcUnitFromProjectUnits(this.type, projectUnits);
            if (Objects.isNull(this.unit)) {
                logger.warn(
                        "Could not find Unit for name<{}>, IfcPropertyType<{}>", this.name, type);
                logger.warn("within ProjectUnits:");
                projectUnits.forEach(
                        (key, value) -> {
                            logger.warn(key.toString());
                            Objects.requireNonNullElse(value, Collections.emptyList())
                                    .forEach(unit -> logger.warn("\t{}", unit));
                        });
                logger.warn("###");
            }
        }
    }

    public IfcProperty build() {
        return new IfcProperty(this.name, this.type, this.unit);
    }

    private static IfcUnit getIfcUnitWithId(
            String id, Map<IfcUnitType, List<IfcUnit>> projectUnits) {
        if (Objects.nonNull(projectUnits)) {

            for (Map.Entry<IfcUnitType, List<IfcUnit>> entryList : projectUnits.entrySet()) {
                for (IfcUnit unit : entryList.getValue()) {
                    if (id.equals(unit.getId())) {
                        return unit;
                    }
                }
            }
        }
        return null;
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
                    Optional<IfcUnit> defaultUnit =
                            units.stream().filter(IfcUnit::isProjectDefault).findFirst();
                    if (defaultUnit.isPresent()) {
                        return defaultUnit.get();
                    }
                    logger.warn(
                            "More than one unit present for IfcPropertyType<{}>, leaving it empty",
                            type);
                    units.forEach(unit -> logger.debug(unit.toString()));
                }
            }
        }
        return null;
    }
}
