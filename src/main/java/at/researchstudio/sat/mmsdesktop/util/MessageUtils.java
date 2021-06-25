package at.researchstudio.sat.mmsdesktop.util;

import java.text.MessageFormat;
import java.util.ResourceBundle;

public class MessageUtils {

    public static String getKeyWithParameters(
            ResourceBundle resourceBundle, String key, Object... values) {
        return MessageFormat.format(resourceBundle.getString(key), values);
    }
}
