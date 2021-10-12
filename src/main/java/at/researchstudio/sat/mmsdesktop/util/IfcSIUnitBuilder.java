package at.researchstudio.sat.mmsdesktop.util;

import at.researchstudio.sat.merkmalservice.model.ifc.IfcSIUnit;
import at.researchstudio.sat.merkmalservice.vocab.ifc.IfcUnitMeasure;
import at.researchstudio.sat.merkmalservice.vocab.ifc.IfcUnitMeasurePrefix;
import at.researchstudio.sat.merkmalservice.vocab.ifc.IfcUnitType;
import org.apache.jena.query.QuerySolution;

public class IfcSIUnitBuilder {
    private final IfcUnitType type;
    private final IfcUnitMeasure measure;
    private final IfcUnitMeasurePrefix prefix;
    private final String uri;
    private final boolean projectDefault;

    public IfcSIUnitBuilder(QuerySolution qs) {
        this.uri =
                qs.getResource("unitUri")
                        .getURI(); // TODO: MAYBE ERROR HANDLING BUT THIS SHOULD BE HERE ALWAYS I
        // THINK
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

        this.projectDefault =
                Utils.executeOrDefaultOnException(
                        () -> qs.getResource("projectUri").getURI() != null, false);
    }

    public IfcSIUnit build() {
        return new IfcSIUnit(uri, type, measure, prefix, projectDefault);
    }
}
