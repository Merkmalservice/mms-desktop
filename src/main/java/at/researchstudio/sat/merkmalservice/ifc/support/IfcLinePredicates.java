package at.researchstudio.sat.merkmalservice.ifc.support;

import at.researchstudio.sat.merkmalservice.ifc.model.IfcLine;
import at.researchstudio.sat.merkmalservice.ifc.model.IfcPropertyEnumeratedValueLine;
import at.researchstudio.sat.merkmalservice.ifc.model.IfcQuantityLine;
import at.researchstudio.sat.merkmalservice.ifc.model.IfcSinglePropertyValueLine;
import at.researchstudio.sat.merkmalservice.utils.Utils;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class IfcLinePredicates {

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
            Predicate<IfcSinglePropertyValueLine> predicate) {
        Objects.requireNonNull(predicate);
        return line -> {
            if (line instanceof IfcSinglePropertyValueLine) {
                IfcSinglePropertyValueLine sp = (IfcSinglePropertyValueLine) line;
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
}
