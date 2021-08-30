package at.researchstudio.sat.mmsdesktop.logic;

import static java.util.stream.Collectors.joining;

import at.researchstudio.sat.merkmalservice.model.*;
import at.researchstudio.sat.merkmalservice.model.ifc.IfcDerivedUnit;
import at.researchstudio.sat.merkmalservice.model.ifc.IfcProperty;
import at.researchstudio.sat.merkmalservice.model.ifc.IfcSIUnit;
import at.researchstudio.sat.merkmalservice.model.ifc.IfcUnit;
import at.researchstudio.sat.merkmalservice.vocab.ifc.*;
import at.researchstudio.sat.merkmalservice.vocab.qudt.QudtQuantityKind;
import at.researchstudio.sat.merkmalservice.vocab.qudt.QudtUnit;
import at.researchstudio.sat.mmsdesktop.model.ifc.IfcVersion;
import at.researchstudio.sat.mmsdesktop.model.task.ExtractResult;
import at.researchstudio.sat.mmsdesktop.util.*;
import be.ugent.progress.StatefulTaskProgressListener;
import java.io.*;
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
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StopWatch;

public class PropertyExtractor {
    private static final boolean USE_NEWEXTRACTION = false;

    public static Task<ExtractResult> generateExtractFilesToJsonTask(
            List<FileWrapper> extractFiles, final ResourceBundle resourceBundle) {
        return new Task<>() {
            @Override
            public ExtractResult call() {
                StringBuilder logOutput = new StringBuilder();
                final int max = extractFiles.size() * 2;
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
                    for (FileWrapper extractFile : extractFiles) {
                        try {
                            if (extractFile instanceof IfcFileWrapper) {
                                StopWatch stopWatch = new StopWatch();
                                stopWatch.start();
                                logOutput
                                        .append("Reading ")
                                        .append(i + 1)
                                        .append("/")
                                        .append(extractFiles.size())
                                        .append(" Files to Lines")
                                        .append(System.lineSeparator());
                                updateMessage(logOutput.toString());
                                int sumHashes = 0;
                                Set<IfcProperty> ifcProperties = new HashSet<>();
                                Set<IfcSIUnit> projectUnits = new HashSet<>();
                                try (LineIterator it =
                                        FileUtils.lineIterator(
                                                extractFile.getFile(),
                                                StandardCharsets.UTF_8.toString())) {
                                    while (it.hasNext()) {
                                        String line = it.nextLine();
                                        sumHashes += line.hashCode();
                                        // do something with line
                                        if (line.contains("IFCPROPERTYSINGLEVALUE")) {
                                            Matcher matcher = propertyExtractPattern.matcher(line);
                                            if (matcher.find()) {
                                                String name =
                                                        StringUtils.trim(matcher.group("name"));
                                                String type =
                                                        StringUtils.trim(matcher.group("type"));
                                                String value =
                                                        StringUtils.trim(matcher.group("value"));
                                                ifcProperties.add(new IfcProperty(name, type));
                                            } else {
                                                logOutput
                                                        .append(
                                                                "WARN: Could not extract IFCPROPERTYSINGLEVALUE from: ")
                                                        .append(extractFile.getPath())
                                                        .append(" Line: ")
                                                        .append(line)
                                                        .append(System.lineSeparator());
                                            }
                                        } else if (line.contains("IFCSIUNIT")) {
                                            Matcher matcher = unitExtractPattern.matcher(line);
                                            if (matcher.find()) {
                                                String lineNumber =
                                                        "TODO"; // TODO Extract linenumber
                                                String type =
                                                        StringUtils.trim(matcher.group("type"));
                                                String measure =
                                                        StringUtils.trim(matcher.group("measure"));
                                                String prefix =
                                                        ""; // TODO: ADD PREFIX EXTRACTION FOR IFC
                                                // FILE
                                                boolean projectDefault =
                                                        false; // TODO: FIGURE OUT PROJECTDEFAULT
                                                // PARSER
                                                projectUnits.add(
                                                        new IfcSIUnit(
                                                                lineNumber,
                                                                type,
                                                                measure,
                                                                prefix,
                                                                projectDefault));
                                            } else {
                                                logOutput
                                                        .append(
                                                                "WARN: Could not extract IFCSIUNIT from: ")
                                                        .append(extractFile.getPath())
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
                                        .append(extractFiles.size())
                                        .append(" Files to Lines finished")
                                        .append(System.lineSeparator());
                            } else {
                                extractedFeatures.addAll(
                                        at.researchstudio.sat.merkmalservice.utils.Utils
                                                .readFromJson(extractFile.getFile()));
                                logOutput
                                        .append("Reading ")
                                        .append(++i)
                                        .append("/")
                                        .append(extractFiles.size())
                                        .append(" Files to Features")
                                        .append(System.lineSeparator());
                            }
                            updateMessage(logOutput.toString());
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
                        updateProgress(i, max);
                        updateTitle(
                                MessageUtils.getKeyWithParameters(
                                        resourceBundle, "label.extract.process.ifc2hdt", i, max));
                    }
                    final int newMax = extractFiles.size() + propertyData.size();
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
                            .append(extractFiles.size())
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
                            .append(extractFiles.size())
                            .append(System.lineSeparator());
                    updateMessage(logOutput.toString());
                    if (propertyData.size() != extractFiles.size()) {
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
                    for (FileWrapper extractFile : extractFiles) {
                        try {
                            if (extractFile instanceof IfcFileWrapper) {
                                IfcFileWrapper ifcFile = (IfcFileWrapper) extractFile;
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
                                                                        .filter(
                                                                                p ->
                                                                                        !p
                                                                                                .isFinished())
                                                                        .mapToDouble(
                                                                                TaskProgress
                                                                                        ::getLevel)
                                                                        .sum();
                                                        long taskcount =
                                                                taskNames.stream()
                                                                        .map(this::getTaskProgress)
                                                                        .filter(
                                                                                p ->
                                                                                        !p
                                                                                                .isFinished())
                                                                        .filter(
                                                                                p ->
                                                                                        p.getLevel()
                                                                                                > 0)
                                                                        .count();
                                                        double progressToDisplay =
                                                                cumulativeProgress
                                                                        / (double) taskcount;

                                                        String cumulativeMessage =
                                                                taskNames.stream()
                                                                        .map(this::getTaskProgress)
                                                                        .sorted(
                                                                                Comparator
                                                                                        .comparingLong(
                                                                                                TaskProgress
                                                                                                        ::getFirstMessageTimestamp))
                                                                        .map(
                                                                                this
                                                                                        ::makeProgressMessage)
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
                                        .append(extractFiles.size())
                                        .append(" Files to HDT")
                                        .append(System.lineSeparator());
                            } else {
                                extractedFeatures.addAll(
                                        at.researchstudio.sat.merkmalservice.utils.Utils
                                                .readFromJson(extractFile.getFile()));
                                logOutput
                                        .append("Reading ")
                                        .append(++i)
                                        .append("/")
                                        .append(extractFiles.size())
                                        .append(" Files to Features")
                                        .append(System.lineSeparator());
                            }
                            updateMessage(logOutput.toString());
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
                        updateProgress(i, max);
                        updateTitle(
                                MessageUtils.getKeyWithParameters(
                                        resourceBundle, "label.extract.process.ifc2hdt", i, max));
                    }
                    final int newMax = extractFiles.size() + models.size();
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
                            .append(extractFiles.size())
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
                            .append(extractFiles.size())
                            .append(System.lineSeparator());
                    updateMessage(logOutput.toString());
                    if (hdtDataCount != extractFiles.size()) {
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
                                    .map(
                                            ifcProperty -> {
                                                BooleanFeature f =
                                                        new BooleanFeature(ifcProperty.getName());
                                                f.setUniqueValues(
                                                        ifcProperty.getExtractedUniqueValues());
                                                f.setDescriptionFromUniqueValues(
                                                        ifcProperty.getExtractedUniqueValues());
                                                return f;
                                            })
                                    .collect(Collectors.toList()));
                    break;
                case IDENTIFIER:
                case TEXT:
                case LABEL:
                    fullLog.append(logString).append(System.getProperty("line.separator"));
                    extractedFeatures.addAll(
                            entry.getValue().stream()
                                    .map(
                                            ifcProperty -> {
                                                StringFeature f =
                                                        new StringFeature(ifcProperty.getName());
                                                f.setUniqueValues(
                                                        ifcProperty.getExtractedUniqueValues());
                                                f.setDescriptionFromUniqueValues(
                                                        ifcProperty.getExtractedUniqueValues());
                                                return f;
                                            })
                                    .collect(Collectors.toList()));
                    break;
                case VOLUME_MEASURE:
                    fullLog.append(logString).append(System.getProperty("line.separator"));
                    extractedFeatures.addAll(
                            entry.getValue().stream()
                                    .map(
                                            ifcProperty ->
                                                    Utils.parseNumericFeature(
                                                            ifcProperty, QudtQuantityKind.VOLUME))
                                    .collect(Collectors.toList()));
                    break;
                case AREA_MEASURE:
                    fullLog.append(logString).append(System.getProperty("line.separator"));
                    extractedFeatures.addAll(
                            entry.getValue().stream()
                                    .map(
                                            ifcProperty ->
                                                    Utils.parseNumericFeature(
                                                            ifcProperty, QudtQuantityKind.AREA))
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
                                                    Utils.parseNumericFeature(
                                                            ifcProperty,
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
                                                    Utils.parseNumericFeature(
                                                            ifcProperty, QudtQuantityKind.ANGLE))
                                    .collect(Collectors.toList()));
                    break;
                case THERMAL_TRANSMITTANCE_MEASURE:
                    fullLog.append(logString).append(System.getProperty("line.separator"));
                    extractedFeatures.addAll(
                            entry.getValue().stream()
                                    .map(
                                            ifcProperty ->
                                                    Utils.parseNumericFeature(
                                                            ifcProperty,
                                                            QudtQuantityKind.DIMENSIONLESS))
                                    // TODO: Figure out what THERMAL_TRANSMITTANCE_MEASURE is in
                                    // QUDT.QuantityKind
                                    .collect(Collectors.toList()));
                    break;
                case LUMINOUS_INTESITY_MEASURE:
                    fullLog.append(logString).append(System.getProperty("line.separator"));
                    extractedFeatures.addAll(
                            entry.getValue().stream()
                                    .map(
                                            ifcProperty ->
                                                    Utils.parseNumericFeature(
                                                            ifcProperty,
                                                            QudtQuantityKind.LUMINOUS_INTENSITY))
                                    .collect(Collectors.toList()));
                    break;
                case ELECTRIC_CURRENT_MEASURE:
                    fullLog.append(logString).append(System.getProperty("line.separator"));
                    extractedFeatures.addAll(
                            entry.getValue().stream()
                                    .map(
                                            ifcProperty ->
                                                    Utils.parseNumericFeature(
                                                            ifcProperty,
                                                            QudtQuantityKind.ELECTRIC_CURRENT))
                                    .collect(Collectors.toList()));
                    break;
                case MASS_DENSITY_MEASURE:
                    fullLog.append(logString).append(System.getProperty("line.separator"));
                    extractedFeatures.addAll(
                            entry.getValue().stream()
                                    .map(
                                            ifcProperty ->
                                                    Utils.parseNumericFeature(
                                                            ifcProperty,
                                                            QudtQuantityKind.MASS_DENSITY))
                                    .collect(Collectors.toList()));
                    break;
                case ILLUMINANCE_MEASURE:
                    fullLog.append(logString).append(System.getProperty("line.separator"));
                    extractedFeatures.addAll(
                            entry.getValue().stream()
                                    .map(
                                            ifcProperty ->
                                                    Utils.parseNumericFeature(
                                                            ifcProperty,
                                                            QudtQuantityKind.ILLUMINANCE))
                                    .collect(Collectors.toList()));
                    break;
                case PLANAR_FORCE_MEASURE:
                    fullLog.append(logString).append(System.getProperty("line.separator"));
                    extractedFeatures.addAll(
                            entry.getValue().stream()
                                    .map(
                                            ifcProperty ->
                                                    Utils.parseNumericFeature(
                                                            ifcProperty,
                                                            QudtQuantityKind.DIMENSIONLESS))
                                    // TODO: Figure out what PLANAR_FORCE_MEASURE is in
                                    // QUDT.QuantityKind
                                    .collect(Collectors.toList()));
                    break;
                case FORCE_MEASURE:
                    fullLog.append(logString).append(System.getProperty("line.separator"));
                    extractedFeatures.addAll(
                            entry.getValue().stream()
                                    .map(
                                            ifcProperty ->
                                                    Utils.parseNumericFeature(
                                                            ifcProperty, QudtQuantityKind.FORCE))
                                    .collect(Collectors.toList()));
                    break;
                case MOMENT_OF_INERTIA_MEASURE:
                    fullLog.append(logString).append(System.getProperty("line.separator"));
                    extractedFeatures.addAll(
                            entry.getValue().stream()
                                    .map(
                                            ifcProperty ->
                                                    Utils.parseNumericFeature(
                                                            ifcProperty,
                                                            QudtQuantityKind.MOMENT_OF_INERTIA))
                                    .collect(Collectors.toList()));
                    break;
                case THERMODYNAMIC_TEMPERATURE_MEASURE:
                    fullLog.append(logString).append(System.getProperty("line.separator"));
                    extractedFeatures.addAll(
                            entry.getValue().stream()
                                    .map(
                                            ifcProperty ->
                                                    Utils.parseNumericFeature(
                                                            ifcProperty,
                                                            QudtQuantityKind
                                                                    .THERMODYNAMIC_TEMPERATURE))
                                    .collect(Collectors.toList()));
                    break;
                case MASS_MEASURE:
                    fullLog.append(logString).append(System.getProperty("line.separator"));
                    extractedFeatures.addAll(
                            entry.getValue().stream()
                                    .map(
                                            ifcProperty ->
                                                    Utils.parseNumericFeature(
                                                            ifcProperty, QudtQuantityKind.MASS))
                                    .collect(Collectors.toList()));
                    break;
                case PRESSURE_MEASURE:
                    fullLog.append(logString).append(System.getProperty("line.separator"));
                    extractedFeatures.addAll(
                            entry.getValue().stream()
                                    .map(
                                            ifcProperty ->
                                                    Utils.parseNumericFeature(
                                                            ifcProperty, QudtQuantityKind.PRESSURE))
                                    .collect(Collectors.toList()));
                    break;
                case LUMINOUS_FLUX_MEASURE:
                    fullLog.append(logString).append(System.getProperty("line.separator"));
                    extractedFeatures.addAll(
                            entry.getValue().stream()
                                    .map(
                                            ifcProperty ->
                                                    Utils.parseNumericFeature(
                                                            ifcProperty,
                                                            QudtQuantityKind.LUMINOUS_FLUX))
                                    .collect(Collectors.toList()));
                    break;
                case VOLUMETRIC_FLOW_RATE_MEASURE:
                    fullLog.append(logString).append(System.getProperty("line.separator"));
                    extractedFeatures.addAll(
                            entry.getValue().stream()
                                    .map(
                                            ifcProperty ->
                                                    Utils.parseNumericFeature(
                                                            ifcProperty,
                                                            QudtQuantityKind.VOLUME_FLOW_RATE))
                                    .collect(Collectors.toList()));
                    break;
                case LINEAR_FORCE_MEASURE:
                    fullLog.append(logString).append(System.getProperty("line.separator"));
                    extractedFeatures.addAll(
                            entry.getValue().stream()
                                    .map(
                                            ifcProperty ->
                                                    Utils.parseNumericFeature(
                                                            ifcProperty,
                                                            QudtQuantityKind.DIMENSIONLESS))
                                    // TODO: Figure out what LINEAR_FORCE_MEASURE is in
                                    // QUDT.QuantityKind
                                    .collect(Collectors.toList()));
                    break;
                case POWER_MEASURE:
                    fullLog.append(logString).append(System.getProperty("line.separator"));
                    extractedFeatures.addAll(
                            entry.getValue().stream()
                                    .map(
                                            ifcProperty ->
                                                    Utils.parseNumericFeature(
                                                            ifcProperty, QudtQuantityKind.POWER))
                                    .collect(Collectors.toList()));
                    break;
                case LENGTH_MEASURE:
                case POSITIVE_LENGTH_MEASURE:
                    fullLog.append(logString).append(System.getProperty("line.separator"));
                    extractedFeatures.addAll(
                            entry.getValue().stream()
                                    .map(Utils::parseNumericFeature)
                                    .collect(Collectors.toList()));
                    break;
                case VALUELIST:
                    fullLog.append(logString).append(System.getProperty("line.separator"));
                    extractedFeatures.addAll(
                            entry.getValue().stream()
                                    .map(
                                            ifcProperty -> {
                                                EnumFeature f =
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
                                                                        .collect(
                                                                                Collectors
                                                                                        .toList()),
                                                                false);
                                                f.setUniqueValues(
                                                        ifcProperty.getExtractedUniqueValues());
                                                f.setDescriptionFromUniqueValues(
                                                        ifcProperty.getExtractedUniqueValues());
                                                return f;
                                            })
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
