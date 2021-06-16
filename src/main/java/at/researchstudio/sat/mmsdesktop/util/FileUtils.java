package at.researchstudio.sat.mmsdesktop.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.io.filefilter.SuffixFileFilter;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.lang.invoke.MethodHandles;
import java.nio.file.NotDirectoryException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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
    if (directory == null) {
      throw new FileNotFoundException("Directory does not exist");
    }

    if (directory.exists()) {
      if (directory.isFile()) {
        logger.error(
            "Specified Directory '" + directory.getName() + "' is a File use command file instead");
        throw new NotDirectoryException(
            "Specified File " + directory.getName() + " is not a Directory");
      } else {
        logger.debug("Searching for IFC Files within Directory: " + directory.getAbsolutePath());
        return Arrays.asList(
            Objects.requireNonNull(directory.listFiles((FileFilter) new SuffixFileFilter(".ifc"))));
      }
    } else {
      logger.error("Specified Directory '" + directory.getName() + "' does not exist");
      throw new FileNotFoundException("Directory does not exist");
    }
  }
}
