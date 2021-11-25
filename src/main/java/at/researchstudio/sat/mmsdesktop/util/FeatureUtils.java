package at.researchstudio.sat.mmsdesktop.util;

import at.researchstudio.sat.merkmalservice.model.Feature;
import at.researchstudio.sat.mmsdesktop.model.ifc.IfcLine;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

public class FeatureUtils {
    public static boolean isFeatureWithinLine(Feature feature, IfcLine ifcLine) {
        if (Objects.isNull(feature) || StringUtils.isEmpty(feature.getName())) return false;

        String translatedName =
                at.researchstudio.sat.merkmalservice.utils.Utils.convertUtf8ToIFCString(
                        feature.getName());
        return ifcLine.getLine().contains("'" + translatedName + "'");
    }
}
