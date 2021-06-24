package at.researchstudio.sat.mmsdesktop.model.ifc;

import at.researchstudio.sat.mmsdesktop.model.ifc.vocab.IfcPropertyType;
import at.researchstudio.sat.mmsdesktop.model.ifc.vocab.IfcUnitMeasure;
import at.researchstudio.sat.mmsdesktop.model.ifc.vocab.IfcUnitType;
import at.researchstudio.sat.mmsdesktop.util.Utils;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class IfcProperty {
  private static final Logger logger =
      LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final String name;
  private final IfcPropertyType type;

  private IfcUnitMeasure measure;

  public IfcProperty(IfcProperty ifc, Map<IfcUnitType, List<IfcUnit>> projectUnits) {
    this.name = Utils.convertIFCStringToUtf8(ifc.name);
    this.type = ifc.type;

    if (this.type.isMeasureType()) {
      this.measure = generateMeasureFromProjectUnits(ifc.type, projectUnits);
    }
  }

  public IfcProperty(QuerySolution qs, Map<IfcUnitType, List<IfcUnit>> projectUnits) {
    this.name = Utils.convertIFCStringToUtf8(qs.getLiteral("propName").toString());

    IfcPropertyType tempType = IfcPropertyType.UNKNOWN;
    try {
      tempType = IfcPropertyType.fromResource(qs.getResource("propType"));
    } catch (IllegalArgumentException e) {
      logger.error(e.getMessage());
    }

    this.type = tempType;

    if (this.type.isMeasureType()) {
      // TODO: update query and add optional propMeasure to add the ifc unit to the query output
      // (not yet possible since our ifc-files do not have a specific unit attached to the
      // properties)
      Resource unitMeasure = qs.getResource("propMeasure");

      IfcUnitMeasure tempMeasure = IfcUnitMeasure.UNKNOWN;
      if (Objects.nonNull(unitMeasure)) {
        try {
          tempMeasure = IfcUnitMeasure.fromResource(unitMeasure);
        } catch (IllegalArgumentException e) {
          logger.error(e.getMessage());
        }
      } else {
        tempMeasure = generateMeasureFromProjectUnits(this.type, projectUnits);
      }
      this.measure = tempMeasure;
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

  private static IfcUnitMeasure generateMeasureFromProjectUnits(IfcPropertyType type,
      Map<IfcUnitType, List<IfcUnit>> projectUnits) {
    if (Objects.nonNull(projectUnits)) {
      IfcUnitType tempUnitType = type.getUnitType();
      List<IfcUnit> units = projectUnits.get(tempUnitType);

      if (Objects.nonNull(units)) {
        if (units.size() == 1) {
          IfcUnit ifcUnit = units.get(0);
          return ifcUnit.getMeasure();
        } else {
          logger.debug("More than one unit present, leaving it empty");
          units.forEach(unit -> logger.debug(unit.toString()));
        }
      }
    }
    return IfcUnitMeasure.UNKNOWN;
  }

  public String getName() {
    return name;
  }

  public IfcPropertyType getType() {
    return type;
  }

  public IfcUnitMeasure getMeasure() {
    return measure;
  }

  @Override public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    IfcProperty that = (IfcProperty) o;
    return Objects.equals(name, that.name) && type == that.type && measure == that.measure;
  }

  @Override public int hashCode() {
    return Objects.hash(name, type, measure);
  }

  @Override public String toString() {
    return "IfcProperty{" + "name='" + name + '\'' + ", type=" + type + ", measure=" + measure
        + '}';
  }
}
