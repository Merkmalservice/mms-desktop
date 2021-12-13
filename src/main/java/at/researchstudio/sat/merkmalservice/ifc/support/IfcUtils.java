package at.researchstudio.sat.merkmalservice.ifc.support;

import static at.researchstudio.sat.merkmalservice.utils.Utils.convertUtf8ToIFCString;
import static java.util.stream.Collectors.joining;

import at.researchstudio.sat.merkmalservice.model.NumericFeature;
import at.researchstudio.sat.merkmalservice.model.ifc.IfcProperty;
import at.researchstudio.sat.merkmalservice.vocab.qudt.QudtQuantityKind;
import at.researchstudio.sat.merkmalservice.vocab.qudt.QudtUnit;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Supplier;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

public class IfcUtils {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static String readFileToString(String path) throws IOException {
        return FileUtils.readFileToString(ResourceUtils.getFile(path), StandardCharsets.UTF_8);
    }

    public static String extractQudtUnitFromProperty(IfcProperty property) {
        try {
            return QudtUnit.extractUnitFromIfcUnit(property.getUnit());
        } catch (IllegalArgumentException | NullPointerException e) {
            logger.warn(e.getMessage());
            logger.warn(
                    "Could not find QudtUnit for ifcProperty: "
                            + property
                            + ", returning UNITLESS("
                            + QudtUnit.UNITLESS
                            + ")");
            return QudtUnit.UNITLESS;
        }
    }

    public static String extractQudtQuantityKindFromProperty(IfcProperty property) {
        try {
            return QudtQuantityKind.extractQuantityKindFromPropertyName(property.getName());
        } catch (IllegalArgumentException | NullPointerException e) {
            logger.warn(e.getMessage());
            logger.error(
                    "Could not find QudtQuantityKind for ifcProperty: "
                            + property
                            + ", returning DIMENSIONLESS("
                            + QudtQuantityKind.DIMENSIONLESS
                            + ")");
            return QudtQuantityKind.DIMENSIONLESS;
        }
    }

    /**
     * Method that returns the Result of a given parameterless function, and returns a defaultValue
     * if a specified Type of exception is caught, if no specific Exceptions are passed, every
     * Exception will return the DefaultValue.
     *
     * @param execMethod method to execute
     * @param defaultValue defaultValue to be returned on Exception
     * @param returnDefaultOnException parameterList of exception classes where (if caught) the
     *     defaultValue is returned
     * @param <T> throws Exception (any exception that is not in the returnDefaultOnException
     *     parameterlist, none if no parameterlist is supplied
     * @return Result of execMethod or defaultValue
     */
    @SafeVarargs
    public static <T> T executeOrDefaultOnException(
            Supplier<? extends T> execMethod,
            T defaultValue,
            Class<? extends Throwable>... returnDefaultOnException) {
        Objects.requireNonNull(execMethod);
        try {
            return execMethod.get();
        } catch (Exception e) {
            if (ArrayUtils.isEmpty(returnDefaultOnException)) {
                return defaultValue;
            }
            for (Class<? extends Throwable> throwableClass : returnDefaultOnException) {
                if (throwableClass.isAssignableFrom(e.getClass())) {
                    return defaultValue;
                }
            }
            throw e;
        }
    }

    public static NumericFeature parseNumericFeature(
            IfcProperty ifcProperty, String quantityKind, String unit) {
        NumericFeature f = new NumericFeature(ifcProperty.getName(), quantityKind, unit);
        f.setUniqueValues(ifcProperty.getExtractedUniqueValues());
        f.setDescriptionFromUniqueValues(ifcProperty.getExtractedUniqueValues());
        return f;
    }

    public static NumericFeature parseNumericFeature(IfcProperty ifcProperty, String quantityKind) {
        return parseNumericFeature(
                ifcProperty, quantityKind, IfcUtils.extractQudtUnitFromProperty(ifcProperty));
    }

    public static NumericFeature parseNumericFeature(IfcProperty ifcProperty) {
        return parseNumericFeature(
                ifcProperty, IfcUtils.extractQudtQuantityKindFromProperty(ifcProperty));
    }

    public static String stacktraceToString(Throwable throwable) {
        StringWriter sw = new StringWriter();
        throwable.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    public static String toOptionalStepValue(Object o) {
        return o == null ? "$" : toStepValue(o);
    }

    public static String toStepValue(Object o) {
        Objects.requireNonNull(o);
        if (o instanceof String) {
            return "'" + convertUtf8ToIFCString((String) o) + "'";
        }
        if (o instanceof Boolean) {
            return (((Boolean) o).booleanValue()) ? ".T." : ".F.";
        }
        if (o instanceof Collection) {
            return ((Collection<?>) o)
                    .stream().map(IfcUtils::toStepValue).collect(joining(",", "(", ")"));
        } else return o.toString();
    }

    public static String toStepId(Integer id) {
        Objects.requireNonNull(id);
        return "#" + id.toString();
    }

    public static String toOptionalStepId(Integer id) {
        return id == null ? "$" : toStepId(id);
    }

    public static String toOptionalStepIds(Collection<Integer> id) {
        return id == null
                ? "$"
                : id.stream().map(IfcUtils::toStepId).collect(joining(",", "(", ")"));
    }

    public static String toStepToken(String s) {
        Objects.requireNonNull(s);
        return s;
    }
}
