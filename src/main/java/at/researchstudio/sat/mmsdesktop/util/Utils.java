package at.researchstudio.sat.mmsdesktop.util;

import at.researchstudio.sat.merkmalservice.vocab.qudt.QudtQuantityKind;
import at.researchstudio.sat.merkmalservice.vocab.qudt.QudtUnit;
import at.researchstudio.sat.mmsdesktop.model.ifc.IfcProperty;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

public class Utils {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static final Pattern UNICODE_REPLACEMENT_PATTERn =
            Pattern.compile("\\\\X2\\\\(.*?)\\\\X0\\\\", Pattern.CASE_INSENSITIVE);

    public static String readFileToString(String path) throws IOException {
        return FileUtils.readFileToString(ResourceUtils.getFile(path), StandardCharsets.UTF_8);
    }

    /**
     * Converts any given String occuring in an ifc-file to utf-8 (replace \X2\*\X0\ with the
     * correct special character
     */
    public static String convertIFCStringToUtf8(String s) {
        Matcher matcher = UNICODE_REPLACEMENT_PATTERn.matcher(s);

        return matcher.replaceAll(
                replacer -> {
                    String hexValue = replacer.group().substring(4, 8);
                    int charValue = Integer.parseInt(hexValue, 16);

                    return Character.toString((char) charValue);
                });
    }

    public static String extractQudtUnitFromProperty(IfcProperty property) {
        try {
            return QudtUnit.extractUnitFromPrefixAndMeasure(
                    property.getMeasurePrefix(), property.getMeasure());
        } catch (IllegalArgumentException e) {
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
        } catch (IllegalArgumentException e) {
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
}
