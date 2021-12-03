package at.researchstudio.sat.merkmalservice.ifc.convert.support;

import static java.util.stream.Collectors.toList;

import at.researchstudio.sat.merkmalservice.ifc.ParsedIfcFile;
import at.researchstudio.sat.merkmalservice.ifc.convert.ConversionRule;
import at.researchstudio.sat.merkmalservice.ifc.convert.ConversionRuleFactory;
import at.researchstudio.sat.merkmalservice.ifc.convert.ParsedIfcFileModification;
import at.researchstudio.sat.merkmalservice.ifc.convert.support.modification.Modification;
import at.researchstudio.sat.merkmalservice.ifc.model.IfcLine;
import at.researchstudio.sat.merkmalservice.ifc.support.IfcLinePredicates;
import at.researchstudio.sat.merkmalservice.model.mapping.*;
import at.researchstudio.sat.merkmalservice.model.mapping.feature.Feature;
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MappingConversionRuleFactory implements ConversionRuleFactory {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private Collection<Mapping> mappings;

    public MappingConversionRuleFactory(Collection<Mapping> mappings) {
        this.mappings = new ArrayList<>(mappings);
    }

    @Override
    public Collection<ConversionRule> getRules() {
        return mappings.stream().map(m -> toConversionRule(m)).collect(toList());
    }

    private ConversionRule toConversionRule(Mapping mapping) {
        Predicate<IfcLineAndModel> ruleCondition =
                mapping.getCondition() == null
                        ? line -> true
                        : buildRulePredicate(mapping.getCondition());
        return new ConversionRule() {
            @Override
            public int getOrder() {
                return 0;
            }

            @Override
            public boolean appliesTo(IfcLine line, ParsedIfcFile ifcModel) {
                return ruleCondition.test(new IfcLineAndModel(line, ifcModel));
            }

            @Override
            public ParsedIfcFileModification applyTo(IfcLine line, ParsedIfcFile ifcModel) {
                List<ActionGroup<? extends Action>> actionGroups =
                        Optional.ofNullable(mapping.getActionGroups())
                                .orElse(Collections.emptyList());
                Stream<ParsedIfcFileModification> modifications =
                        actionGroups.stream()
                                .flatMap(group -> group.getActions().stream())
                                .map(a -> makeModification(a, line));
                return Modification.multiple(modifications);
            }
        };
    }

    private ParsedIfcFileModification makeModification(Action action, IfcLine line) {
        if (action instanceof DeleteAction) {
            return Modification.removePropertyWithName(
                    ((DeleteAction) action).getFeature().getName(), line);
        }
        if (action instanceof AddAction) {
            throw new UnsupportedOperationException("TODO implement Modifications for AddAction!");
        }
        throw new IllegalStateException(
                "Cannot generate modification for action of type " + action.getClass().getName());
    }

    private Predicate<IfcLineAndModel> buildRulePredicate(Condition condition) {
        if (condition instanceof SingleCondition) {
            return makePredicate((SingleCondition) condition);
        } else if (condition instanceof ConditionGroup) {
            return makePredicate((ConditionGroup) condition);
        } else
            throw new IllegalStateException(
                    "Cannot process condition of type " + condition.getClass().getName());
    }

    private Predicate<IfcLineAndModel> makePredicate(ConditionGroup condition) {
        return condition.getConditions().stream()
                .map(c -> buildRulePredicate(c))
                .reduce(
                        (p1, p2) ->
                                condition.getConnective() == Connective.AND
                                        ? p1.and(p2)
                                        : p2.or(p2))
                .get();
    }

    private Predicate<IfcLineAndModel> makePredicate(SingleCondition condition) {
        return ilam -> {
            BiFunction<IfcLineAndModel, SingleCondition, Boolean> fun =
                    singleConditionPredicates.get(condition.getPredicate());
            if (fun != null) {
                return fun.apply(ilam, condition);
            } else {
                logger.info(
                        "Ignoring condition with predicate {}: not implemented",
                        condition.getPredicate());
            }
            return false;
        };
    }

    private static List<? extends IfcLine> getProperties(IfcLineAndModel ilam, Feature feature) {
        return ilam.getIfcModel()
                .getProperties(
                        ilam.ifcLine, IfcLinePredicates.isPropertyWithName(feature.getName()));
    }

    private static final Map<
                    MappingPredicate, BiFunction<IfcLineAndModel, SingleCondition, Boolean>>
            singleConditionPredicates =
                    Map.ofEntries(
                            Map.entry(
                                    MappingPredicate.PRESENT,
                                    (ilam, condition) ->
                                            !getProperties(ilam, condition.getFeature()).isEmpty()),
                            Map.entry(
                                    MappingPredicate.CONTAINS,
                                    (ilam, condition) ->
                                            evaluateCondition(
                                                    ilam,
                                                    condition,
                                                    IfcLinePredicates
                                                            ::isStringPropertyWithValueContaining)),
                            Map.entry(
                                    MappingPredicate.CONTAINS_NOT,
                                    (ilam, condition) ->
                                            evaluateCondition(
                                                    ilam,
                                                    condition,
                                                    IfcLinePredicates
                                                            ::isStringPropertyWithValueNotContaining)),
                            Map.entry(
                                    MappingPredicate.MATCHES,
                                    (ilam, condition) ->
                                            evaluateCondition(
                                                    ilam,
                                                    condition,
                                                    IfcLinePredicates
                                                            ::isStringPropertyWithValueMatching)),
                            Map.entry(
                                    MappingPredicate.EQUALS,
                                    (ilam, condition) ->
                                            evaluateCondition(
                                                    ilam,
                                                    condition,
                                                    IfcLinePredicates::valueEquals)),
                            Map.entry(
                                    MappingPredicate.NOT,
                                    (ilam, condition) ->
                                            evaluateCondition(
                                                    ilam,
                                                    condition,
                                                    o ->
                                                            IfcLinePredicates.valueEquals(o)
                                                                    .negate())),
                            Map.entry(
                                    MappingPredicate.GREATER_OR_EQUALS,
                                    (ilam, condition) ->
                                            evaluateCondition(
                                                    ilam,
                                                    condition,
                                                    o ->
                                                            IfcLinePredicates.isNumericProperty()
                                                                    .and(
                                                                            IfcLinePredicates
                                                                                    .isNumericPropertyWithValueLessThan(
                                                                                            o)
                                                                                    .negate()))),
                            Map.entry(
                                    MappingPredicate.GREATER_THAN,
                                    (ilam, condition) ->
                                            evaluateCondition(
                                                    ilam,
                                                    condition,
                                                    o ->
                                                            IfcLinePredicates.isNumericProperty()
                                                                    .and(
                                                                            IfcLinePredicates
                                                                                    .isNumericPropertyWithValueLessThanOrEqualTo(
                                                                                            o)
                                                                                    .negate()))),
                            Map.entry(
                                    MappingPredicate.LESS_OR_EQUALS,
                                    (ilam, condition) ->
                                            evaluateCondition(
                                                    ilam,
                                                    condition,
                                                    o ->
                                                            IfcLinePredicates
                                                                    .isNumericPropertyWithValueLessThanOrEqualTo(
                                                                            o))),
                            Map.entry(
                                    MappingPredicate.LESS_THAN,
                                    (ilam, condition) ->
                                            evaluateCondition(
                                                    ilam,
                                                    condition,
                                                    o ->
                                                            IfcLinePredicates
                                                                    .isNumericPropertyWithValueLessThan(
                                                                            o))));

    @NotNull
    private static <T> Boolean evaluateCondition(
            IfcLineAndModel ilam,
            SingleCondition condition,
            Function<T, Predicate<IfcLine>> predicateGenerator) {
        List<? extends IfcLine> props = getProperties(ilam, condition.getFeature());
        if (props.isEmpty()) {
            return false;
        }
        warnIfMultipleValues(ilam, condition, props);
        IfcLine prop = props.get(0);
        Optional<MappingExecutionValue> conditionValue = condition.getValue();
        if (!condition.getPredicate().isValueless() && conditionValue.isEmpty()) {
            warnAboutMissingConditionValue(condition);
            return false;
        }
        return predicateGenerator.apply(conditionValue.get().getValue()).test(prop);
    }

    private static void warnAboutMissingConditionValue(SingleCondition condition) {
        logger.info(
                "expected a conditionvalue in condition {} on feature {}, but none was found",
                condition.getId(),
                condition.getFeature().getName());
    }

    private static void warnIfMultipleValues(
            IfcLineAndModel ilam, SingleCondition condition, List<? extends IfcLine> props) {
        if (props.size() > 1) {
            logger.info(
                    "more than one property with name {} found for object {} ",
                    condition.getFeature().getName(),
                    ilam.ifcLine);
        }
    }

    private class IfcLineAndModel {
        private final IfcLine ifcLine;
        private final ParsedIfcFile ifcModel;

        public IfcLineAndModel(IfcLine ifcLine, ParsedIfcFile ifcModel) {
            this.ifcLine = ifcLine;
            this.ifcModel = ifcModel;
        }

        public IfcLine getIfcLine() {
            return ifcLine;
        }

        public ParsedIfcFile getIfcModel() {
            return ifcModel;
        }
    }
}
