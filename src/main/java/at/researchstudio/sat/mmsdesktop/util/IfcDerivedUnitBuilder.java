package at.researchstudio.sat.mmsdesktop.util;

import at.researchstudio.sat.merkmalservice.model.ifc.IfcDerivedUnit;
import at.researchstudio.sat.merkmalservice.vocab.ifc.IfcUnitType;
import java.util.Objects;
import org.apache.jena.query.QuerySolution;

public class IfcDerivedUnitBuilder {
    private IfcUnitType type;
    private String userDefinedLabel;
    private String uri;
    private boolean projectDefault;

    public IfcDerivedUnitBuilder(QuerySolution qs) {
        this.uri =
                qs.getResource("unitUri")
                        .getURI(); // TODO: MAYBE ERROR HANDLING BUT THIS SHOULD BE HERE ALWAYS I
        // THINK
        if (Objects.nonNull(qs.getLiteral("userDefinedTypeLabel"))) {
            this.userDefinedLabel = qs.getLiteral("userDefinedTypeLabel").getString();
        }

        this.projectDefault =
                Utils.executeOrDefaultOnException(
                        () -> qs.getResource("projectUri").getURI() != null, false);

        this.type =
                Utils.executeOrDefaultOnException(
                        () -> IfcUnitType.fromString(qs.getResource("derivedUnitType").getURI()),
                        IfcUnitType.UNKNOWN,
                        NullPointerException.class,
                        IllegalArgumentException.class);
    }

    public IfcDerivedUnit build() {
        return new IfcDerivedUnit(uri, type, userDefinedLabel, projectDefault);
    }
}