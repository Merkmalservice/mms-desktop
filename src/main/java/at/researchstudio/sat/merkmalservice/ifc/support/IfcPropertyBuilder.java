package at.researchstudio.sat.merkmalservice.ifc.support;

import at.researchstudio.sat.merkmalservice.ifc.model.*;
import at.researchstudio.sat.merkmalservice.model.ifc.IfcProperty;
import at.researchstudio.sat.merkmalservice.model.ifc.IfcUnit;
import at.researchstudio.sat.merkmalservice.utils.Utils;
import at.researchstudio.sat.merkmalservice.vocab.ifc.IfcPropertyType;
import at.researchstudio.sat.merkmalservice.vocab.ifc.IfcUnitType;
import java.lang.invoke.MethodHandles;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IfcPropertyBuilder {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final String name;
    private final IfcPropertyType type;
    private IfcUnit unit;

    public IfcPropertyBuilder(IfcQuantityLine line, Map<IfcUnitType, List<IfcUnit>> projectUnits) {
        this.name = Utils.convertIFCStringToUtf8(line.getName());

        this.type =
                IfcUtils.executeOrDefaultOnException(
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

        if (line.getUnitId() != 0) {
            this.unit = getIfcUnitWithId(line.getUnitId(), projectUnits);
            if (Objects.isNull(this.unit)) {
                logger.warn(
                        "Could not find Unit for IfcUnit with Id<{}>, name<{}>, IfcPropertyType<{}> in project units",
                        line.getUnitId(),
                        this.name,
                        this.type);
                logProjectUnits(projectUnits);
            }
        } else if (this.type.isMeasureType()) {
            this.unit = getIfcUnitFromProjectUnits(this.type, projectUnits);
            if (Objects.isNull(this.unit)) {
                logger.warn(
                        "Could not find Unit for name<{}>, IfcPropertyType<{}> in project units",
                        this.name,
                        type);
                logProjectUnits(projectUnits);
            }
        }
    }

    private void logProjectUnits(Map<IfcUnitType, List<IfcUnit>> projectUnits) {
        logger.debug("project units:");
        projectUnits.forEach(
                (key, value) -> {
                    logger.debug("\t{}", key.toString());
                    Objects.requireNonNullElse(value, Collections.emptyList())
                            .forEach(unit -> logger.debug("\t\t{}", unit));
                });
    }

    public IfcPropertyBuilder(
            IfcSinglePropertyValueLine line, Map<IfcUnitType, List<IfcUnit>> projectUnits) {
        this.name = Utils.convertIFCStringToUtf8(line.getName());

        this.type =
                IfcUtils.executeOrDefaultOnException(
                        () -> IfcPropertyType.fromString(line.getType()),
                        IfcPropertyType.UNKNOWN,
                        NullPointerException.class,
                        IllegalArgumentException.class);

        if (line.getUnitId() != null) {
            this.unit = getIfcUnitWithId(line.getUnitId(), projectUnits);
            if (Objects.isNull(this.unit)) {
                logger.warn(
                        "Could not find Unit for IfcUnit with Id<{}>, name<{}>, IfcPropertyType<{}> in project units",
                        line.getUnitId(),
                        this.name,
                        this.type);
                logProjectUnits(projectUnits);
            }
        } else if (this.type.isMeasureType()) {
            this.unit = getIfcUnitFromProjectUnits(this.type, projectUnits);
            if (Objects.isNull(this.unit)) {
                logger.warn(
                        "Could not find Unit for name<{}>, IfcPropertyType<{}> in project units",
                        this.name,
                        type);
                logger.warn("within ProjectUnits");
                logProjectUnits(projectUnits);
            }
        }
    }

    public IfcProperty build() {
        return new IfcProperty(this.name, this.type, this.unit);
    }

    private static IfcUnit getIfcUnitWithId(int id, Map<IfcUnitType, List<IfcUnit>> projectUnits) {
        if (Objects.nonNull(projectUnits)) {

            for (Map.Entry<IfcUnitType, List<IfcUnit>> entryList : projectUnits.entrySet()) {
                for (IfcUnit unit : entryList.getValue()) {
                    if (unit.getId() != null && unit.getId() == id) {
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
                    return units.get(0);
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
