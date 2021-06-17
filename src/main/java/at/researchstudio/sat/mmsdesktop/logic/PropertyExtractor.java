package at.researchstudio.sat.mmsdesktop.logic;

import at.researchstudio.sat.merkmalservice.model.BooleanFeature;
import at.researchstudio.sat.merkmalservice.model.Feature;
import at.researchstudio.sat.merkmalservice.model.NumericFeature;
import at.researchstudio.sat.merkmalservice.model.StringFeature;
import at.researchstudio.sat.mmsdesktop.model.ifc.IfcProperty;
import at.researchstudio.sat.mmsdesktop.model.ifc.IfcUnit;
import at.researchstudio.sat.mmsdesktop.model.ifc.IfcVersion;
import at.researchstudio.sat.mmsdesktop.model.ifc.vocab.IfcPropertyType;
import at.researchstudio.sat.mmsdesktop.model.ifc.vocab.IfcUnitType;
import at.researchstudio.sat.mmsdesktop.model.task.ExtractResult;
import at.researchstudio.sat.mmsdesktop.util.IfcFileWrapper;
import at.researchstudio.sat.mmsdesktop.util.MessageUtils;
import at.researchstudio.sat.mmsdesktop.vocab.qudt.QudtQuantityKind;
import at.researchstudio.sat.mmsdesktop.vocab.qudt.QudtUnit;
import javafx.concurrent.Task;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.LineIterator;
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
import org.springframework.util.StopWatch;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PropertyExtractor {
  private static final Logger logger =
      LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final Pattern propertyExtractPattern = Pattern.compile("^#\\d+= IFCPROPERTYSINGLEVALUE\\('([^']+)',\\$,([A-Z]+)\\('?([^\\)']+)'?\\),\\$\\);\\s*$");
  private static final Pattern unitExtractPattern = Pattern.compile("^#\\d+= IFCSIUNIT\\('([^']+)',\\$,([A-Z]+)\\('?([^\\)']+)'?\\),\\$\\);\\s*$"); //TODO: UPDATE PATTERN

  public static Task generateIfcFileToJsonTask(
      boolean keepTempFiles,
      String outputFileName,
      List<IfcFileWrapper> ifcFiles,
      final ResourceBundle resourceBundle) {
    return new Task<ExtractResult>() {
      @Override
      public ExtractResult call() {
        StringBuilder logOutput = new StringBuilder();

        final int max = ifcFiles.size() * 2;

        // TODO: Adapt Properties
        // EXTRACTED METHOD

        Map<IfcVersion, List<HDT>> hdtData = new HashMap<>();
        int hdtDataCount = 0;
        int i = 0;
        updateTitle(
            MessageUtils.getKeyWithParameters(resourceBundle, "label.extract.process.start"));
        for (IfcFileWrapper ifcFile : ifcFiles) {
          File tempOutputFile =
              new File("temp_ttl_" + FilenameUtils.removeExtension(ifcFile.getName()) + ".ttl");
          try {
            List<HDT> updatedList = new ArrayList<>();
            if (!hdtData.isEmpty() && !hdtData.get(ifcFile.getIfcVersion()).isEmpty()) {
              updatedList.addAll(hdtData.get(ifcFile.getIfcVersion()));
            }
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            // TODO: REMOVE THIS; THIS IS JUST A TEST ******************************************
            logOutput.append("Reading " + (i + 1) + "/" + ifcFiles.size() + " Files to Lines\n");
            updateMessage(logOutput.toString());
            // TODO: REMOVE THIS; THIS IS JUST A TEST ******************************************


            // now read the file line by line...
            int lineNum = 0;

            int sumHashes = 0;

            Set<IfcProperty> features = new HashSet<>();

            try (LineIterator it = FileUtils.lineIterator(ifcFile.getFile(), StandardCharsets.UTF_8.toString())) {
              while (it.hasNext()) {
                String line = it.nextLine();
                sumHashes += line.hashCode();
                lineNum++;
                // do something with line
                if(line.contains("IFCPROPERTYSINGLEVALUE")) {
                  Matcher matcher = propertyExtractPattern.matcher(line);

                  if(matcher.find()) {
                    String name = matcher.group(1); //NAME
                    String type = matcher.group(2); //TYPE
                    String value = matcher.group(3); //VALUE
                    features.add(new IfcProperty(name, type));
                  }
                }
              }
            }

            logOutput.append("extractedFeatures: ").append(features.size()).append("\n");

            stopWatch.stop();
            logOutput
                .append(sumHashes)
                .append("\n")
                .append(stopWatch.getTotalTimeSeconds())
                .append("\n")
                .append(stopWatch.prettyPrint())
                .append("\n");
            updateMessage(logOutput.toString());

            // ********************************************************************************

            logOutput.append(
                "Reading " + (i + 1) + "/" + ifcFiles.size() + " Files to Lines finished\n");
            updateMessage(logOutput.toString());
            // ********************************************************************************

            // updatedList.add(IFC2HDTConverter.readFromFile(keepTempFiles, ifcFile.getFile(),
            // tempOutputFile));
            hdtDataCount++;
            hdtData.put(ifcFile.getIfcVersion(), updatedList);
            logOutput.append("Converted " + (++i) + "/" + ifcFiles.size() + " Files to HDT\n");
            updateMessage(logOutput.toString());
          } catch (Exception e) {
            logOutput.append(
                "Can't convert file: " + ifcFile.getPath() + " Reason: " + e.getMessage() + "\n");
            updateMessage(logOutput.toString());
          }
          if (isCancelled()) {
            logOutput.append("Operation cancelled by User\n");
            updateMessage(logOutput.toString());
            break;
          }
          updateProgress(i, max);
          updateTitle(
              MessageUtils.getKeyWithParameters(
                  resourceBundle, "label.extract.process.ifc2hdt", i, max));
        }

        List<Feature> extractedFeatures = new ArrayList<>();
        int extractedIfcProperties = 0;

        final int newMax = ifcFiles.size() + hdtData.size();
        for (Map.Entry<IfcVersion, List<HDT>> hdtMapEntry : hdtData.entrySet()) {
          for (HDT hdt : hdtMapEntry.getValue()) {
            try {
              Map<IfcUnitType, List<IfcUnit>> extractedProjectUnitMap =
                  extractProjectUnits(hdt, hdtMapEntry.getKey());
              Map<IfcPropertyType, List<IfcProperty>> extractedPropertyMap =
                  extractPropertiesFromHdtData(hdt, extractedProjectUnitMap, hdtMapEntry.getKey());
              extractedIfcProperties +=
                  extractedPropertyMap.values().stream().mapToInt(Collection::size).sum();
              ExtractResult partialExtractResult =
                  extractFeaturesFromProperties(extractedPropertyMap);
              extractedFeatures.addAll(partialExtractResult.getExtractedFeatures());
              logOutput.append(partialExtractResult.getLogOutput());
            } catch (IOException ioException) {
              logOutput.append(Throwables.getStackTraceAsString(ioException) + "\n");
            }
            updateMessage(logOutput.toString());
            if (isCancelled()) {
              logOutput.append("Operation cancelled by User\n");
              updateMessage(logOutput.toString());
              break;
            }
            updateProgress(++i, newMax);
            updateTitle(
                MessageUtils.getKeyWithParameters(
                    resourceBundle, "label.extract.process.features", i, newMax));
          }
        }

        logOutput.append(
            "-------------------------------------------------------------------------------\n"
                + "Extracted "
                + extractedIfcProperties
                + " out of the "
                + ifcFiles.size()
                + " ifcFiles\n"
                + "Parsed "
                + extractedFeatures.size()
                + " jsonFeatures\n"
                + "into File: "
                + new File(outputFileName).getAbsolutePath()
                + "\n"
                + "-------------------------------------------------------------------------------\n\n"
                + "EXITING, converted "
                + hdtDataCount
                + "/"
                + ifcFiles.size()
                + "\n");
        updateMessage(logOutput.toString());
        if (hdtDataCount != ifcFiles.size()) {
          logOutput.append(
              "Not all Files could be converted, look in the log above to find out why\n");
          updateMessage(logOutput.toString());
        }

        // EXTRACTED METHOD END
        return new ExtractResult(extractedFeatures, logOutput.toString());
      }
    };
  }

  private static Map<IfcUnitType, List<IfcUnit>> extractProjectUnits(
      HDT hdtData, IfcVersion ifcVersion) throws IOException {
    HDTGraph graph = new HDTGraph(hdtData);
    Model model = ModelFactory.createModelForGraph(graph);
    ResourceLoader resourceLoader = new DefaultResourceLoader();
    String query;
    switch (ifcVersion) {
      case IFC4:
        query = "extract_ifc4_projectunits";
        break;
      case IFC2X3:
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
      HDT hdtData, Map<IfcUnitType, List<IfcUnit>> projectUnits, IfcVersion ifcVersion)
      throws IOException {
    HDTGraph graph = new HDTGraph(hdtData);
    Model model = ModelFactory.createModelForGraph(graph);
    ResourceLoader resourceLoader = new DefaultResourceLoader();
    String query;
    switch (ifcVersion) {
      case IFC4:
        query = "extract_ifc4_properties";
        break;
      case IFC2X3:
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

  private static ExtractResult extractFeaturesFromProperties(
      Map<IfcPropertyType, List<IfcProperty>> extractedProperties) {
    List<Feature> extractedFeatures = new ArrayList<>();
    StringBuilder fullLog = new StringBuilder();
    for (Map.Entry<IfcPropertyType, List<IfcProperty>> entry : extractedProperties.entrySet()) {
      IfcPropertyType ifcPropertyType = entry.getKey();
      String logString = entry.getValue().size() + " " + ifcPropertyType + " Properties";
      switch (ifcPropertyType) {
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
                              QudtUnit.getUnitBasedOnIfcUnitMeasureLengthBasedOnName(
                                  ifcProperty.getMeasure())))
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
                              QudtUnit.getUnitBasedOnIfcUnitMeasureLengthBasedOnName(
                                  ifcProperty.getMeasure())))
                  .collect(Collectors.toList()));
          break;
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
        case LENGTH_MEASURE:
        case POSITIVE_LENGTH_MEASURE:
          fullLog.append(logString).append(System.getProperty("line.separator"));
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
          fullLog
              .append(logString)
              .append(", will be ignored, no matching Feature-Type determined yet for:")
              .append(System.getProperty("line.separator"));
          entry
              .getValue()
              .forEach(
                  property ->
                      fullLog
                          .append(property.toString())
                          .append(System.getProperty("line.separator")));
          fullLog
              .append("-------------------------------------------------------------------------")
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
