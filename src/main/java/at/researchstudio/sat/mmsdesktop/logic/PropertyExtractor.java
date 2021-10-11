package at.researchstudio.sat.mmsdesktop.logic;

import static java.util.stream.Collectors.joining;

import at.researchstudio.sat.merkmalservice.model.*;
import at.researchstudio.sat.merkmalservice.model.ifc.IfcDerivedUnit;
import at.researchstudio.sat.merkmalservice.model.ifc.IfcProperty;
import at.researchstudio.sat.merkmalservice.model.ifc.IfcSIUnit;
import at.researchstudio.sat.merkmalservice.model.ifc.IfcUnit;
import at.researchstudio.sat.merkmalservice.vocab.ifc.*;
import at.researchstudio.sat.mmsdesktop.model.helper.FeatureSet;
import at.researchstudio.sat.mmsdesktop.model.ifc.*;
import at.researchstudio.sat.mmsdesktop.model.task.ExtractResult;
import at.researchstudio.sat.mmsdesktop.util.*;
import be.ugent.progress.StatefulTaskProgressListener;
import be.ugent.progress.TaskProgressListener;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import javafx.concurrent.Task;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

public class PropertyExtractor {
    private static final boolean USE_NEWEXTRACTION = true;

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
                        USE_NEWEXTRACTION
                                ? new TaskProgressListener() {
                                    @Override
                                    public void notifyProgress(
                                            String title, String logMessage, float progress) {
                                        if (progress > 0) {
                                            updateProgress(progress, 100);
                                        }
                                        if (Objects.nonNull(logMessage)) {
                                            logOutput
                                                    .append(logMessage)
                                                    .append(System.lineSeparator());
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
                                }
                                : new StatefulTaskProgressListener() {
                                    @Override
                                    public void doNotifyProgress(
                                            String task, String message, float progress) {
                                        Set<String> taskNames = getTaskNames();
                                        double cumulativeProgress =
                                                taskNames.stream()
                                                        .map(this::getTaskProgress)
                                                        .filter(p -> !p.isFinished())
                                                        .mapToDouble(TaskProgress::getLevel)
                                                        .sum();
                                        long taskcount =
                                                taskNames.stream()
                                                        .map(this::getTaskProgress)
                                                        .filter(p -> !p.isFinished())
                                                        .filter(p -> p.getLevel() > 0)
                                                        .count();
                                        double progressToDisplay =
                                                cumulativeProgress / (double) taskcount;

                                        String cumulativeMessage =
                                                taskNames.stream()
                                                        .map(this::getTaskProgress)
                                                        .sorted(
                                                                Comparator.comparingLong(
                                                                        TaskProgress
                                                                                ::getFirstMessageTimestamp))
                                                        .map(this::makeProgressMessage)
                                                        .collect(joining("\n"));
                                        updateMessage(cumulativeMessage);
                                        updateProgress(progressToDisplay, 1);
                                    }

                                    private String makeProgressMessage(TaskProgress taskProgress) {
                                        if (taskProgress.isFinished()) {
                                            return String.format(
                                                    "%s: finished", taskProgress.getTask());
                                        }
                                        if (taskProgress.getLevel() > 0) {
                                            return String.format(
                                                    "%s: %s (%.0f%%)",
                                                    taskProgress.getTask(),
                                                    taskProgress.getMessage(),
                                                    taskProgress.getLevel() * 100);
                                        } else {
                                            return String.format(
                                                    "%s: %s",
                                                    taskProgress.getTask(),
                                                    taskProgress.getMessage());
                                        }
                                    }

                                    @Override
                                    public void doNotifyFinished(String s) {
                                        // ignore
                                    }

                                    @Override
                                    public void doNotifyFailed(String task) {
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

                            if (USE_NEWEXTRACTION) {
                                ParsedIfcFile parsedIfcFile =
                                        IfcFileReader.readIfcFile(
                                                (IfcFileWrapper) extractFile, taskProgressListener);

                                extractedFeaturesFromIfcFile = parsedIfcFile.getFeatures();
                                extractedFeatureSetsFromIfcFile = parsedIfcFile.getFeatureSets();
                            } else {
                                Model model =
                                        IFC2ModelConverter.readFromFile(
                                                ifcFile.getFile(), taskProgressListener);

                                Map<IfcUnitType, List<IfcUnit>> extractedProjectUnitMap =
                                        extractProjectUnits(model, ifcFile.getIfcVersion());
                                Map<IfcPropertyType, List<IfcProperty>> extractedPropertyMap =
                                        extractPropertiesFromHdtData(
                                                model,
                                                extractedProjectUnitMap,
                                                ifcFile.getIfcVersion());
                                extractedFeaturesFromIfcFile =
                                        IfcFileReader.extractFeaturesFromProperties(
                                                extractedPropertyMap, logOutput);
                                extractedFeatureSetsFromIfcFile = Collections.emptySet();
                            }
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

    private static Map<IfcUnitType, List<IfcUnit>> extractProjectUnits(
            Model model, IfcVersion ifcVersion) throws IOException {
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        Resource resource =
                resourceLoader.getResource(ifcVersion.getProjectUnitQueryResourceString());
        InputStream inputStream = resource.getInputStream();
        String extractProjectUnitsQuery =
                getFileContent(inputStream, StandardCharsets.UTF_8.toString());
        Map<IfcUnitType, List<IfcUnit>> projectUnits;

        try (QueryExecution qe = QueryExecutionFactory.create(extractProjectUnitsQuery, model)) {
            ResultSet rs = qe.execSelect();
            List<IfcSIUnit> extractedUnits = new ArrayList<>();
            while (rs.hasNext()) {
                QuerySolution qs = rs.next();
                extractedUnits.add(new IfcSIUnitBuilder(qs).build());
            }
            projectUnits = extractedUnits.stream().collect(Collectors.groupingBy(IfcUnit::getType));
        }

        resource = resourceLoader.getResource(ifcVersion.getDerivedUnitQueryResourceString());
        inputStream = resource.getInputStream();
        String extractDerivedUnitQuery =
                getFileContent(inputStream, StandardCharsets.UTF_8.toString());

        // TODO DERIVEDUNIT QUERY (IMPL QUERY FOR IFC4 still)
        try (QueryExecution qe = QueryExecutionFactory.create(extractDerivedUnitQuery, model)) {
            ResultSet rs = qe.execSelect();
            Set<IfcDerivedUnit> derivedUnits = new HashSet<>();
            while (rs.hasNext()) {
                QuerySolution qs = rs.next();

                IfcDerivedUnit tempDerivedUnit = new IfcDerivedUnitBuilder(qs).build();

                IfcDerivedUnit derivedUnit =
                        derivedUnits.stream()
                                .filter(tempDerivedUnit::equals)
                                .findAny()
                                .orElse(tempDerivedUnit);
                derivedUnits.add(derivedUnit);

                derivedUnit.addDerivedUnitElement(
                        new IfcSIUnit(
                                qs.getResource("derivedUnitElUri").getURI(),
                                Utils.executeOrDefaultOnException(
                                        () ->
                                                IfcUnitType.fromString(
                                                        qs.getResource("derivedUnitElType")
                                                                .getURI()),
                                        IfcUnitType.UNKNOWN,
                                        NullPointerException.class,
                                        IllegalArgumentException.class),
                                Utils.executeOrDefaultOnException(
                                        () ->
                                                IfcUnitMeasure.fromString(
                                                        qs.getResource("derivedUnitElMeasure")
                                                                .getURI()),
                                        IfcUnitMeasure.UNKNOWN,
                                        NullPointerException.class,
                                        IllegalArgumentException.class),
                                Utils.executeOrDefaultOnException(
                                        () ->
                                                IfcUnitMeasurePrefix.fromString(
                                                        qs.getResource("derivedUnitElPrefix")
                                                                .getURI()),
                                        IfcUnitMeasurePrefix.NONE,
                                        NullPointerException.class,
                                        IllegalArgumentException.class),
                                false),
                        qs.getLiteral("exponentValue").getInt());
            }

            // TODO: IFC4 UNIT QUERY INCL IFCCONVERSIONBASEDUNIT, IFCMEASUREWITHUNIT
            projectUnits.putAll(
                    derivedUnits.stream().collect(Collectors.groupingBy(IfcUnit::getType)));
        }

        return projectUnits;
    }

    private static Map<IfcPropertyType, List<IfcProperty>> extractPropertiesFromHdtData(
            Model model, Map<IfcUnitType, List<IfcUnit>> projectUnits, IfcVersion ifcVersion)
            throws IOException {
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        Resource resource = resourceLoader.getResource(ifcVersion.getPropertyQueryResourceString());
        InputStream inputStream = resource.getInputStream();
        String extractPropNamesQuery =
                getFileContent(inputStream, StandardCharsets.UTF_8.toString());
        try (QueryExecution qe = QueryExecutionFactory.create(extractPropNamesQuery, model)) {
            ResultSet rs = qe.execSelect();
            Set<IfcProperty> extractedProperties = new HashSet<>();
            while (rs.hasNext()) {
                QuerySolution qs = rs.next();
                IfcProperty tempProp = new IfcPropertyBuilder(qs, projectUnits).build();
                IfcProperty prop =
                        extractedProperties.stream()
                                .filter(tempProp::equals)
                                .findAny()
                                .orElse(tempProp);
                extractedProperties.add(prop);

                Literal propValue = qs.getLiteral("propValue");
                if (propValue != null) {
                    prop.addExtractedValue(propValue.getValue().toString());
                }

                if (IfcPropertyType.VALUELIST.equals(prop.getType())) {
                    Literal enumOptionValue = qs.getLiteral("enumOptionValue");
                    if (enumOptionValue != null) {
                        prop.addEnumOptionValue(enumOptionValue.toString());
                    }
                }
            }
            return extractedProperties.stream()
                    .collect(Collectors.groupingBy(IfcProperty::getType));
        }
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
