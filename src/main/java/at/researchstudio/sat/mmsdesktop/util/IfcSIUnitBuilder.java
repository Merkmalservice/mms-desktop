package at.researchstudio.sat.mmsdesktop.util;

import at.researchstudio.sat.merkmalservice.model.ifc.IfcSIUnit;
import at.researchstudio.sat.merkmalservice.vocab.ifc.IfcUnitMeasure;
import at.researchstudio.sat.merkmalservice.vocab.ifc.IfcUnitMeasurePrefix;
import at.researchstudio.sat.merkmalservice.vocab.ifc.IfcUnitType;
import org.apache.jena.query.QuerySolution;

public class IfcSIUnitBuilder {
    private IfcUnitType type;
    private IfcUnitMeasure measure;
    private IfcUnitMeasurePrefix prefix;

    public IfcSIUnitBuilder(QuerySolution qs) {
        this.type =
                Utils.executeOrDefaultOnException(
                        () -> IfcUnitType.fromString(qs.getResource("unitType").getURI()),
                        IfcUnitType.UNKNOWN,
                        NullPointerException.class,
                        IllegalArgumentException.class);
        this.measure =
                Utils.executeOrDefaultOnException(
                        () -> IfcUnitMeasure.fromString(qs.getResource("unitMeasure").getURI()),
                        IfcUnitMeasure.UNKNOWN,
                        NullPointerException.class,
                        IllegalArgumentException.class);
        this.prefix =
                Utils.executeOrDefaultOnException(
                        () ->
                                IfcUnitMeasurePrefix.fromString(
                                        qs.getResource("unitPrefix").getURI()),
                        IfcUnitMeasurePrefix.NONE,
                        NullPointerException.class,
                        IllegalArgumentException.class);
    }

    public IfcSIUnit build() {
        return new IfcSIUnit(type, measure, prefix);
    }
}
