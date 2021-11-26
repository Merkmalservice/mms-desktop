package at.researchstudio.sat.merkmalservice.ifc;

import at.researchstudio.sat.merkmalservice.ifc.model.IfcVersion;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IfcFileWrapper extends FileWrapper {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final IfcVersion ifcVersion;

    public IfcFileWrapper(File file) {
        super(file);
        this.ifcVersion = extractIFCVersionFromFile(file);
    }

    public IfcVersion getIfcVersion() {
        return ifcVersion;
    }

    private IfcVersion extractIFCVersionFromFile(File file) {
        try (LineIterator it = FileUtils.lineIterator(file, StandardCharsets.UTF_8.toString())) {
            while (it.hasNext()) {
                String line = it.nextLine();
                if (line.contains("FILE_SCHEMA")) {
                    String versionString = line.substring(14, line.indexOf(")") - 1);
                    logger.debug(
                            "File: "
                                    + file.getAbsolutePath()
                                    + " is in IfcVersion: "
                                    + versionString);
                    return IfcVersion.valueOf(versionString);
                }
            }
        } catch (IOException e) {
            // TODO: handle this
        }

        return IfcVersion.UNKNOWN;
    }
}
