package at.researchstudio.sat.mmsdesktop.util;

import at.researchstudio.sat.merkmalservice.ifc.support.IfcUtils;
import java.text.MessageFormat;
import java.util.ResourceBundle;

public class MessageUtils {

    public static String getKeyWithParameters(
            ResourceBundle resourceBundle, String key, Object... values) {
        return MessageFormat.format(resourceBundle.getString(key), values);
    }

    public static String getKeyForUnit(ResourceBundle resourceBundle, String unit) {
        return getKeyForUri(resourceBundle, "unit.", unit);
    }

    public static String getKeyForQuantityKind(ResourceBundle resourceBundle, String quantityKind) {
        return getKeyForUri(resourceBundle, "quantitykind.", quantityKind);
    }

    private static String getKeyForUri(ResourceBundle resourceBundle, String prefix, String uri) {
        return IfcUtils.executeOrDefaultOnException(
                () -> resourceBundle.getString(prefix + uri.substring(uri.lastIndexOf("/") + 1)),
                uri);
    }
}
