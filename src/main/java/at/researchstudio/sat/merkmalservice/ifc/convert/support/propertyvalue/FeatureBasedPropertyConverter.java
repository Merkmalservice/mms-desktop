package at.researchstudio.sat.merkmalservice.ifc.convert.support.propertyvalue;

import at.researchstudio.sat.merkmalservice.ifc.ParsedIfcFile;
import at.researchstudio.sat.merkmalservice.ifc.support.ProjectUnits;
import at.researchstudio.sat.merkmalservice.model.ifc.IfcUnit;
import at.researchstudio.sat.merkmalservice.model.mapping.feature.Feature;
import at.researchstudio.sat.merkmalservice.model.mapping.feature.featuretype.NumericFeatureType;
import at.researchstudio.sat.merkmalservice.model.mapping.feature.featuretype.StringFeatureType;
import at.researchstudio.sat.merkmalservice.model.qudt.QuantityValue;
import at.researchstudio.sat.merkmalservice.model.qudt.Qudt;
import at.researchstudio.sat.merkmalservice.model.qudt.Unit;
import at.researchstudio.sat.merkmalservice.qudtifc.QudtIfcMapper;
import at.researchstudio.sat.merkmalservice.support.exception.UnsupportedTypeConversionException;
import at.researchstudio.sat.merkmalservice.vocab.ifc.IfcPropertyType;
import java.lang.invoke.MethodHandles;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static at.researchstudio.sat.merkmalservice.ifc.support.IfcUtils.toStepValue;

public class FeatureBasedPropertyConverter implements PropertyConverter {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final Feature inputFeature;
    private final Feature outputFeature;
    private BiFunction<StepValueAndTypeAndIfcUnit, ParsedIfcFile, StepValueAndTypeAndIfcUnit>
            conversionFunction;

    public FeatureBasedPropertyConverter(Feature inputFeature, Feature outputFeature) {
        this.inputFeature = inputFeature;
        this.outputFeature = outputFeature;
        this.conversionFunction = generateConversionFunction(inputFeature, outputFeature);
    }

    private BiFunction<StepValueAndTypeAndIfcUnit, ParsedIfcFile, StepValueAndTypeAndIfcUnit>
            generateConversionFunction(Feature inputFeature, Feature outputFeature) {
        // identical in/out types
        if (inputFeature.getType().equals(outputFeature.getType())) {
            return (vat, parsedIfcFile) ->
                    new StepValueAndTypeAndIfcUnit(
                            new StepValueAndType(
                                    vat.getStepValueAndType().getValue(),
                                    vat.getStepValueAndType().getType()),
                            vat.getIfcUnit());
        }
        // output type string: just do a toString
        if (outputFeature.getType() instanceof StringFeatureType) {
            return (vat, parsedIfcFile) -> {
                String strval = vat.getStepValueAndType().getValue().toString();
                String type =
                        strval.length() > 255
                                ? IfcPropertyType.TEXT.getStepTypeName()
                                : IfcPropertyType.LABEL.getStepTypeName();
                return new StepValueAndTypeAndIfcUnit(
                        new StepValueAndType(strval, type), vat.getIfcUnit());
            };
        }
        // input and output type numeric, different units/quantity kinds: convert
        if (outputFeature.getType() instanceof NumericFeatureType
                && inputFeature.getType() instanceof NumericFeatureType) {
            return (vat, parsedIfcFile) -> {
                double value;
                if (!(vat.getStepValueAndType().getValue() instanceof Number)) {
                    throw new UnsupportedTypeConversionException(
                            String.format(
                                    "Cannot convert value %s from input feature %s to output feature %s: value is not a number",
                                    vat.toString(),
                                    inputFeature.getName(),
                                    outputFeature.getName()));
                }
                value = ((Number) vat.getStepValueAndType().getValue()).doubleValue();

                Set<Unit> toConvertUnits = new HashSet<>();
                if (vat.getIfcUnit() != null) {
                    QudtIfcMapper.mapIfcUnitToQudtUnit(vat.getIfcUnit());
                } else {
                    //fallback to inputfeature unit
                    toConvertUnits.add(Qudt.unit(((NumericFeatureType) inputFeature.getType()).getUnit().getId()));
                }
                toConvertUnits = toConvertUnits.stream().filter(unit -> ! unit.equals(Qudt.Units.UNITLESS)).collect(Collectors.toSet());
                if (toConvertUnits.isEmpty()) {
                    logger.info(
                                    "No qudt unit found to interpret input data of inputFeature '{}' to outputFeature '{}', interpreting as unitless input", inputFeature.getName(), outputFeature.getName());
                }
                if (toConvertUnits.size() > 1) {
                    logger.info(
                            "Multiple qudt unit choices for ifc unit {}: {}",
                            vat.getIfcUnit(),
                            toConvertUnits.stream()
                                    .map(Object::toString)
                                    .collect(Collectors.joining(", ")));
                }

                QuantityValue converted = null;
                if (toConvertUnits.isEmpty()) {
                    //just use outputfeature unit, no conversion
                    Unit convertedUnit = Qudt.unit(((NumericFeatureType) outputFeature.getType()).getUnit().getId());
                    converted = Qudt.quantityValue(value, convertedUnit);
                } else {
                    QuantityValue toConvert =
                                    Qudt.quantityValue(value, toConvertUnits.stream().findFirst().get());
                    converted =
                                    Qudt.convert(
                                                    toConvert,
                                                    ((NumericFeatureType) outputFeature.getType()).getUnit().getId());
                }
                IfcUnit convertedIfcUnit = QudtIfcMapper.mapQudtUnitToIfcUnit(converted.getUnit());
                parsedIfcFile.addIfcUnit(convertedIfcUnit);
                return new StepValueAndTypeAndIfcUnit(
                        new StepValueAndType(
                                converted.getValue(),
                                QudtIfcMapper.getIfcUnitType(converted.getUnit()).toString()),
                        convertedIfcUnit);
            };
        }
        throw new UnsupportedTypeConversionException(
                String.format(
                        "Cannot convert property values from input feature %s (type: %s) to outputFeature %s (type: %s)",
                        inputFeature.getName(),
                        inputFeature.getType(),
                        outputFeature.getName(),
                        outputFeature.getType()));
    }

    private Predicate<StepValueAndTypeAndIfcUnit> generateAllowedInputFeatureValuePredicate(
            Feature inputFeature, ProjectUnits projectUnits) {
        return null;
    }

    @Override
    public StepValueAndTypeAndIfcUnit convert(
            StepValueAndTypeAndIfcUnit stepValueAndTypeAndIfcUnit, ParsedIfcFile parsedIfcFile) {

        return conversionFunction.apply(stepValueAndTypeAndIfcUnit, parsedIfcFile);
    }
}
