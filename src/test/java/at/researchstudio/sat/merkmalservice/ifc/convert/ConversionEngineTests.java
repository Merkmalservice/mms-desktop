package at.researchstudio.sat.merkmalservice.ifc.convert;

import static at.researchstudio.sat.merkmalservice.ifc.convert.support.modification.Modification.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import at.researchstudio.sat.merkmalservice.ifc.IfcFileReader;
import at.researchstudio.sat.merkmalservice.ifc.IfcFileWrapper;
import at.researchstudio.sat.merkmalservice.ifc.IfcFileWriter;
import at.researchstudio.sat.merkmalservice.ifc.ParsedIfcFile;
import at.researchstudio.sat.merkmalservice.ifc.convert.support.DefaultIfcFileConversionConfig;
import at.researchstudio.sat.merkmalservice.ifc.model.IfcLine;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

public class ConversionEngineTests {
    private static final File testResources =
            new File("src/test/resources/at/researchstudio/sat/merkmalservice/ifc/convert/");
    private static final File inoutFolder = new File(testResources, "inout");
    private static final File targetFolder =
            new File("target/test-output/at/researchstudio/sat/merkmalservice/ifc/convert/");

    public void testInOut(String folderName, ConversionEngine engine) throws IOException {
        File folder = new File(inoutFolder, folderName);
        ParsedIfcFile parsedIfcFile = parseFile(new File(folder, "input.ifc"));
        ParsedIfcFile output = engine.convert(parsedIfcFile);
        String expectedOutput =
                Files.readString(Path.of(new File(folder, "expectedOutput.ifc").getAbsolutePath()))
                        .replaceAll("\r\n", "\n");
        StringWriter sw = new StringWriter();
        IfcFileWriter.write(output, sw);
        String actualOutput = sw.toString().replaceAll("\r\n", "\n");
        try {
            Assertions.assertEquals(expectedOutput, actualOutput);
        } catch (AssertionFailedError e) {
            File outFolder = new File(targetFolder, folderName);
            outFolder.mkdirs();
            IfcFileWriter.write(output, new File(outFolder, "output.ifc"));
            throw e;
        }
    }

    @Test
    public void testNoChanges() throws IOException {
        testInOut("no_change", new ConversionEngine(Collections.emptyList()));
    }

    @Test
    public void testRead() throws IOException {
        ParsedIfcFile parsedIfcFile = loadTestFile1();
        assertEquals(421, parsedIfcFile.getLines().size());
        assertEquals(3, parsedIfcFile.getBuiltElementLines().size());
        assertEquals(19, parsedIfcFile.getDataLinesByClass().size());
        assertEquals(81, parsedIfcFile.getExtractedProperties().size());
        assertEquals(9, parsedIfcFile.getExtractedPropertyMap().size());
    }

    @Test
    public void testDeleteOneProperty1() throws IOException {
        ParsedIfcFile parsedIfcFile = loadTestFile1();
        IfcFileConversionConfig config =
                new DefaultIfcFileConversionConfig(
                        Set.of(
                                new ConversionRule() {
                                    @Override
                                    public int getOrder() {
                                        return 0;
                                    }

                                    @Override
                                    public boolean appliesTo(IfcLine line, ParsedIfcFile ifcModel) {
                                        return true;
                                    }

                                    @Override
                                    public ParsedIfcFileModification applyTo(
                                            IfcLine line, ParsedIfcFile ifcModel) {
                                        return removePropertyWithName("Versatz unten", line);
                                    }
                                }));
        ConversionEngine engine = new ConversionEngine(config.getConversionRules());
        testInOut("delete_property_single_value_versatz_unten", engine);
    }

    @Test
    public void testDeleteAllProperties() throws IOException {
        ParsedIfcFile parsedIfcFile = loadTestFile1();
        IfcFileConversionConfig config =
                new DefaultIfcFileConversionConfig(
                        Set.of(
                                new ConversionRule() {
                                    @Override
                                    public int getOrder() {
                                        return 0;
                                    }

                                    @Override
                                    public boolean appliesTo(IfcLine line, ParsedIfcFile ifcModel) {
                                        return true;
                                    }

                                    @Override
                                    public ParsedIfcFileModification applyTo(
                                            IfcLine line, ParsedIfcFile ifcModel) {
                                        return multiple(
                                                removePropertyWithMatchingName(".*", line),
                                                removeQuantityWithMatchingName(".*", line));
                                    }
                                }));
        ConversionEngine engine = new ConversionEngine(config.getConversionRules());
        testInOut("delete_all_properties_and_quantities", engine);
    }

    @NotNull
    private ParsedIfcFile loadTestFile1() throws IOException {
        File testFile = new File(testResources, "IFC test.ifc");
        return parseFile(testFile);
    }

    @NotNull
    private ParsedIfcFile parseFile(File testFile) throws IOException {
        IfcFileWrapper wrapper = new IfcFileWrapper(testFile);
        return IfcFileReader.readIfcFile(wrapper);
    }
}
