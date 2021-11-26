package at.researchstudio.sat.merkmalservice.ifc.convert;

import static java.util.stream.Collectors.groupingBy;

import at.researchstudio.sat.merkmalservice.ifc.ParsedIfcFile;
import at.researchstudio.sat.merkmalservice.support.progress.TaskProgressListener;
import java.util.*;
import java.util.stream.Collectors;

public class ConversionEngine {
    private Map<Integer, Set<ConversionRule>> ruleSet;
    private TaskProgressListener taskProgressListener;

    public ConversionEngine(Collection<ConversionRule> rules) {
        this(rules, null);
    }

    public ConversionEngine(
            Collection<ConversionRule> rules, TaskProgressListener taskProgressListener) {
        Objects.requireNonNull(rules);
        this.taskProgressListener = taskProgressListener;
        this.ruleSet =
                rules.stream().collect(groupingBy(ConversionRule::getOrder, Collectors.toSet()));
    }

    public ParsedIfcFile convert(ParsedIfcFile parsedIfcFile) {
        Objects.requireNonNull(parsedIfcFile);
        ParsedIfcFile result = new ParsedIfcFile(parsedIfcFile);
        int levels = ruleSet.keySet().size();
        List<Integer> orderEntries =
                ruleSet.keySet().stream().sorted().collect(Collectors.toList());
        int level = 0;
        for (Integer orderEntry : orderEntries) {
            Set<ConversionRule> currentRules = ruleSet.get(orderEntry);
            List<ParsedIfcFileModification> modifications =
                    result.getLines().stream()
                            .flatMap(
                                    line ->
                                            currentRules.stream()
                                                    .filter(rule -> rule.appliesTo(line, result))
                                                    .map(rule -> rule.applyTo(line, result)))
                            .collect(Collectors.toList());
            modifications.stream().forEach(mod -> mod.accept(result));
            level++;
            if (taskProgressListener != null) {
                float progress = (float) level / (float) levels;
                taskProgressListener.notifyProgress(
                        "Conversion",
                        String.format("Applied rule level %d/%d", level, levels),
                        progress);
            }
        }
        if (taskProgressListener != null) {
            taskProgressListener.notifyFinished("Conversion");
        }
        return result;
    }
}
