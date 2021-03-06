package at.researchstudio.sat.merkmalservice.ifc.support;

import static at.researchstudio.sat.merkmalservice.ifc.model.TypeConverter.castToOpt;

import at.researchstudio.sat.merkmalservice.ifc.ParsedIfcFile;
import at.researchstudio.sat.merkmalservice.ifc.model.IfcLine;
import at.researchstudio.sat.merkmalservice.ifc.model.IfcPropertyEnumeratedValueLine;
import at.researchstudio.sat.merkmalservice.ifc.model.IfcPropertySingleValueLine;
import at.researchstudio.sat.merkmalservice.ifc.model.IfcQuantityLine;
import at.researchstudio.sat.merkmalservice.utils.Utils;
import at.researchstudio.sat.merkmalservice.vocab.ifc.IfcPropertyType;
import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class IfcLinePredicates {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static Predicate<IfcLine> isPropertyWithName(String name) {
        Objects.requireNonNull(name);
        return ifcSinglePropertyValueLinePredicate(
                prop -> name.equals(Utils.convertIFCStringToUtf8(prop.getName())));
    }

    public static Predicate<IfcLine> isEnumValueWithName(String name) {
        Objects.requireNonNull(name);
        return ifcPropertyEnumeratedValueLinePredicate(
                enumVal -> name.equals(Utils.convertIFCStringToUtf8(enumVal.getName())));
    }

    public static Predicate<IfcLine> isQuantityWithName(String name) {
        Objects.requireNonNull(name);
        return ifcQuantityLinePredicate(
                quantity -> name.equals(Utils.convertIFCStringToUtf8(quantity.getName())));
    }

    public static Predicate<IfcLine> isPropertyWithNameMatching(String regex) {
        Objects.requireNonNull(regex);
        Pattern p = Pattern.compile(regex);
        return ifcSinglePropertyValueLinePredicate(
                prop -> p.matcher(Utils.convertIFCStringToUtf8(prop.getName())).matches());
    }

    public static Predicate<IfcLine> isEnumValueWithNameMatching(String regex) {
        Objects.requireNonNull(regex);
        Pattern p = Pattern.compile(regex);
        return ifcPropertyEnumeratedValueLinePredicate(prop -> p.matcher(prop.getName()).matches());
    }

    public static Predicate<IfcLine> isQuantityWithNameMatching(String regex) {
        Objects.requireNonNull(regex);
        Pattern p = Pattern.compile(regex);
        return ifcQuantityLinePredicate(prop -> p.matcher(prop.getName()).matches());
    }

    public static Predicate<IfcLine> ifcSinglePropertyValueLinePredicate(
            Predicate<IfcPropertySingleValueLine> predicate) {
        Objects.requireNonNull(predicate);
        return line -> {
            if (line instanceof IfcPropertySingleValueLine) {
                IfcPropertySingleValueLine sp = (IfcPropertySingleValueLine) line;
                return predicate.test(sp);
            }
            return false;
        };
    }

    public static Predicate<IfcLine> ifcPropertyEnumeratedValueLinePredicate(
            Predicate<IfcPropertyEnumeratedValueLine> predicate) {
        Objects.requireNonNull(predicate);
        return line -> {
            if (line instanceof IfcPropertyEnumeratedValueLine) {
                IfcPropertyEnumeratedValueLine sp = (IfcPropertyEnumeratedValueLine) line;
                return predicate.test(sp);
            }
            return false;
        };
    }

    public static Predicate<IfcLine> ifcQuantityLinePredicate(
            Predicate<IfcQuantityLine> predicate) {
        Objects.requireNonNull(predicate);
        return line -> {
            if (line instanceof IfcQuantityLine) {
                return predicate.test((IfcQuantityLine) line);
            }
            return false;
        };
    }

    public static BiPredicate<ParsedIfcFile, IfcLine> isStringElementValueWithValuePredicate(
            IfcElementValueExtractor extractor, Predicate<String> valueMatcher) {
        Objects.requireNonNull(valueMatcher);
        return (model, line) -> {
            return extractor
                    .apply(model, line)
                    .map(o -> o.getStepValueAndType().getValue().toString())
                    .map(valueMatcher::test)
                    .orElse(false);
        };
    }

    public static BiPredicate<ParsedIfcFile, IfcLine> isStringElementValueWithValue(
            IfcElementValueExtractor extractor, String stringValue) {
        Objects.requireNonNull(stringValue);
        return isStringElementValueWithValuePredicate(extractor, stringValue::equals);
    }

    public static BiPredicate<ParsedIfcFile, IfcLine> isStringElementValueWithDifferentValue(
            IfcElementValueExtractor extractor, String stringValue) {
        Objects.requireNonNull(stringValue);
        return isStringElementValueWithValuePredicate(
                extractor, actualString -> !stringValue.equals(actualString));
    }

    public static BiPredicate<ParsedIfcFile, IfcLine> isStringElementValueWithValueContaining(
            IfcElementValueExtractor extractor, String subString) {
        Objects.requireNonNull(subString);
        return isStringElementValueWithValuePredicate(
                extractor, s -> s != null && s.contains(subString));
    }

    public static BiPredicate<ParsedIfcFile, IfcLine> isStringElementValueWithValueNotContaining(
            IfcElementValueExtractor extractor, String subString) {
        Objects.requireNonNull(subString);
        return isStringElementValueWithValuePredicate(
                extractor, s -> s != null && !s.contains(subString));
    }

    public static BiPredicate<ParsedIfcFile, IfcLine> isStringElementValueWithValueMatching(
            IfcElementValueExtractor extractor, String regex) {
        Objects.requireNonNull(regex);
        return isStringElementValueWithValueMatchingPattern(extractor, Pattern.compile(regex));
    }

    public static BiPredicate<ParsedIfcFile, IfcLine> isStringElementValueWithValueMatchingPattern(
            IfcElementValueExtractor extractor, Pattern regex) {
        Objects.requireNonNull(regex);
        return isStringElementValueWithValuePredicate(extractor, s -> regex.matcher(s).matches());
    }

    public static Predicate<IfcLine> isStringPropertyWithValuePredicate(
            Predicate<String> valueMatcher) {
        Objects.requireNonNull(valueMatcher);
        return line ->
                castToOpt(line, IfcPropertySingleValueLine.class)
                        .map(IfcLinePredicates::getStringValue)
                        .map(valueMatcher::test)
                        .orElse(false);
    }

    public static Predicate<IfcLine> isStringPropertyWithValue(String stringValue) {
        Objects.requireNonNull(stringValue);
        return isStringPropertyWithValuePredicate(stringValue::equals);
    }

    public static Predicate<IfcLine> isStringPropertyWithValueContaining(String subString) {
        Objects.requireNonNull(subString);
        return isStringPropertyWithValuePredicate(s -> s != null && s.contains(subString));
    }

    public static Predicate<IfcLine> isStringPropertyWithValueNotContaining(String subString) {
        Objects.requireNonNull(subString);
        return isStringPropertyWithValuePredicate(s -> s != null && !s.contains(subString));
    }

    public static Predicate<IfcLine> isStringPropertyWithValueMatching(String regex) {
        Objects.requireNonNull(regex);
        return isStringPropertyWithValueMatchingPattern(Pattern.compile(regex));
    }

    public static Predicate<IfcLine> isStringPropertyWithValueMatchingPattern(Pattern regex) {
        Objects.requireNonNull(regex);
        return isStringPropertyWithValuePredicate(s -> regex.matcher(s).matches());
    }

    public static Predicate<IfcLine> isIntegerPropertyWithValuePredicate(
            Predicate<Long> valueMatcher) {
        Objects.requireNonNull(valueMatcher);
        return line ->
                castToOpt(line, IfcPropertySingleValueLine.class)
                        .map(IfcLinePredicates::getIntegerValue)
                        .map(valueMatcher::test)
                        .orElse(false);
    }

    public static Predicate<IfcLine> isIntegerPropertyWithValueEqualTo(long value) {
        return isIntegerPropertyWithValuePredicate(v -> v == value);
    }

    public static Predicate<IfcLine> isIntegerPropertyWithValueLessThan(long value) {
        return isIntegerPropertyWithValuePredicate(v -> v < value);
    }

    public static Predicate<IfcLine> isIntegerPropertyWithValueLessThanOrEqualTo(long value) {
        return isIntegerPropertyWithValuePredicate(v -> v <= value);
    }

    public static Predicate<IfcLine> isRealPropertyWithValuePredicate(
            Predicate<Double> valueMatcher) {
        Objects.requireNonNull(valueMatcher);
        return line ->
                castToOpt(line, IfcPropertySingleValueLine.class)
                        .map(IfcLinePredicates::getRealValue)
                        .map(valueMatcher::test)
                        .orElse(false);
    }

    public static Predicate<IfcLine> isRealPropertyWithValueEqualTo(double value) {
        return isRealPropertyWithValuePredicate(v -> value == v);
    }

    public static Predicate<IfcLine> isRealPropertyWithValueLessThan(double value) {
        return isRealPropertyWithValuePredicate(v -> v < value);
    }

    public static Predicate<IfcLine> isRealPropertyWithValueLessThanOrEqualTo(double value) {
        return isRealPropertyWithValuePredicate(v -> v <= value);
    }

    public static Predicate<IfcLine> isBooleanPropertyWithValuePredicate(
            Predicate<Boolean> valueMatcher) {
        Objects.requireNonNull(valueMatcher);
        return line ->
                castToOpt(line, IfcPropertySingleValueLine.class)
                        .map(IfcLinePredicates::getBooleanValue)
                        .map(valueMatcher::test)
                        .orElse(false);
    }

    public static Predicate<IfcLine> valueEquals(String value) {
        Objects.requireNonNull(value);
        return isStringPropertyWithValuePredicate(value::equals);
    }

    public static Predicate<IfcLine> valueEquals(Double value) {
        Objects.requireNonNull(value);
        return isRealPropertyWithValuePredicate(value::equals);
    }

    public static Predicate<IfcLine> valueEquals(Long value) {
        Objects.requireNonNull(value);
        return isIntegerPropertyWithValuePredicate(value::equals);
    }

    public static Predicate<IfcLine> valueEquals(Boolean value) {
        Objects.requireNonNull(value);
        return isBooleanPropertyWithValuePredicate(value::equals);
    }

    public static <T> Predicate<IfcLine> valueEquals(T value) {
        Objects.requireNonNull(value);
        if (value instanceof String) {
            return valueEquals((String) value);
        }
        if (value instanceof Double) {
            return valueEquals((Double) value);
        }
        if (value instanceof Float) {
            return valueEquals(((Float) value).doubleValue());
        }
        if (value instanceof Integer) {
            return valueEquals(((Integer) value).longValue());
        }
        if (value instanceof Long) {
            return valueEquals((Long) value);
        }
        if (value instanceof Boolean) {
            return valueEquals((Boolean) value);
        }
        logger.info(
                "Cannot generate 'equals' predicate for value of type {}",
                value.getClass().getName());
        return line -> false;
    }

    public static <T> Predicate<IfcLine> isNumericPropertyWithValueLessThanOrEqualTo(T value) {
        Objects.requireNonNull(value);
        if (value instanceof Long) {
            return isIntegerPropertyWithValueLessThanOrEqualTo((Long) value);
        }
        if (value instanceof Integer) {
            return isIntegerPropertyWithValueLessThanOrEqualTo(((Integer) value).longValue());
        }
        if (value instanceof Double) {
            return isRealPropertyWithValueLessThanOrEqualTo((Double) value);
        }
        if (value instanceof Float) {
            return isRealPropertyWithValueLessThanOrEqualTo(((Float) value).doubleValue());
        }
        logger.info(
                "Cannot generate 'lessThanOrEqualTo' predicate for value of type {}",
                value.getClass().getName());
        return line -> false;
    }

    public static <T> Predicate<IfcLine> isNumericPropertyWithValueLessThan(T value) {
        Objects.requireNonNull(value);
        if (value instanceof Integer) {
            return isIntegerPropertyWithValueLessThan(((Integer) value).longValue());
        }
        if (value instanceof Long) {
            return isIntegerPropertyWithValueLessThan((Long) value);
        }
        if (value instanceof Double) {
            return isRealPropertyWithValueLessThan((Double) value);
        }
        if (value instanceof Float) {
            return isRealPropertyWithValueLessThan(((Float) value).doubleValue());
        }
        logger.info(
                "Cannot generate 'lessThanOrEqualTo' predicate for value of type {}",
                value.getClass().getName());
        return line -> false;
    }

    public static Predicate<IfcLine> isProperty() {
        return line ->
                line instanceof IfcPropertySingleValueLine || line instanceof IfcQuantityLine;
    }

    public static Predicate<IfcLine> isRealProperty() {
        return line ->
                castToOpt(line, IfcQuantityLine.class).isPresent()
                        || castToOpt(line, IfcPropertySingleValueLine.class)
                                .map(IfcLinePredicates::isRealValue)
                                .orElse(false);
    }

    public static Predicate<IfcLine> isIntegerProperty() {
        return line ->
                castToOpt(line, IfcPropertySingleValueLine.class)
                        .map(IfcLinePredicates::isIntegerValue)
                        .orElse(false);
    }

    public static Predicate<IfcLine> isStringProperty() {
        return line ->
                castToOpt(line, IfcPropertySingleValueLine.class)
                        .map(IfcLinePredicates::isStringValue)
                        .orElse(false);
    }

    public static Predicate<IfcLine> isBooleanProperty() {
        return line ->
                castToOpt(line, IfcPropertySingleValueLine.class)
                        .map(IfcLinePredicates::isBooleanValue)
                        .orElse(false);
    }

    public static Predicate<IfcLine> isNumericProperty() {
        return line ->
                castToOpt(line, IfcQuantityLine.class).isPresent()
                        || castToOpt(line, IfcPropertySingleValueLine.class)
                                .map(l -> isRealValue(l) || isIntegerValue(l))
                                .orElse(false);
    }

    public static Predicate<IfcLine> isIfcLineType(Class<? extends IfcLine> type) {
        return line -> type.isAssignableFrom(line.getClass());
    }

    private static boolean isRealValue(IfcPropertySingleValueLine p) {
        return IfcPropertyType.fromString(p.getType()).isMeasureType()
                || IfcPropertyType.isOneOf(
                        p.getType(), IfcPropertyType.REAL, IfcPropertyType.EXPRESS_REAL);
    }

    private static Double getRealValue(IfcPropertySingleValueLine line) {
        return Optional.ofNullable(line)
                .filter(IfcLinePredicates::isRealValue)
                .map(IfcPropertySingleValueLine::getValue)
                .map(Double::valueOf)
                .orElse(null);
    }

    private static Double getRealValue(IfcQuantityLine line) {
        return Optional.ofNullable(line)
                .filter(isRealProperty())
                .map(IfcQuantityLine::getValue)
                .orElse(null);
    }

    private static boolean isIntegerValue(IfcPropertySingleValueLine p) {
        return IfcPropertyType.isOneOf(
                p.getType(),
                IfcPropertyType.INTEGER,
                IfcPropertyType.POSITIVE_INTEGER,
                IfcPropertyType.EXPRESS_INTEGER);
    }

    private static Long getIntegerValue(IfcPropertySingleValueLine line) {
        return Optional.ofNullable(line)
                .filter(IfcLinePredicates::isIntegerValue)
                .map(IfcPropertySingleValueLine::getValue)
                .map(Long::valueOf)
                .orElse(null);
    }

    private static String getStringValue(IfcPropertySingleValueLine line) {
        return Optional.ofNullable(line)
                .filter(IfcLinePredicates::isStringValue)
                .map(IfcPropertySingleValueLine::getValue)
                .orElse(null);
    }

    private static boolean isStringValue(IfcPropertySingleValueLine p) {
        return IfcPropertyType.isOneOf(
                p.getType(),
                IfcPropertyType.TEXT,
                IfcPropertyType.LABEL,
                IfcPropertyType.IDENTIFIER);
    }

    private static boolean isBooleanValue(IfcPropertySingleValueLine p) {
        return IfcPropertyType.isOneOf(
                p.getType(),
                IfcPropertyType.EXPRESS_BOOL,
                IfcPropertyType.BOOL,
                IfcPropertyType.LOGICAL);
    }

    private static Boolean getBooleanValue(IfcPropertySingleValueLine line) {
        return Optional.ofNullable(line)
                .filter(p -> IfcPropertyType.is(p.getType(), IfcPropertyType.BOOL))
                .map(IfcPropertySingleValueLine::getValue)
                .map(".T."::equals)
                .or(
                        () ->
                                Optional.ofNullable(line)
                                        .filter(
                                                p ->
                                                        IfcPropertyType.isOneOf(
                                                                p.getType(),
                                                                IfcPropertyType.EXPRESS_BOOL,
                                                                IfcPropertyType.LOGICAL))
                                        .map(IfcPropertySingleValueLine::getValue)
                                        .map(Boolean::valueOf))
                .orElse(null);
    }
}
