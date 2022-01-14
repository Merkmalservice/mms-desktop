package at.researchstudio.sat.merkmalservice.ifc.support;

import static java.util.stream.Collectors.joining;

import at.researchstudio.sat.merkmalservice.model.ifc.IfcUnit;
import at.researchstudio.sat.merkmalservice.vocab.ifc.IfcUnitType;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProjectUnits {
    private final Map<IfcUnitType, List<IfcUnit>> unitsByUnitType;
    private final Map<Integer, IfcUnit> unitsById;

    public ProjectUnits(
            Map<IfcUnitType, List<IfcUnit>> unitsByUnitType, Map<Integer, IfcUnit> unitsById) {
        this.unitsByUnitType = unitsByUnitType;
        this.unitsById = unitsById;
    }

    public ProjectUnits(ProjectUnits toCopy) {
        this.unitsByUnitType = new HashMap<>(toCopy.unitsByUnitType);
        this.unitsById = new HashMap<>(toCopy.unitsById);
    }

    public IfcUnit getById(Integer id) {
        IfcUnit unit = unitsById.get(id);
        if (unit == null) {
            throw new IllegalArgumentException("No unit found for id " + id);
        }
        return unit;
    }

    public IfcUnit getDefaultUnitForUnitType(IfcUnitType unitType) {
        List<IfcUnit> units =
                this.unitsByUnitType.get(unitType).stream()
                        .filter(IfcUnit::isProjectDefault)
                        .collect(Collectors.toList());
        if (units == null || units.isEmpty()) {
            throw new IllegalStateException(
                    "No project unit found for measurement type " + unitType);
        }
        if (units.size() > 1) {
            throw new IllegalStateException(
                    String.format(
                            "More than one unit found for measurement type %s: %s",
                            unitType, units.stream().map(Object::toString).collect(joining(","))));
        }
        return units.get(0);
    }

    public Map<IfcUnitType, List<IfcUnit>> getUnitsByUnitType() {
        return Collections.unmodifiableMap(unitsByUnitType);
    }

    public Map<Integer, IfcUnit> getUnitsById() {
        return Collections.unmodifiableMap(unitsById);
    }
}
