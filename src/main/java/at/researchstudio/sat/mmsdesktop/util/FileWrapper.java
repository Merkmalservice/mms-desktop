package at.researchstudio.sat.mmsdesktop.util;

import java.io.File;
import java.util.Objects;

public class FileWrapper {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileWrapper that = (FileWrapper) o;
        return Objects.equals(name, that.name) &&
               Objects.equals(path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, path);
    }
}
