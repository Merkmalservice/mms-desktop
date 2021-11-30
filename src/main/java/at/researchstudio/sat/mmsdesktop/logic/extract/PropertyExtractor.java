package at.researchstudio.sat.mmsdesktop.logic.extract;

import at.researchstudio.sat.merkmalservice.ifc.FileWrapper;
import at.researchstudio.sat.merkmalservice.ifc.IfcFileReader;
import at.researchstudio.sat.merkmalservice.ifc.IfcFileWrapper;
import at.researchstudio.sat.merkmalservice.ifc.ParsedIfcFile;
import at.researchstudio.sat.merkmalservice.model.Feature;
import at.researchstudio.sat.merkmalservice.support.progress.TaskProgressListener;
import at.researchstudio.sat.mmsdesktop.model.helper.FeatureSet;
import at.researchstudio.sat.mmsdesktop.model.task.ExtractResult;
import at.researchstudio.sat.mmsdesktop.support.MessageUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;
import javafx.concurrent.Task;

public class PropertyExtractor {
    public static Task<ExtractResult> generateExtractFilesToJsonTask(
            List<FileWrapper> extractFiles, final ResourceBundle resourceBundle) {
        return new Task<>() {
            @Override
            public ExtractResult call() {
                updateTitle(
                        MessageUtils.getKeyWithParameters(
                                resourceBundle, "label.extract.process.start"));
                StringBuilder logOutput = new StringBuilder();
                int convertedFileCount = 0;
                final int fileCount = extractFiles.size();
                final int max = fileCount * 2;
                List<Feature> extractedFeatures = new ArrayList<>();
                int extractedFeaturesCount = 0;
                Set<FeatureSet> extractedFeatureSets = new HashSet<>();
                int extractedFeatureSetsCount = 0;
                Map<Class<? extends FileWrapper>, List<FileWrapper>> extractFilesMap =
                        extractFiles.stream().collect(Collectors.groupingBy(FileWrapper::getClass));
                // Read Json Files
                int i = 0;
                List<FileWrapper> jsonExtractFiles =
                        extractFilesMap.getOrDefault(FileWrapper.class, Collections.emptyList());
                int jsonFileCount = jsonExtractFiles.size();
                if (jsonFileCount != 0) {
                    updateTitle(
                            MessageUtils.getKeyWithParameters(
                                    resourceBundle, "label.extract.process.startJson"));
                    for (FileWrapper extractFile : jsonExtractFiles) {
                        try {
                            logOutput
                                    .append("Reading ")
                                    .append(++i)
                                    .append("/")
                                    .append(jsonFileCount)
                                    .append(" from JSON File ")
                                    .append(extractFile.getPath())
                                    .append(": ");
                            List<Feature> extractedFeaturesFromJsonFile =
                                    at.researchstudio.sat.merkmalservice.utils.Utils.readFromJson(
                                            extractFile.getFile());
                            logOutput
                                    .append("Extracted ")
                                    .append(extractedFeaturesFromJsonFile.size())
                                    .append(" Features")
                                    .append(System.lineSeparator());
                            extractedFeatures.addAll(extractedFeaturesFromJsonFile);
                            extractedFeaturesCount += extractedFeaturesFromJsonFile.size();
                            convertedFileCount++;
                        } catch (Exception e) {
                            logOutput
                                    .append("Can't convert file, Reason: ")
                                    .append(e.getMessage())
                                    .append(System.lineSeparator());
                        }
                        if (isCancelled()) {
                            logOutput
                                    .append("Operation cancelled by User")
                                    .append(System.lineSeparator());
                            updateMessage(logOutput.toString());
                            break;
                        }
                        updateMessage(logOutput.toString());
                        updateProgress(convertedFileCount, fileCount);
                    }
                }
                updateTitle(
                        MessageUtils.getKeyWithParameters(
                                resourceBundle, "label.extract.process.startIfc"));
                List<FileWrapper> ifcExtractFiles =
                        extractFilesMap.getOrDefault(IfcFileWrapper.class, Collections.emptyList());
                final int ifcFileCount = ifcExtractFiles.size();
                TaskProgressListener taskProgressListener =
                        new TaskProgressListener() {
                            @Override
                            public void notifyProgress(
                                    String title, String logMessage, float progress) {
                                if (progress > 0) {
                                    updateProgress(progress, 100);
                                }
                                if (Objects.nonNull(logMessage)) {
                                    logOutput.append(logMessage).append(System.lineSeparator());
                                    updateMessage(logOutput.toString());
                                }
                                if (Objects.nonNull(title)) {
                                    updateTitle(title);
                                }
                            }

                            @Override
                            public void notifyFinished(String s) {
                                // ignore
                            }

                            @Override
                            public void notifyFailed(String s) {
                                // ignore
                            }
                        };
                i = 0;
                for (FileWrapper extractFile : ifcExtractFiles) {
                    try {
                        logOutput
                                .append("Reading ")
                                .append(++i)
                                .append("/")
                                .append(ifcFileCount)
                                .append(" IFC File ")
                                .append(extractFile.getPath())
                                .append(System.lineSeparator());
                        updateMessage(logOutput.toString());
                        if (extractFile instanceof IfcFileWrapper) {
                            IfcFileWrapper ifcFile = (IfcFileWrapper) extractFile;
                            List<Feature> extractedFeaturesFromIfcFile;
                            Set<FeatureSet> extractedFeatureSetsFromIfcFile;
                            ParsedIfcFile parsedIfcFile =
                                    IfcFileReader.readIfcFile(
                                            (IfcFileWrapper) extractFile, taskProgressListener);
                            extractedFeaturesFromIfcFile = parsedIfcFile.getFeatures();
                            extractedFeatureSetsFromIfcFile = parsedIfcFile.getFeatureSets();
                            logOutput
                                    .append("Extracted ")
                                    .append(extractedFeaturesFromIfcFile.size())
                                    .append(" Features")
                                    .append(System.lineSeparator())
                                    .append("Extracted ")
                                    .append(extractedFeatureSetsFromIfcFile.size())
                                    .append(" FeatureSets")
                                    .append(System.lineSeparator())
                                    .append(
                                            "-------------------------------------------------------------------------------")
                                    .append(System.lineSeparator())
                                    .append(System.lineSeparator());
                            extractedFeaturesCount += extractedFeaturesFromIfcFile.size();
                            extractedFeatures.addAll(extractedFeaturesFromIfcFile);
                            extractedFeatureSetsCount += extractedFeatureSetsFromIfcFile.size();
                            extractedFeatureSets.addAll(extractedFeatureSetsFromIfcFile);
                            updateMessage(logOutput.toString());
                            convertedFileCount++;
                            updateProgress(convertedFileCount, fileCount);
                            updateTitle(
                                    MessageUtils.getKeyWithParameters(
                                            resourceBundle,
                                            "label.extract.process.converted",
                                            convertedFileCount,
                                            fileCount));
                        } else {
                            throw new IOException("File is not a correct Ifc File");
                        }
                    } catch (Exception e) {
                        logOutput
                                .append("Can't convert file: ")
                                .append(extractFile.getPath())
                                .append(" Reason: ")
                                .append(e.getMessage())
                                .append(System.lineSeparator());
                        updateMessage(logOutput.toString());
                    }
                    if (isCancelled()) {
                        logOutput
                                .append("Operation cancelled by User")
                                .append(System.lineSeparator());
                        updateMessage(logOutput.toString());
                        break;
                    }
                }
                logOutput
                        .append(
                                "-------------------------------------------------------------------------------")
                        .append(System.lineSeparator())
                        .append("Extracted ")
                        .append(extractedFeaturesCount)
                        .append(" out of the ")
                        .append(fileCount)
                        .append(" ifcFiles")
                        .append(System.lineSeparator())
                        .append("Parsed ")
                        .append(extractedFeatures.size())
                        .append(" jsonFeatures")
                        .append(System.lineSeparator())
                        .append(
                                "-------------------------------------------------------------------------------")
                        .append(System.lineSeparator())
                        .append(System.lineSeparator())
                        .append("EXITING, converted ")
                        .append(convertedFileCount)
                        .append("/")
                        .append(fileCount)
                        .append(System.lineSeparator());
                updateMessage(logOutput.toString());
                if (convertedFileCount != fileCount) {
                    logOutput
                            .append(
                                    "Not all Files could be converted, look in the log above to find out why")
                            .append(System.lineSeparator());
                    updateMessage(logOutput.toString());
                }
                return new ExtractResult(
                        extractedFeatures, extractedFeatureSets, logOutput.toString());
            }
        };
    }

    private static String getFileContent(InputStream fis, String encoding) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(fis, encoding))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append('\n');
            }
            return sb.toString();
        }
    }
}
