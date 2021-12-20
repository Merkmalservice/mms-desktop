package at.researchstudio.sat.merkmalservice.ifc.convert.support;

import at.researchstudio.sat.merkmalservice.model.mapping.MappingExecutionValue;
import at.researchstudio.sat.merkmalservice.model.mapping.feature.Feature;
import at.researchstudio.sat.merkmalservice.model.mapping.feature.featuretype.EnumFeatureType;
import at.researchstudio.sat.merkmalservice.model.mapping.feature.featuretype.OptionValue;
import at.researchstudio.sat.merkmalservice.support.exception.ActionValueException;
import java.util.Optional;

public class ConversionRuleUtils {
    public static MappingExecutionValue getEffectiveActionValue(
            Feature actionFeature, MappingExecutionValue actionValue) {
        if (actionFeature.getType() instanceof EnumFeatureType) {
            if (actionValue.getIdValue().isEmpty()) {
                throw new ActionValueException(
                        String.format(
                                "Action value must be an IdValue if the feature type is EnumFeatureType, but was %s",
                                actionValue.toString()));
            }
            EnumFeatureType featureType = (EnumFeatureType) actionFeature.getType();
            Optional<OptionValue> enumOptionValue =
                    featureType.getOptions().stream()
                            .filter(o -> o.getId().equals(actionValue.getIdValue().get()))
                            .findFirst();
            if (enumOptionValue.isEmpty()) {
                throw new ActionValueException(
                        String.format(
                                "Action value must be an IdValue of one of the EnumFeatureType's options, but was %s",
                                actionValue.toString()));
            }
            return enumOptionValue.get().getValue();
        }
        return actionValue;
    }
}
