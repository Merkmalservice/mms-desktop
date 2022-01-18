package at.researchstudio.sat.merkmalservice.ifc.convert;

import static java.util.stream.Collectors.groupingBy;

import at.researchstudio.sat.merkmalservice.ifc.ParsedIfcFile;
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
        ParsedIfcFile result = new ParsedIfcFile(parsedIfcFile);
        int levels = ruleSet.keySet().size();
        List<Integer> orderEntries =
                ruleSet.keySet().stream().sorted().collect(Collectors.toList());
        int level = 0;
        try {
            for (Integer orderEntry : orderEntries) {
                level++;
                Set<ConversionRule> currentRules = ruleSet.get(orderEntry);
                List<ParsedIfcFileModification> modifications = new ArrayList<>();
                int i = 0;
                int lineCount = result.getLines().size();

                for (ConversionRule rule : currentRules) {
                    List<IfcLine> appliedToLines =
                            result.getLines().stream()
                                    .filter(l -> rule.appliesTo(l, result))
                                    .collect(Collectors.toList());

                    if (taskProgressListener != null) {
                        taskProgressListener.notifyProgress(
                                String.format(
                                        "Checking Rule: %s, rule appliesTo %d of %d Lines",
                                        rule.toString(),
                                        appliedToLines.size(),
                                        result.getLines().size()),
                                "",
                                (float) i++ / (float) currentRules.size());
                    }

                    if (!appliedToLines.isEmpty()) {
                        final int ruleNumber = i;
                        appliedToLines.stream()
                                .map(
                                        line -> {
                                            if (taskProgressListener != null) {
                                                taskProgressListener.notifyProgress(
                                                        String.format(
                                                                "Checking Rule: %s, rule appliesTo %d of %d Lines",
                                                                rule.toString(),
                                                                appliedToLines.size(),
                                                                lineCount),
                                                        String.format(
                                                                "applying Rule to line: %s",
                                                                line.toString()),
                                                        (float) ruleNumber
                                                                / (float) currentRules.size());
                                            }
                                            return rule.applyTo(line, result);
                                        })
                                .forEach(modifications::add);
                    }
                }

                i = 0;
                int modCount = modifications.size();
                int reportSize = 10;
                for (ParsedIfcFileModification modification : modifications) {
                    try {
                        modification.accept(result);

                        if (taskProgressListener != null && i % reportSize == 0) {
                            taskProgressListener.notifyProgress(
                                    String.format(
                                            "Applying modifications at level %d of %d ",
                                            level, levels),
                                    String.format("Applied %d of %d", i, modCount),
                                    (float) i / (float) modCount);
                        }
                        i++;
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
