package at.researchstudio.sat.mmsdesktop.model.ifc;

import at.researchstudio.sat.mmsdesktop.model.ifc.vocab.IfcUnitMeasure;
import at.researchstudio.sat.mmsdesktop.model.ifc.vocab.IfcUnitType;
import org.apache.jena.rdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

public class IfcUnit {
  private static final Logger logger =
          LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final IfcUnitType type;
  private final IfcUnitMeasure measure;

  public IfcUnit(Resource type, Resource measure) {
    IfcUnitMeasure tempMeasure = IfcUnitMeasure.UNKNOWN;
    IfcUnitType tempType = IfcUnitType.UNKNOWN;
    try {
      tempType = IfcUnitType.fromResource(type);
    } catch (IllegalArgumentException e) {
      logger.error(e.getMessage());
    }

    try {
      tempMeasure = IfcUnitMeasure.fromResource(measure);
    } catch (IllegalArgumentException e) {
      logger.error(e.getMessage());
    }

    this.type = tempType;
    this.measure = tempMeasure;
  }

  public IfcUnitType getType() {
    return type;
  }

  public IfcUnitMeasure getMeasure() {
    return measure;
  }

  @Override
  public String toString() {
    return "IfcUnit{" + "type=" + type + ", measure=" + measure + '}';
  }
}
