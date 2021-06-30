package at.researchstudio.sat.mmsdesktop.util;

import at.researchstudio.sat.merkmalservice.vocab.qudt.QudtQuantityKind;
import at.researchstudio.sat.merkmalservice.vocab.qudt.QudtUnit;
import at.researchstudio.sat.mmsdesktop.model.ifc.IfcProperty;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
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
}
