package at.researchstudio.sat.mmsdesktop.util;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.Objects;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileWrapper {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final File file;
    private final String name;
    private final String path;

    public FileWrapper(File file) {
        this.file = file;
        this.name = file.getName();
        this.path = file.getAbsolutePath();
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

    public String getFileType() {
        if (this instanceof IfcFileWrapper) {
            return ((IfcFileWrapper) this).getIfcVersion().toString();
        } else {
            return FilenameUtils.getExtension(file.getAbsolutePath());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileWrapper that = (FileWrapper) o;
        return Objects.equals(name, that.name) && Objects.equals(path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, path);
    }
}
