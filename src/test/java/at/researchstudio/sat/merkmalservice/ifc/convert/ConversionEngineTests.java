package at.researchstudio.sat.merkmalservice.ifc.convert;

import static at.researchstudio.sat.merkmalservice.ifc.convert.support.modification.Modification.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import at.researchstudio.sat.merkmalservice.ifc.IfcFileReader;
import at.researchstudio.sat.merkmalservice.ifc.IfcFileWrapper;
import at.researchstudio.sat.merkmalservice.ifc.IfcFileWriter;
import at.researchstudio.sat.merkmalservice.ifc.ParsedIfcFile;
import at.researchstudio.sat.merkmalservice.ifc.convert.support.MappingConversionRuleFactory;
import at.researchstudio.sat.merkmalservice.ifc.model.IfcLine;
import at.researchstudio.sat.merkmalservice.model.Organization;
import at.researchstudio.sat.merkmalservice.model.Project;
import at.researchstudio.sat.merkmalservice.model.Standard;
import at.researchstudio.sat.merkmalservice.model.mapping.*;
import at.researchstudio.sat.merkmalservice.model.mapping.feature.Feature;
import at.researchstudio.sat.merkmalservice.model.mapping.feature.featuretype.BooleanFeatureType;
import at.researchstudio.sat.merkmalservice.model.mapping.feature.featuretype.NumericFeatureType;
import at.researchstudio.sat.merkmalservice.model.mapping.feature.featuretype.StringFeatureType;
import at.researchstudio.sat.merkmalservice.vocab.qudt.QudtQuantityKind;
import at.researchstudio.sat.merkmalservice.vocab.qudt.QudtUnit;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
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
    public void test_delete_IFCLENGTHMEASURE_one() throws IOException {
        ConversionRuleFactory factory =
                () ->
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
                                });
        ConversionEngine engine = new ConversionEngine(factory.getRules());
        testInOut("delete_property_versatz_unten", engine);
    }

    @Test
    public void test_delete_IFCTEXT_one() throws IOException {
        ConversionRuleFactory factory =
                () ->
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
                                        return removePropertyWithName("cpiFitMatchKey", line);
                                    }
                                });
        ConversionEngine engine = new ConversionEngine(factory.getRules());
        testInOut("delete_property_cpiFitMatchKey", engine);
    }

    @Test
    public void test_delete_allProperties() throws IOException {
        ConversionRuleFactory factory =
                () ->
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
                                });
        ConversionEngine engine = new ConversionEngine(factory.getRules());
        testInOut("delete_all_properties_and_quantities", engine);
    }

    @Test
    public void test_delete_allproperties_byobjecttype() throws IOException {
        ConversionRuleFactory factory =
                () ->
                        Set.of(
                                new ConversionRule() {
                                    @Override
                                    public int getOrder() {
                                        return 0;
                                    }

                                    @Override
                                    public boolean appliesTo(IfcLine line, ParsedIfcFile ifcModel) {
                                        return "IFCWALLSTANDARDCASE".equals(line.getType());
                                    }

                                    @Override
                                    public ParsedIfcFileModification applyTo(
                                            IfcLine line, ParsedIfcFile ifcModel) {
                                        return multiple(
                                                removePropertyWithMatchingName(".*", line),
                                                removeQuantityWithMatchingName(".*", line));
                                    }
                                });
        ConversionEngine engine = new ConversionEngine(factory.getRules());
        testInOut("delete_all_properties_of_one_wall", engine);
    }

    @Test
    public void test_delete_IFCTEXT_present() throws IOException {
        MappingConversionRuleFactory factory =
                new MappingConversionRuleFactory(
                        List.of(
                                delete(
                                        "byPresent",
                                        Inst.featureCpiFitMatchKey,
                                        MappingPredicate.PRESENT,
                                        null)));
        ConversionEngine engine = new ConversionEngine(factory.getRules());
        testInOut("delete_property_cpiFitMatchKey", engine);
    }

    @Test
    public void test_delete_IFCTEXT_contains() throws IOException {
        MappingConversionRuleFactory factory =
                new MappingConversionRuleFactory(
                        List.of(
                                delete(
                                        "byContains",
                                        Inst.featureCpiFitMatchKey,
                                        MappingPredicate.CONTAINS,
                                        new MappingExecutionValue("ABC"))));
        ConversionEngine engine = new ConversionEngine(factory.getRules());
        testInOut("delete_property_cpiFitMatchKey_containing_string", engine);
    }

    @Test
    public void test_delete_IFCTEXT_contains_not() throws IOException {
        MappingConversionRuleFactory factory =
                new MappingConversionRuleFactory(
                        List.of(
                                delete(
                                        "byContainsNot",
                                        Inst.featureCpiFitMatchKey,
                                        MappingPredicate.CONTAINS_NOT,
                                        new MappingExecutionValue("ABC"))));
        ConversionEngine engine = new ConversionEngine(factory.getRules());
        testInOut("delete_property_cpiFitMatchKey_2_instances", engine);
    }

    @Test
    public void test_delete_IFCTEXT_contains_notFound() throws IOException {
        MappingConversionRuleFactory factory =
                new MappingConversionRuleFactory(
                        List.of(
                                delete(
                                        "byContains",
                                        Inst.featureCpiFitMatchKey,
                                        MappingPredicate.CONTAINS,
                                        new MappingExecutionValue("DO-NOT-FIND-THIS"))));
        ConversionEngine engine = new ConversionEngine(factory.getRules());
        testInOut("no_change", engine);
    }

    @Test
    public void test_delete_IFCTEXT_matches() throws IOException {
        MappingConversionRuleFactory factory =
                new MappingConversionRuleFactory(
                        List.of(
                                delete(
                                        "byMatches",
                                        Inst.featureCpiFitMatchKey,
                                        MappingPredicate.MATCHES,
                                        new MappingExecutionValue("^AB(\\.|C)[DEFG]+$"))));
        ConversionEngine engine = new ConversionEngine(factory.getRules());
        testInOut("delete_property_cpiFitMatchKey_containing_string", engine);
    }

    @Test
    public void test_delete_IFCTEXT_equals() throws IOException {
        MappingConversionRuleFactory factory =
                new MappingConversionRuleFactory(
                        List.of(
                                delete(
                                        "byEquals",
                                        Inst.featureCpiFitMatchKey,
                                        MappingPredicate.EQUALS,
                                        new MappingExecutionValue("ABCDEFG"))));
        ConversionEngine engine = new ConversionEngine(factory.getRules());
        testInOut("delete_property_cpiFitMatchKey_containing_string", engine);
    }

    @Test
    public void test_delete_IFCTEXT_equals_not() throws IOException {
        MappingConversionRuleFactory factory =
                new MappingConversionRuleFactory(
                        List.of(
                                delete(
                                        "byEquals",
                                        Inst.featureCpiFitMatchKey,
                                        MappingPredicate.NOT,
                                        new MappingExecutionValue("ABCDEFG"))));
        ConversionEngine engine = new ConversionEngine(factory.getRules());
        testInOut("delete_property_cpiFitMatchKey_2_instances", engine);
    }

    @Test
    public void test_delete_IFCTEXT_equals_notFound() throws IOException {
        MappingConversionRuleFactory factory =
                new MappingConversionRuleFactory(
                        List.of(
                                delete(
                                        "byEquals",
                                        Inst.featureCpiFitMatchKey,
                                        MappingPredicate.EQUALS,
                                        new MappingExecutionValue("DO-NOT-FIND-THIS"))));
        ConversionEngine engine = new ConversionEngine(factory.getRules());
        testInOut("no_change", engine);
    }

    @Test
    public void test_delete_IFCTEXT_matches_notFound() throws IOException {
        MappingConversionRuleFactory factory =
                new MappingConversionRuleFactory(
                        List.of(
                                delete(
                                        "byMatches",
                                        Inst.featureCpiFitMatchKey,
                                        MappingPredicate.MATCHES,
                                        new MappingExecutionValue("^DO-NOT-FIND-THIS$"))));
        ConversionEngine engine = new ConversionEngine(factory.getRules());
        testInOut("no_change", engine);
    }

    @Test
    public void test_delete_IFCBOOLEAN_equals() throws IOException {
        MappingConversionRuleFactory factory =
                new MappingConversionRuleFactory(
                        List.of(
                                delete(
                                        "byEquals",
                                        Inst.featureRaumbegrenzung,
                                        MappingPredicate.EQUALS,
                                        new MappingExecutionValue(true))));
        ConversionEngine engine = new ConversionEngine(factory.getRules());
        testInOut("delete_property_raumbegrenzung", engine);
    }

    @Test
    public void test_delete_IFCBOOLEAN_equals_notFound() throws IOException {
        MappingConversionRuleFactory factory =
                new MappingConversionRuleFactory(
                        List.of(
                                delete(
                                        "byEquals",
                                        Inst.featureRaumbegrenzung,
                                        MappingPredicate.EQUALS,
                                        new MappingExecutionValue(false))));
        ConversionEngine engine = new ConversionEngine(factory.getRules());
        testInOut("no_change", engine);
    }

    @Test
    public void test_delete_IFCBOOLEAN_equals_not() throws IOException {
        MappingConversionRuleFactory factory =
                new MappingConversionRuleFactory(
                        List.of(
                                delete(
                                        "byEqualsNot",
                                        Inst.featureRaumbegrenzung,
                                        MappingPredicate.NOT,
                                        new MappingExecutionValue(false))));
        ConversionEngine engine = new ConversionEngine(factory.getRules());
        testInOut("delete_property_raumbegrenzung", engine);
    }

    @Test
    public void test_delete_IFCBOOLEAN_equals_not_notFound() throws IOException {
        MappingConversionRuleFactory factory =
                new MappingConversionRuleFactory(
                        List.of(
                                delete(
                                        "byEqualsNot",
                                        Inst.featureRaumbegrenzung,
                                        MappingPredicate.NOT,
                                        new MappingExecutionValue(true))));
        ConversionEngine engine = new ConversionEngine(factory.getRules());
        testInOut("no_change", engine);
    }

    @Test
    public void test_delete_IFCBOOLEAN_greaterThan() throws IOException {
        MappingConversionRuleFactory factory =
                new MappingConversionRuleFactory(
                        List.of(
                                delete(
                                        "byGreaterThan",
                                        Inst.featureRaumbegrenzung,
                                        MappingPredicate.GREATER_THAN,
                                        new MappingExecutionValue(true))));
        ConversionEngine engine = new ConversionEngine(factory.getRules());
        testInOut("no_change", engine);
    }

    @Test
    public void test_delete_IFCINTEGER_greaterThan_walltypeproperty() throws IOException {
        MappingConversionRuleFactory factory =
                new MappingConversionRuleFactory(
                        List.of(
                                delete(
                                        "byGreaterThan",
                                        Inst.featureRauigkeit,
                                        MappingPredicate.GREATER_THAN,
                                        new MappingExecutionValue(1))));
        ConversionEngine engine = new ConversionEngine(factory.getRules());
        testInOut("delete_property_rauigkeit", engine);
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

    private static class Inst {
        static Feature featureCpiFitMatchKey =
                new Feature(
                        "feature1Id",
                        "cpiFitMatchKey",
                        "the match key",
                        List.of(),
                        new StringFeatureType());
        static Feature featureRaumbegrenzung =
                new Feature(
                        "feature2Id",
                        "Raumbegrenzung",
                        "Raumbegrenzung oder nicht",
                        List.of(),
                        new BooleanFeatureType());
        static Feature featureRauigkeit =
                new Feature(
                        "feature3Id",
                        "Rauigkeit",
                        "Indikator für Rauigkeit",
                        List.of(),
                        new NumericFeatureType(QudtQuantityKind.DIMENSIONLESS, QudtUnit.UNITLESS));
        static Organization organization1 = new Organization("org1Id", "Org one");
        static Project project1 =
                new Project("project1Id", "Project One", "a project", List.of(), List.of());
        static Standard standard1 =
                new Standard("standard1Id", "standard one", false, organization1);
        static Mapping mappingDeleteCpiFitMatchKey =
                new Mapping(
                        "mapping1Id",
                        "Delete cpiFitMatchKey",
                        project1,
                        List.of(),
                        new SingleCondition(
                                "singleCondition1Id",
                                featureCpiFitMatchKey,
                                MappingPredicate.PRESENT,
                                null),
                        List.of(
                                new DeleteActionGroup(
                                        List.of(
                                                new DeleteAction(
                                                        "deleteAction1Id",
                                                        featureCpiFitMatchKey)))));
    }

    private Mapping delete(
            String name, Feature feature, MappingPredicate predicate, MappingExecutionValue value) {
        return new Mapping(
                "mappingIdDelete" + name + feature.getName(),
                "DeleteMapping " + name + " " + feature.getName(),
                Inst.project1,
                List.of(),
                new SingleCondition(
                        "singleCondition" + name + feature.getName(), feature, predicate, value),
                List.of(
                        new DeleteActionGroup(
                                List.of(new DeleteAction("actionId" + name, feature)))));
    }
}
