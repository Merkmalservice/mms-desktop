package at.researchstudio.sat.mmsdesktop.util;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.lang.invoke.MethodHandles;
import java.nio.file.NotDirectoryException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtils {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Extracts all IFC Files from a given Directory
     *
     * @param directory specified Directory
     * @return All IFC-Files within the given directory
     * @throws FileNotFoundException when directory is null or does not exist
     * @throws NotDirectoryException when directory is not a directory
     */
    public static List<File> getIfcFilesFromDirectory(File directory)
            throws FileNotFoundException, NotDirectoryException {
        return getFilesFromDirectory(directory, ".ifc");
    }

    /**
     * Returns true if given file has ifc extension
     *
     * @param file
     * @return true if file is an ifc file, false if it isnt
     */
    public static boolean isIfcFile(File file) {
        return FilenameUtils.getExtension(file.getAbsolutePath()).equals("ifc");
    }

    /**
     * Extracts all valid Files from a given Directory (currently ifc and json are Valid Extraction
     * files)
     *
     * @param directory specified Directory
     * @return All IFC/Json-Files within the given directory
     * @throws FileNotFoundException when directory is null or does not exist
     * @throws NotDirectoryException when directory is not a directory
     */
    public static List<File> getValidExtractionFilesFromDirectory(File directory)
            throws FileNotFoundException, NotDirectoryException {
        return getFilesFromDirectory(directory, ".ifc", ".json");
    }

    /**
     * Extracts all JSON Files from a given Directory
     *
     * @param directory specified Directory
     * @return All JSON-Files within the given directory
     * @throws FileNotFoundException when directory is null or does not exist
     * @throws NotDirectoryException when directory is not a directory
     */
    public static List<File> getJsonFilesFromDirectory(File directory)
            throws FileNotFoundException, NotDirectoryException {
        return getFilesFromDirectory(directory, ".json");
    }

    /**
     * Extracts all Files from a given Directory
     *
     * @param directory specified Directory
     * @param suffix specified file such as ".ifc"
     * @return All Files within the given directory that have a certain suffix
     * @throws FileNotFoundException when directory is null or does not exist
     * @throws NotDirectoryException when directory is not a directory
     */
    private static List<File> getFilesFromDirectory(File directory, String... suffix)
            throws FileNotFoundException, NotDirectoryException {
        if (directory == null) {
            throw new FileNotFoundException("Directory does not exist");
        }

        if (directory.exists()) {
            if (directory.isFile()) {
                logger.error(
                        "Specified Directory '"
                                + directory.getName()
                                + "' is a File use command file instead");
                throw new NotDirectoryException(
                        "Specified File " + directory.getName() + " is not a Directory");
            } else {
                logger.debug(
                        "Searching for IFC Files within Directory: " + directory.getAbsolutePath());
                return Arrays.asList(
                        Objects.requireNonNull(
                                directory.listFiles((FileFilter) new SuffixFileFilter(suffix))));
            }
        } else {
            logger.error("Specified Directory '" + directory.getName() + "' does not exist");
            throw new FileNotFoundException("Directory does not exist");
        }
    }
}
