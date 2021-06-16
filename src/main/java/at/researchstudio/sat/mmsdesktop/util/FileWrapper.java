package at.researchstudio.sat.mmsdesktop.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Objects;
import java.util.Scanner;

public class FileWrapper {
    private final File file;
    private final String name;
    private final String path;
    private final String ifcVersion;

    public String getIfcVersion() {
        return ifcVersion;
    }

    public FileWrapper(File file) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        FileWrapper that = (FileWrapper) o;
        return Objects.equals(name, that.name) &&
                        Objects.equals(path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, path);
    }

    public static String extractIFCVersionFromFile(File file) {
        try {
            Scanner scanner = new Scanner(file);
            //now read the file line by line...
            int lineNum = 0;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                lineNum++;
                if (line.contains("FILE_SCHEMA")) {
                    String versionString = line.substring(14, line.indexOf(")") - 1);
                    System.out.println("Version: " + versionString);
                    return versionString;
                }
            }
        } catch (FileNotFoundException e) {
            //handle this
        }
        return "42";
    }
}
