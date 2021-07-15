package at.researchstudio.sat.mmsdesktop.logic;

import static java.util.stream.Collectors.joining;

import at.researchstudio.sat.merkmalservice.model.*;
import at.researchstudio.sat.merkmalservice.vocab.ifc.IfcPropertyType;
import at.researchstudio.sat.merkmalservice.vocab.ifc.IfcUnitType;
import at.researchstudio.sat.merkmalservice.vocab.qudt.QudtQuantityKind;
import at.researchstudio.sat.merkmalservice.vocab.qudt.QudtUnit;
import at.researchstudio.sat.mmsdesktop.model.ifc.IfcProperty;
import at.researchstudio.sat.mmsdesktop.model.ifc.IfcUnit;
import at.researchstudio.sat.mmsdesktop.model.ifc.IfcVersion;
import at.researchstudio.sat.mmsdesktop.model.task.ExtractResult;
import at.researchstudio.sat.mmsdesktop.util.IfcFileWrapper;
import at.researchstudio.sat.mmsdesktop.util.MessageUtils;
import at.researchstudio.sat.mmsdesktop.util.Utils;
import be.ugent.progress.StatefulTaskProgressListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javafx.concurrent.Task;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.ext.com.google.common.base.Throwables;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StopWatch;

public class PropertyExtractor {
    private static final boolean USE_NEWEXTRACTION = false;

    public static Task<ExtractResult> generateIfcFileToJsonTask(
            List<IfcFileWrapper> ifcFiles, final ResourceBundle resourceBundle) {
        return new Task<>() {
            @Override
            public ExtractResult call() {
                StringBuilder logOutput = new StringBuilder();
                final int max = ifcFiles.size() * 2;
                List<Feature> extractedFeatures = new ArrayList<>();
                int extractedIfcProperties = 0;
                if (USE_NEWEXTRACTION) {
                    // NEW PROCESS
                    final Pattern propertyExtractPattern =
                            Pattern.compile(
                                    "(?>#[0-9]*= IFCPROPERTYSINGLEVALUE\\(')(?<name>.*)',\\$,(?<type>[A-Z]*)\\(('?)(?<value>.*)(\\),\\$)");
                    // <- warning might contain a . or ' at the end of the value
                    // Pattern.compile("^#\\d+=IFCPROPERTYSINGLEVALUE\\('([^']+)',\\$,([A-Z]+)\\('?([^)']+)'?\\),\\$\\);\\s*$");
                    final Pattern unitExtractPattern =
                            Pattern.compile(
                                    "(?>#[0-9]*= IFCSIUNIT\\(\\*,.)(?<type>.*).,\\$,.(?<measure>.*).\\)");
                    // <- warning might contain whitespaces, trim needed
                    List<Map<IfcPropertyType, List<IfcProperty>>> propertyData = new ArrayList<>();
                    int i = 0;
                    updateTitle(
                            MessageUtils.getKeyWithParameters(
                                    resourceBundle, "label.extract.process.start"));
                    for (IfcFileWrapper ifcFile : ifcFiles) {
                        try {
                            StopWatch stopWatch = new StopWatch();
                            stopWatch.start();
                            logOutput
                                    .append("Reading ")
                                    .append(i + 1)
                                    .append("/")
                                    .append(ifcFiles.size())
                                    .append(" Files to Lines")
                                    .append(System.lineSeparator());
                            updateMessage(logOutput.toString());
                            int sumHashes = 0;
                            Set<IfcProperty> ifcProperties = new HashSet<>();
                            Set<IfcUnit> projectUnits = new HashSet<>();
                            try (LineIterator it =
                                    FileUtils.lineIterator(
                                            ifcFile.getFile(), StandardCharsets.UTF_8.toString())) {
                                while (it.hasNext()) {
                                    String line = it.nextLine();
                                    sumHashes += line.hashCode();
                                    // do something with line
                                    if (line.contains("IFCPROPERTYSINGLEVALUE")) {
                                        Matcher matcher = propertyExtractPattern.matcher(line);
                                        if (matcher.find()) {
                                            String name = StringUtils.trim(matcher.group("name"));
                                            String type = StringUtils.trim(matcher.group("type"));
                                            String value = StringUtils.trim(matcher.group("value"));
                                            ifcProperties.add(new IfcProperty(name, type));
                                        } else {
                                            logOutput
                                                    .append(
                                                            "WARN: Could not extract IFCPROPERTYSINGLEVALUE from: ")
                                                    .append(ifcFile.getPath())
                                                    .append(" Line: ")
                                                    .append(line)
                                                    .append(System.lineSeparator());
                                        }
                                    } else if (line.contains("IFCSIUNIT")) {
                                        Matcher matcher = unitExtractPattern.matcher(line);
                                        if (matcher.find()) {
                                            String type = StringUtils.trim(matcher.group("type"));
                                            String measure =
                                                    StringUtils.trim(matcher.group("measure"));
                                            String prefix =
                                                    ""; // TODO: ADD PREFIX EXTRACTION FOR IFC FILE
                                            // PARSER
                                            projectUnits.add(new IfcUnit(type, measure, prefix));
                                        } else {
                                            logOutput
                                                    .append(
                                                            "WARN: Could not extract IFCSIUNIT from: ")
                                                    .append(ifcFile.getPath())
                                                    .append(" Line: ")
                                                    .append(line)
                                                    .append(System.lineSeparator());
                                        }
                                    }
                                }
                            }
                            Map<IfcUnitType, List<IfcUnit>> projectUnitsMap =
                                    projectUnits.stream()
                                            .collect(Collectors.groupingBy(IfcUnit::getType));
                            logOutput
                                    .append("extractedIfcProperties: ")
                                    .append(ifcProperties.size())
                                    .append(System.lineSeparator());
                            Map<IfcPropertyType, List<IfcProperty>> extractedPropertyMap =
                                    extractPropertiesFromData(ifcProperties, projectUnitsMap);
                            stopWatch.stop();
                            logOutput
                                    .append(sumHashes)
                                    .append(System.lineSeparator())
                                    .append(stopWatch.getTotalTimeSeconds())
                                    .append(System.lineSeparator())
                                    .append(stopWatch.prettyPrint())
                                    .append(System.lineSeparator());
                            updateMessage(logOutput.toString());
                            propertyData.add(extractedPropertyMap);
                            logOutput
                                    .append("Reading ")
                                    .append(++i)
                                    .append("/")
                                    .append(ifcFiles.size())
                                    .append(" Files to Lines finished")
                                    .append(System.lineSeparator());
                            updateMessage(logOutput.toString());
                        } catch (Exception e) {
                            logOutput
                                    .append("Can't convert file: ")
                                    .append(ifcFile.getPath())
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
                        updateProgress(i, max);
                        updateTitle(
                                MessageUtils.getKeyWithParameters(
                                        resourceBundle, "label.extract.process.ifc2hdt", i, max));
                    }
                    final int newMax = ifcFiles.size() + propertyData.size();
                    for (Map<IfcPropertyType, List<IfcProperty>> extractedPropertyMap :
                            propertyData) {
                        extractedIfcProperties +=
                                extractedPropertyMap.values().stream()
                                        .mapToInt(Collection::size)
                                        .sum();
                        ExtractResult partialExtractResult =
                                extractFeaturesFromProperties(extractedPropertyMap);
                        extractedFeatures.addAll(partialExtractResult.getExtractedFeatures());
                        logOutput.append(partialExtractResult.getLogOutput());
                        updateMessage(logOutput.toString());
                        if (isCancelled()) {
                            logOutput
                                    .append("Operation cancelled by User")
                                    .append(System.lineSeparator());
                            updateMessage(logOutput.toString());
                            break;
                        }
                        updateProgress(++i, newMax);
                        updateTitle(
                                MessageUtils.getKeyWithParameters(
                                        resourceBundle,
                                        "label.extract.process.features",
                                        i,
                                        newMax));
                    }
                    logOutput
                            .append(
                                    "-------------------------------------------------------------------------------")
                            .append(System.lineSeparator())
                            .append("Extracted ")
                            .append(extractedIfcProperties)
                            .append(" out of the ")
                            .append(ifcFiles.size())
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
                            .append(propertyData.size())
                            .append("/")
                            .append(ifcFiles.size())
                            .append(System.lineSeparator());
                    updateMessage(logOutput.toString());
                    if (propertyData.size() != ifcFiles.size()) {
                        logOutput
                                .append(
                                        "Not all Files could be converted, look in the log above to find out why")
                                .append(System.lineSeparator());
                        updateMessage(logOutput.toString());
                    }
                } else {
                    // OLD PROCESS
                    Map<IfcVersion, List<Model>> models = new HashMap<>();
                    int hdtDataCount = 0;
                    int i = 0;
                    updateTitle(
                            MessageUtils.getKeyWithParameters(
                                    resourceBundle, "label.extract.process.start"));
                    for (IfcFileWrapper ifcFile : ifcFiles) {
                        try {
                            List<Model> updatedList = new ArrayList<>();
                            if (!models.isEmpty()
                                    && !models.get(ifcFile.getIfcVersion()).isEmpty()) {
                                updatedList.addAll(models.get(ifcFile.getIfcVersion()));
                            }
                            updatedList.add(
                                    IFC2ModelConverter.readFromFile(
                                            ifcFile.getFile(),
                                            new StatefulTaskProgressListener() {
                                                @Override
                                                public void doNotifyProgress(
                                                        String task,
                                                        String message,
                                                        float progress) {
                                                    Set<String> taskNames = getTaskNames();
                                                    double cumulativeProgress =
                                                            taskNames.stream()
                                                                    .map(this::getTaskProgress)
                                                                    .filter(p -> !p.isFinished())
                                                                    .mapToDouble(
                                                                            TaskProgress::getLevel)
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
                                                                            Comparator
                                                                                    .comparingLong(
                                                                                            TaskProgress
                                                                                                    ::getFirstMessageTimestamp))
                                                                    .map(this::makeProgressMessage)
                                                                    .collect(joining("\n"));
                                                    updateMessage(cumulativeMessage);
                                                    updateProgress(progressToDisplay, 1);
                                                }

                                                private String makeProgressMessage(
                                                        TaskProgress taskProgress) {
                                                    if (taskProgress.isFinished()) {
                                                        return String.format(
                                                                "%s: finished",
                                                                taskProgress.getTask());
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
                                            }));
                            hdtDataCount++;
                            models.put(ifcFile.getIfcVersion(), updatedList);
                            logOutput
                                    .append("Converted ")
                                    .append(++i)
                                    .append("/")
                                    .append(ifcFiles.size())
                                    .append(" Files to HDT")
                                    .append(System.lineSeparator());
                            updateMessage(logOutput.toString());
                        } catch (Exception e) {
                            logOutput
                                    .append("Can't convert file: ")
                                    .append(ifcFile.getPath())
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
                        updateProgress(i, max);
                        updateTitle(
                                MessageUtils.getKeyWithParameters(
                                        resourceBundle, "label.extract.process.ifc2hdt", i, max));
                    }
                    final int newMax = ifcFiles.size() + models.size();
                    for (Map.Entry<IfcVersion, List<Model>> modelMapEntry : models.entrySet()) {
                        for (Model model : modelMapEntry.getValue()) {
                            try {
                                Map<IfcUnitType, List<IfcUnit>> extractedProjectUnitMap =
                                        extractProjectUnits(model, modelMapEntry.getKey());
                                Map<IfcPropertyType, List<IfcProperty>> extractedPropertyMap =
                                        extractPropertiesFromHdtData(
                                                model,
                                                extractedProjectUnitMap,
                                                modelMapEntry.getKey());
                                extractedIfcProperties +=
                                        extractedPropertyMap.values().stream()
                                                .mapToInt(Collection::size)
                                                .sum();
                                ExtractResult partialExtractResult =
                                        extractFeaturesFromProperties(extractedPropertyMap);
                                extractedFeatures.addAll(
                                        partialExtractResult.getExtractedFeatures());
                                logOutput.append(partialExtractResult.getLogOutput());
                            } catch (IOException ioException) {
                                logOutput
                                        .append(Throwables.getStackTraceAsString(ioException))
                                        .append(System.lineSeparator());
                            }
                            updateMessage(logOutput.toString());
                            if (isCancelled()) {
                                logOutput
                                        .append("Operation cancelled by User")
                                        .append(System.lineSeparator());
                                updateMessage(logOutput.toString());
                                break;
                            }
                            updateProgress(++i, newMax);
                            updateTitle(
                                    MessageUtils.getKeyWithParameters(
                                            resourceBundle,
                                            "label.extract.process.features",
                                            i,
                                            newMax));
                        }
                    }
                    logOutput
                            .append(
                                    "-------------------------------------------------------------------------------")
                            .append(System.lineSeparator())
                            .append("Extracted ")
                            .append(extractedIfcProperties)
                            .append(" out of the ")
                            .append(ifcFiles.size())
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
                            .append(hdtDataCount)
                            .append("/")
                            .append(ifcFiles.size())
                            .append(System.lineSeparator());
                    updateMessage(logOutput.toString());
                    if (hdtDataCount != ifcFiles.size()) {
                        logOutput
                                .append(
                                        "Not all Files could be converted, look in the log above to find out why")
                                .append(System.lineSeparator());
                        updateMessage(logOutput.toString());
                    }
                }
                return new ExtractResult(extractedFeatures, logOutput.toString());
            }
        };
    }

    private static Map<IfcUnitType, List<IfcUnit>> extractProjectUnits(
            Model model, IfcVersion ifcVersion) throws IOException {
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        String query;
        Resource resource =
                resourceLoader.getResource(ifcVersion.getProjectUnitQueryResourceString());
        InputStream inputStream = resource.getInputStream();
        String extractPropNamesQuery =
                getFileContent(inputStream, StandardCharsets.UTF_8.toString());
        try (QueryExecution qe = QueryExecutionFactory.create(extractPropNamesQuery, model)) {
            ResultSet rs = qe.execSelect();
            List<IfcUnit> extractedUnits = new ArrayList<>();
            while (rs.hasNext()) {
                QuerySolution qs = rs.next();
                extractedUnits.add(
                        new IfcUnit(
                                qs.getResource("unitType"),
                                qs.getResource("unitMeasure"),
                                qs.getResource("unitPrefix")));
            }
            return extractedUnits.stream().collect(Collectors.groupingBy(IfcUnit::getType));
        }
    }

    private static Map<IfcPropertyType, List<IfcProperty>> extractPropertiesFromData(
            Set<IfcProperty> ifcProperties, Map<IfcUnitType, List<IfcUnit>> projectUnits) {
        return ifcProperties.stream()
                .map(ifc -> new IfcProperty(ifc, projectUnits))
                .collect(Collectors.groupingBy(IfcProperty::getType));
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
                IfcProperty tempProp = new IfcProperty(qs, projectUnits);
                IfcProperty prop =
                        extractedProperties.stream()
                                .filter(tempProp::equals)
                                .findAny()
                                .orElse(tempProp);
                extractedProperties.add(prop);

                prop.addExtractedValue(qs);
                if (IfcPropertyType.VALUELIST.equals(prop.getType())) {
                    prop.addEnumOptionValue(qs);
                }
            }
            return extractedProperties.stream()
                    .collect(Collectors.groupingBy(IfcProperty::getType));
        }
    }

    private static ExtractResult extractFeaturesFromProperties(
            Map<IfcPropertyType, List<IfcProperty>> extractedProperties) {
        List<Feature> extractedFeatures = new ArrayList<>();
        StringBuilder fullLog = new StringBuilder();
        for (Map.Entry<IfcPropertyType, List<IfcProperty>> entry : extractedProperties.entrySet()) {
            IfcPropertyType ifcPropertyType = entry.getKey();
            String logString = entry.getValue().size() + " " + ifcPropertyType + " Properties";
            switch (ifcPropertyType) {
                case LOGICAL:
                case EXPRESS_BOOL:
                case BOOL:
                    fullLog.append(logString).append(System.getProperty("line.separator"));
                    extractedFeatures.addAll(
                            entry.getValue().stream()
                                    .map(ifcProperty -> new BooleanFeature(ifcProperty.getName()))
                                    .collect(Collectors.toList()));
                    break;
                case TEXT:
                case LABEL:
                    fullLog.append(logString).append(System.getProperty("line.separator"));
                    extractedFeatures.addAll(
                            entry.getValue().stream()
                                    .map(ifcProperty -> new StringFeature(ifcProperty.getName()))
                                    .collect(Collectors.toList()));
                    break;
                case VOLUME_MEASURE:
                    fullLog.append(logString).append(System.getProperty("line.separator"));
                    extractedFeatures.addAll(
                            entry.getValue().stream()
                                    .map(
                                            ifcProperty ->
                                                    new NumericFeature(
                                                            ifcProperty.getName(),
                                                            QudtQuantityKind.VOLUME,
                                                            Utils.extractQudtUnitFromProperty(
                                                                    ifcProperty)))
                                    .collect(Collectors.toList()));
                    break;
                case AREA_MEASURE:
                    fullLog.append(logString).append(System.getProperty("line.separator"));
                    extractedFeatures.addAll(
                            entry.getValue().stream()
                                    .map(
                                            ifcProperty ->
                                                    new NumericFeature(
                                                            ifcProperty.getName(),
                                                            QudtQuantityKind.AREA,
                                                            Utils.extractQudtUnitFromProperty(
                                                                    ifcProperty)))
                                    .collect(Collectors.toList()));
                    break;
                case DIMENSION_COUNT:
                case REAL:
                case EXPRESS_INTEGER:
                case POSITIVE_INTEGER:
                case INTEGER:
                case COUNT_MEASURE:
                    fullLog.append(logString).append(System.getProperty("line.separator"));
                    extractedFeatures.addAll(
                            entry.getValue().stream()
                                    .map(
                                            ifcProperty ->
                                                    new NumericFeature(
                                                            ifcProperty.getName(),
                                                            QudtQuantityKind.DIMENSIONLESS,
                                                            QudtUnit.UNITLESS))
                                    .collect(Collectors.toList()));
                    break;
                case PLANE_ANGLE_MEASURE:
                    fullLog.append(logString).append(System.getProperty("line.separator"));
                    extractedFeatures.addAll(
                            entry.getValue().stream()
                                    .map(
                                            ifcProperty ->
                                                    new NumericFeature(
                                                            ifcProperty.getName(),
                                                            QudtQuantityKind.ANGLE,
                                                            Utils.extractQudtUnitFromProperty(
                                                                    ifcProperty)))
                                    .collect(Collectors.toList()));
                    break;
                case THERMAL_TRANSMITTANCE_MEASURE:
                    fullLog.append(logString).append(System.getProperty("line.separator"));
                    extractedFeatures.addAll(
                            entry.getValue().stream()
                                    .map(
                                            ifcProperty ->
                                                    new NumericFeature(
                                                            ifcProperty.getName(),
                                                            QudtQuantityKind.DIMENSIONLESS,
                                                            // TODO: Figure out what
                                                            // THERMAL_TRANSMITTANCE_MEASURE is in
                                                            // QUDT.QuantityKind
                                                            Utils.extractQudtUnitFromProperty(
                                                                    ifcProperty)))
                                    .collect(Collectors.toList()));
                case LENGTH_MEASURE:
                case POSITIVE_LENGTH_MEASURE:
                    fullLog.append(logString).append(System.getProperty("line.separator"));
                    extractedFeatures.addAll(
                            entry.getValue().stream()
                                    .map(
                                            ifcProperty ->
                                                    new NumericFeature(
                                                            ifcProperty.getName(),
                                                            Utils
                                                                    .extractQudtQuantityKindFromProperty(
                                                                            ifcProperty),
                                                            Utils.extractQudtUnitFromProperty(
                                                                    ifcProperty)))
                                    .collect(Collectors.toList()));
                    break;
                case VALUELIST:
                    fullLog.append(logString).append(System.getProperty("line.separator"));
                    extractedFeatures.addAll(
                            entry.getValue().stream()
                                    .map(
                                            ifcProperty ->
                                                    new EnumFeature(
                                                            ifcProperty.getName(),
                                                            ifcProperty
                                                                    .getEnumOptionValues()
                                                                    .stream()
                                                                    .map(
                                                                            option ->
                                                                                    new EnumFeature
                                                                                            .MEStringValue(
                                                                                            option,
                                                                                            null))
                                                                    .collect(Collectors.toList()),
                                                            false))
                                    .collect(Collectors.toList()));
                    break;
                default:
                    fullLog.append(logString)
                            .append(
                                    ", will be ignored, no matching Feature-Type determined yet for:")
                            .append(System.getProperty("line.separator"));
                    entry.getValue()
                            .forEach(
                                    property ->
                                            fullLog.append(property.toString())
                                                    .append(System.getProperty("line.separator")));
                    fullLog.append(
                                    "-------------------------------------------------------------------------")
                            .append(System.getProperty("line.separator"));
                    break;
            }
        }
        return new ExtractResult(extractedFeatures, fullLog.toString());
    }

    private static String getFileContent(InputStream fis, String encoding) throws IOException {
        // TODO: REFACTOR THIS
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
