package at.researchstudio.sat.mmsdesktop.logic;

import at.researchstudio.sat.merkmalservice.model.BooleanFeature;
import at.researchstudio.sat.merkmalservice.model.Feature;
import at.researchstudio.sat.merkmalservice.model.NumericFeature;
import at.researchstudio.sat.merkmalservice.model.StringFeature;
import at.researchstudio.sat.mmsdesktop.model.ifc.IfcProperty;
import at.researchstudio.sat.mmsdesktop.model.ifc.IfcUnit;
import at.researchstudio.sat.mmsdesktop.model.ifc.vocab.IfcPropertyType;
import at.researchstudio.sat.mmsdesktop.model.ifc.vocab.IfcUnitType;
import at.researchstudio.sat.mmsdesktop.model.task.ExtractResult;
import at.researchstudio.sat.mmsdesktop.util.FileWrapper;
import at.researchstudio.sat.mmsdesktop.vocab.qudt.QudtQuantityKind;
import at.researchstudio.sat.mmsdesktop.vocab.qudt.QudtUnit;
import javafx.concurrent.Task;
import org.apache.commons.io.FilenameUtils;
import org.apache.jena.ext.com.google.common.base.Throwables;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.rdfhdt.hdt.hdt.HDT;
import org.rdfhdt.hdtjena.HDTGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class PropertyExtractor {
    private static final Logger logger =
                    LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static Task generateIfcFileToJsonTask(boolean keepTempFiles, String outputFileName, List<File> ifcFiles) {
        return new Task<ExtractResult>() {
            @Override public ExtractResult call() {
                String logOutput = "";
                final int max = ifcFiles.size() * 2;
                int hdtDataCount = 0;
                //TODO: Adapt Properties
                // EXTRACTED METHOD
                Map<String, List<HDT>> hdtData = new HashMap<>();
                int i = 0;
                updateProgress(i, max);
                updateTitle("Starting Extraction");
                for (File ifcFile : ifcFiles) {
                    File tempOutputFile =
                                    new File(
                                                    "temp_ttl_"
                                                                    + FilenameUtils.removeExtension(ifcFile.getName())
                                                                    + ".ttl");
                    try {
                        String ifcVersion = FileWrapper.extractIFCVersionFromFile(ifcFile);
                        List<HDT> updatedList = new ArrayList<>();
                        if (!hdtData.isEmpty() && !hdtData.get(ifcVersion).isEmpty()) {
                            updatedList.addAll(hdtData.get(ifcVersion));
                        }
                        updatedList.add(IFC2HDTConverter
                                        .readFromFile(keepTempFiles, ifcFile, tempOutputFile));
                        hdtDataCount++;
                        hdtData.put(ifcVersion, updatedList);
                        logOutput += "Converted " + (++i) + "/" + ifcFiles.size() + " Files to HDT\n";
                        updateMessage(logOutput);
                    } catch (Exception e) {
                        logOutput += "Can't convert file: "
                                        + ifcFile.getAbsolutePath()
                                        + " Reason: "
                                        + e.getMessage()
                                        + "\n";
                        updateMessage(logOutput);
                    }
                    if (isCancelled()) {
                        logOutput += "Operation cancelled by User\n";
                        updateMessage(logOutput);
                        break;
                    }
                    updateProgress(i, max);
                    updateTitle("Converted IFC to HDT, Step " + i + "/" + max);
                }
                List<Feature> extractedFeatures = new ArrayList<>();
                int extractedIfcProperties = 0;
                final int newMax = ifcFiles.size() + hdtData.size();
                for (Map.Entry<String, List<HDT>> hdtMapEntry : hdtData.entrySet()) {
                    try {
                        for (HDT hdt : hdtMapEntry.getValue()) {
                            Map<IfcUnitType, List<IfcUnit>> extractedProjectUnitMap = extractProjectUnits(hdt,
                                            hdtMapEntry.getKey());
                            Map<IfcPropertyType, List<IfcProperty>> extractedPropertyMap = extractPropertiesFromHdtData(
                                            hdt, extractedProjectUnitMap, hdtMapEntry.getKey());
                            extractedIfcProperties += extractedPropertyMap.values().stream()
                                            .mapToInt(Collection::size)
                                            .sum();
                            extractedFeatures.addAll(extractFeaturesFromProperties(extractedPropertyMap));
                        }
                    } catch (IOException ioException) {
                        logOutput += Throwables.getStackTraceAsString(ioException) + "\n";
                        updateMessage(logOutput);
                    }
                    if (isCancelled()) {
                        logOutput += "Operation cancelled by User\n";
                        updateMessage(logOutput);
                        break;
                    }
                    updateProgress(++i, newMax);
                    updateTitle("Extracted Features out of File, Step " + i + "/" + newMax);
                }
                logOutput +=
                                "-------------------------------------------------------------------------------\n" +
                                                "Extracted " + extractedIfcProperties + " out of the " + ifcFiles.size()
                                                + " ifcFiles\n" +
                                                "Parsed " + extractedFeatures.size() + " jsonFeatures\n" +
                                                "into File: " + new File(outputFileName).getAbsolutePath() + "\n" +
                                                "-------------------------------------------------------------------------------\n\n"
                                                + "EXITING, converted " + hdtDataCount + "/" + ifcFiles.size() + "\n";
                updateMessage(logOutput);
                if (hdtDataCount != ifcFiles.size()) {
                    logOutput +=
                                    "Not all Files could be converted, look in the log above to find out why\n";
                    updateMessage(logOutput);
                }
                //EXTRACTED METHOD END
                return new ExtractResult(extractedFeatures, logOutput);
            }
        };
    }

    private static Map<IfcUnitType, List<IfcUnit>> extractProjectUnits(HDT hdtData, String ifcVersion)
                    throws IOException {
        HDTGraph graph = new HDTGraph(hdtData);
        Model model = ModelFactory.createModelForGraph(graph);
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        String query;
        switch (ifcVersion) {
            case "IFC4":
                query = "extract_ifc4_projectunits";
                break;
            case "IFC2X3":
            default:
                query = "extract_ifc2x3_projectunits";
                break;
        }
        Resource resource = resourceLoader.getResource("classpath:" + query + ".rq");
        InputStream inputStream = resource.getInputStream();
        String extractPropNamesQuery = getFileContent(inputStream, StandardCharsets.UTF_8.toString());
        try (QueryExecution qe = QueryExecutionFactory.create(extractPropNamesQuery, model)) {
            ResultSet rs = qe.execSelect();
            List<IfcUnit> extractedUnits = new ArrayList<>();
            while (rs.hasNext()) {
                QuerySolution qs = rs.next();
                extractedUnits.add(new IfcUnit(qs.getResource("unitType"), qs.getResource("unitMeasure")));
            }
            return extractedUnits.stream().collect(Collectors.groupingBy(IfcUnit::getType));
        }
    }

    private static Map<IfcPropertyType, List<IfcProperty>> extractPropertiesFromHdtData(
                    HDT hdtData, Map<IfcUnitType, List<IfcUnit>> projectUnits, String ifcVersion) throws IOException {
        HDTGraph graph = new HDTGraph(hdtData);
        Model model = ModelFactory.createModelForGraph(graph);
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        String query;
        switch (ifcVersion) {
            case "IFC4":
                query = "extract_ifc4_properties";
                break;
            case "IFC2X3":
            default:
                query = "extract_ifc2x3_properties";
                break;
        }
        Resource resource = resourceLoader.getResource("classpath:" + query + ".rq");
        InputStream inputStream = resource.getInputStream();
        String extractPropNamesQuery = getFileContent(inputStream, StandardCharsets.UTF_8.toString());
        try (QueryExecution qe = QueryExecutionFactory.create(extractPropNamesQuery, model)) {
            ResultSet rs = qe.execSelect();
            List<IfcProperty> extractedProperties = new ArrayList<>();
            while (rs.hasNext()) {
                QuerySolution qs = rs.next();
                extractedProperties.add(new IfcProperty(qs, projectUnits));
            }
            return extractedProperties.stream().collect(Collectors.groupingBy(IfcProperty::getType));
        }
    }

    private static List<Feature> extractFeaturesFromProperties(
                    Map<IfcPropertyType, List<IfcProperty>> extractedProperties) {
        List<Feature> extractedFeatures = new ArrayList<>();
        for (Map.Entry<IfcPropertyType, List<IfcProperty>> entry : extractedProperties.entrySet()) {
            IfcPropertyType ifcPropertyType = entry.getKey();
            String logString = entry.getValue().size() + " " + ifcPropertyType + " Properties";
            switch (ifcPropertyType) {
                case EXPRESS_BOOL:
                case BOOL:
                    logger.debug(logString);
                    extractedFeatures.addAll(
                                    entry.getValue().stream()
                                                    .map(ifcProperty -> new BooleanFeature(ifcProperty.getName()))
                                                    .collect(Collectors.toList()));
                    break;
                case TEXT:
                case LABEL:
                    logger.debug(logString);
                    extractedFeatures.addAll(
                                    entry.getValue().stream()
                                                    .map(ifcProperty -> new StringFeature(ifcProperty.getName()))
                                                    .collect(Collectors.toList()));
                    break;
                case VOLUME_MEASURE:
                    logger.debug(logString);
                    extractedFeatures.addAll(
                                    entry.getValue().stream()
                                                    .map(
                                                                    ifcProperty ->
                                                                                    new NumericFeature(
                                                                                                    ifcProperty.getName(),
                                                                                                    QudtQuantityKind.VOLUME,
                                                                                                    QudtUnit.getUnitBasedOnIfcUnitMeasureLengthBasedOnName(
                                                                                                                    ifcProperty.getMeasure())))
                                                    .collect(Collectors.toList()));
                    break;
                case AREA_MEASURE:
                    logger.debug(logString);
                    extractedFeatures.addAll(
                                    entry.getValue().stream()
                                                    .map(
                                                                    ifcProperty ->
                                                                                    new NumericFeature(
                                                                                                    ifcProperty.getName(),
                                                                                                    QudtQuantityKind.AREA,
                                                                                                    QudtUnit.getUnitBasedOnIfcUnitMeasureLengthBasedOnName(
                                                                                                                    ifcProperty.getMeasure())))
                                                    .collect(Collectors.toList()));
                    break;
                case LENGTH_MEASURE:
                case POSITIVE_LENGTH_MEASURE:
                    logger.debug(logString);
                    extractedFeatures.addAll(
                                    entry.getValue().stream()
                                                    .map(
                                                                    ifcProperty ->
                                                                                    new NumericFeature(
                                                                                                    ifcProperty.getName(),
                                                                                                    QudtQuantityKind.getQuantityKindLengthBasedOnName(
                                                                                                                    ifcProperty.getName()),
                                                                                                    QudtUnit.getUnitBasedOnIfcUnitMeasureLengthBasedOnName(
                                                                                                                    ifcProperty.getMeasure())))
                                                    .collect(Collectors.toList()));
                    break;
                default:
                    logger.error(logString + ", will be ignored, no matching Feature-Type determined yet for:");
                    entry.getValue().forEach(property -> logger.error(property.toString()));
                    logger.error("-------------------------------------------------------------------------");
                    break;
            }
        }
        return extractedFeatures;
    }

    private static String getFileContent(
                    InputStream fis, String encoding) throws IOException {
        try (BufferedReader br =
                        new BufferedReader(new InputStreamReader(fis, encoding))) {
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