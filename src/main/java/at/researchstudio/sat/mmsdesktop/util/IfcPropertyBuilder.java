package at.researchstudio.sat.mmsdesktop.util;

import at.researchstudio.sat.merkmalservice.model.ifc.IfcProperty;
import at.researchstudio.sat.merkmalservice.model.ifc.IfcUnit;
import at.researchstudio.sat.merkmalservice.utils.Utils;
import at.researchstudio.sat.merkmalservice.vocab.ifc.IfcPropertyType;
import at.researchstudio.sat.merkmalservice.vocab.ifc.IfcUnitType;
import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.jena.query.QuerySolution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IfcPropertyBuilder {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private String name;
    private IfcPropertyType type;
    private IfcUnit unit;

    public IfcPropertyBuilder(QuerySolution qs, Map<IfcUnitType, List<IfcUnit>> projectUnits) {
        this.name = Utils.convertIFCStringToUtf8(qs.getLiteral("propName").toString());

        this.type =
                at.researchstudio.sat.mmsdesktop.util.Utils.executeOrDefaultOnException(
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

    public IfcProperty build() {
        return new IfcProperty(this.name, this.type, this.unit);
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
}
