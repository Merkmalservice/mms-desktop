package at.researchstudio.sat.mmsdesktop.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.springframework.util.ResourceUtils;

public class Utils {
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
}
