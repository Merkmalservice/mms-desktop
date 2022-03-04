package at.researchstudio.sat.merkmalservice.ifc.convert;

import static java.util.stream.Collectors.groupingBy;

import at.researchstudio.sat.merkmalservice.ifc.IfcFileReader;
import at.researchstudio.sat.merkmalservice.ifc.ParsedIfcFile;
import at.researchstudio.sat.merkmalservice.ifc.convert.support.change.HighlevelChange;
import at.researchstudio.sat.merkmalservice.ifc.convert.support.modification.ElementModification;
import at.researchstudio.sat.merkmalservice.ifc.model.IfcLine;
import at.researchstudio.sat.merkmalservice.support.progress.TaskProgressListener;
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConversionEngine {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private Map<Integer, Set<ConversionRule>> ruleSet;

    public ConversionEngine(Collection<ConversionRule> rules) {
        Objects.requireNonNull(rules);
        this.ruleSet =
                rules.stream().collect(groupingBy(ConversionRule::getOrder, Collectors.toSet()));
    }

    public ParsedIfcFile convert(ParsedIfcFile parsedIfcFile) {
        return convert(parsedIfcFile, null);
    }

    public ParsedIfcFile convert(
            ParsedIfcFile parsedIfcFile, TaskProgressListener taskProgressListener) {
        Objects.requireNonNull(parsedIfcFile);
        if (taskProgressListener != null) {
            taskProgressListener.notifyProgress("Cloning IFC Model...", "", 0);
        }
        final ParsedIfcFile result =
                IfcFileReader.cloneIfcFile(parsedIfcFile, taskProgressListener);

        int levels = ruleSet.keySet().size();
        List<Integer> orderEntries =
                ruleSet.keySet().stream().sorted().collect(Collectors.toList());
        int level = 0;
        try {
            for (Integer orderEntry : orderEntries) {
                level++;
                Set<ConversionRule> currentRules = ruleSet.get(orderEntry);
                List<ParsedIfcFileModification> modifications = new ArrayList<>();
                int ruleIndex = 0;
                int lineCount = result.getLines().size();

                for (ConversionRule rule : currentRules) {
                    ruleIndex++;
                    Set<Class<? extends IfcLine>> typeRestrictions = rule.getIfcTypeRestrictions();
                    Set<Class<? extends IfcLine>> applicableToClasses =
                            result.getDataLinesByClass().keySet();
                    if (taskProgressListener != null) {
                        taskProgressListener.notifyProgress(
                                String.format(
                                        "Checking Rule: %s - identifying elements that match the condition",
                                        rule.toString()),
                                "",
                                (float) ruleIndex / (float) currentRules.size());
                    }
                    if (!typeRestrictions.isEmpty()) {
                        applicableToClasses =
                                applicableToClasses.stream()
                                        .filter(
                                                t ->
                                                        typeRestrictions.stream()
                                                                .anyMatch(
                                                                        r -> r.isAssignableFrom(t)))
                                        .collect(Collectors.toSet());
                    }

                    Set<IfcLine> appliedToLines =
                            applicableToClasses.stream()
                                    .flatMap(
                                            ifcClass ->
                                                    result.getDataLinesByClass(ifcClass).stream())
                                    .filter(l -> rule.appliesTo(l, result))
                                    .collect(Collectors.toSet());

                    if (taskProgressListener != null) {
                        taskProgressListener.notifyProgress(
                                String.format(
                                        "Checking Rule: %s, rule applies to nor more than %d of %d Lines",
                                        rule.toString(),
                                        appliedToLines.size(),
                                        result.getLines().size()),
                                "",
                                (float) ruleIndex / (float) currentRules.size());
                    }

                    if (!appliedToLines.isEmpty()) {
                        final int finalRuleIndex = ruleIndex;
                        int lineIndex = 0;
                        int reportSize = 100;
                        int modSizeAtStart = modifications.size();
                        for (IfcLine line: appliedToLines) {
                            lineIndex++;
                            if (taskProgressListener != null) {
                                if (lineIndex % reportSize == 0) {
                                    taskProgressListener.notifyProgress(
                                                    String.format(
                                                                    "Checking Rule: %s, processed %d of %d (collected %d modifications so far)",
                                                                    rule.toString(),
                                                                    lineIndex,
                                                                    appliedToLines.size(),
                                                                    modifications.size() - modSizeAtStart),
                                                    "",
                                                    (float) appliedToLines.size()
                                                                    / (float) lineIndex);
                                }
                            }
                            try {
                                modifications.addAll(rule.applyTo(line, result));
                            } catch (Exception e) {
                                logger.warn(
                                        "Error generating modification of line "
                                                + line.getId()
                                                + " by rule "
                                                + rule.toString()
                                                + ": "
                                                + e.getMessage(),
                                        e);
                            }
                        }
                    }
                }

                int modificationIndex = 0;
                int totalModificationCount = modifications.size();
                int reportSize = 100;
                int changeCount = 0;
                for (ParsedIfcFileModification modification : modifications) {
                    try {
                        List<HighlevelChange> highlevelChanges = modification.accept(result);
                        if (!highlevelChanges.isEmpty()) {
                            changeCount += highlevelChanges.stream().map(HighlevelChange::getLowlevelChanges).flatMap(Collection::stream).count();
                            result.addChanges(highlevelChanges);
                        }
                        if (taskProgressListener != null && modificationIndex % reportSize == 0) {
                            taskProgressListener.notifyProgress(
                                    String.format(
                                            "Level %d of %d: Processed %d of %d modifications, made %d actual changes so far",  level, levels, modificationIndex, totalModificationCount, changeCount),
                                    "",
                                    (float) modificationIndex / (float) totalModificationCount);
                        }
                        modificationIndex++;
                    } catch (Exception e) {
                        logger.info("Error during conversion: {}", e.getMessage(), e);
                    }
                }
                if (taskProgressListener != null) {
                    float progress = (float) level / (float) levels;
                    taskProgressListener.notifyProgress(
                            "Conversion",
                            String.format("Applied rules at level %d of %d", level, levels),
                            progress);
                }
            }
            if (taskProgressListener != null) {
                taskProgressListener.notifyFinished("Conversion");
            }
        } catch (Throwable t) {
            if (taskProgressListener != null) {
                taskProgressListener.notifyFailed(t.getClass() + ": " + t.getMessage());
            }
            logger.warn("Error during conversion: {}", t.getMessage(), t);
            throw t;
        }
        return result;
    }
}
