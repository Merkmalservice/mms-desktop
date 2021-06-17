package at.researchstudio.sat.mmsdesktop.util;

import at.researchstudio.sat.mmsdesktop.model.ifc.IfcVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.Scanner;

public class IfcFileWrapper {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final File file;
    private final String name;
    private final String path;
    private final IfcVersion ifcVersion;

    public IfcFileWrapper(File file) {
        this.file = file;
        this.name = file.getName();
        this.path = file.getAbsolutePath();
        this.ifcVersion = extractIFCVersionFromFile(file);
    }

    public File getFile() {
        return file;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public IfcVersion getIfcVersion() {
        return ifcVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IfcFileWrapper that = (IfcFileWrapper) o;
        return Objects.equals(name, that.name) &&
               Objects.equals(path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, path);
    }

    private IfcVersion extractIFCVersionFromFile(File file) {
        try {
            Scanner scanner = new Scanner(file);
            //now read the file line by line...
            int lineNum = 0;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                lineNum++;
                if (line.contains("FILE_SCHEMA")) {
                    String versionString = line.substring(14, line.indexOf(")") - 1);
                    logger.debug("File: "+file.getAbsolutePath()+" is in IfcVersion: " + versionString);
                    return IfcVersion.valueOf(versionString);
                }
            }
        } catch (FileNotFoundException e) {
            //TODO: handle this
        }
        return IfcVersion.UNKNOWN;
    }
}
