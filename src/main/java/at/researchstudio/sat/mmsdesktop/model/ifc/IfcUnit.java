package at.researchstudio.sat.mmsdesktop.model.ifc;

import at.researchstudio.sat.mmsdesktop.model.ifc.vocab.IfcUnitMeasure;
import at.researchstudio.sat.mmsdesktop.model.ifc.vocab.IfcUnitType;
import org.apache.jena.rdf.model.Resource;

public class IfcUnit {
  private final IfcUnitType type;
  private final IfcUnitMeasure measure;

  public IfcUnit(Resource type, Resource measure) {
    IfcUnitMeasure tempMeasure = IfcUnitMeasure.UNKNOWN;
    IfcUnitType tempType = IfcUnitType.UNKNOWN;
    try {
      tempType = IfcUnitType.fromResource(type);
    } catch (IllegalArgumentException e) {
      System.err.println(e.getMessage());
    }

    try {
      tempMeasure = IfcUnitMeasure.fromResource(measure);
    } catch (IllegalArgumentException e) {
      System.err.println(e.getMessage());
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
