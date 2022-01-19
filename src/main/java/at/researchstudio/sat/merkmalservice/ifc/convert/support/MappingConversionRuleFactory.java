package at.researchstudio.sat.merkmalservice.ifc.convert.support;

import static java.util.stream.Collectors.toList;

import at.researchstudio.sat.merkmalservice.ifc.ParsedIfcFile;
import at.researchstudio.sat.merkmalservice.ifc.convert.ConversionRule;
import at.researchstudio.sat.merkmalservice.ifc.convert.ConversionRuleFactory;
import at.researchstudio.sat.merkmalservice.ifc.convert.ParsedIfcFileModification;
import at.researchstudio.sat.merkmalservice.ifc.convert.support.modification.Modification;
import at.researchstudio.sat.merkmalservice.ifc.convert.support.rule.IfcElementConversionRule;
import at.researchstudio.sat.merkmalservice.ifc.model.IfcLine;
import at.researchstudio.sat.merkmalservice.ifc.support.IfcLinePredicates;
import at.researchstudio.sat.merkmalservice.model.mapping.Mapping;
import at.researchstudio.sat.merkmalservice.model.mapping.MappingExecutionValue;
import at.researchstudio.sat.merkmalservice.model.mapping.MappingPredicate;
import at.researchstudio.sat.merkmalservice.model.mapping.action.Action;
import at.researchstudio.sat.merkmalservice.model.mapping.action.ActionGroup;
import at.researchstudio.sat.merkmalservice.model.mapping.action.add.AddAction;
import at.researchstudio.sat.merkmalservice.model.mapping.action.add.AddActionGroup;
import at.researchstudio.sat.merkmalservice.model.mapping.action.convert.ConvertAction;
import at.researchstudio.sat.merkmalservice.model.mapping.action.convert.ConvertActionGroup;
import at.researchstudio.sat.merkmalservice.model.mapping.action.delete.DeleteAction;
import at.researchstudio.sat.merkmalservice.model.mapping.condition.Condition;
import at.researchstudio.sat.merkmalservice.model.mapping.condition.ConditionGroup;
import at.researchstudio.sat.merkmalservice.model.mapping.condition.Connective;
import at.researchstudio.sat.merkmalservice.model.mapping.condition.SingleCondition;
import at.researchstudio.sat.merkmalservice.model.mapping.feature.Feature;
import at.researchstudio.sat.merkmalservice.support.exception.ErrorUtils;
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MappingConversionRuleFactory implements ConversionRuleFactory {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final Collection<Mapping> mappings;

    public MappingConversionRuleFactory(Collection<Mapping> mappings) {
        this.mappings = new ArrayList<>(mappings);
    }

    @Override
    public Collection<ConversionRule> getRules() {
        return mappings.stream().map(this::toConversionRule).collect(toList());
    }

    private ConversionRule toConversionRule(Mapping mapping) {
        Predicate<IfcLineAndModel> ruleCondition =
                mapping.getCondition() == null
                        ? line -> true
                        : buildRulePredicate(mapping.getCondition());
        ConversionRule rule =
                new ConversionRule() {
                    private final String name = mapping.getName();

                    @Override
                    public int getOrder() {
                        return 0;
                    }

                    @Override
                    public boolean appliesTo(IfcLine line, ParsedIfcFile ifcModel) {
                        return ruleCondition.test(new IfcLineAndModel(line, ifcModel));
                    }

                    @Override
                    public String toString() {
                        return name;
                    }

                    @Override
                    public ParsedIfcFileModification applyTo(IfcLine line, ParsedIfcFile ifcModel) {
                        List<ActionGroup<? extends Action>> actionGroups =
                                Optional.ofNullable(mapping.getActionGroups())
                                        .orElse(Collections.emptyList());
                        Stream<ParsedIfcFileModification> modifications =
                                actionGroups.stream()
                                        .flatMap(
                                                group ->
                                                        group.getActions().stream()
                                                                .map(
                                                                        a ->
                                                                                ErrorUtils
                                                                                        .logThrowableMessage(
                                                                                                () ->
                                                                                                        makeModification(
                                                                                                                group,
                                                                                                                a,
                                                                                                                line))))
                                        .filter(Objects::nonNull);
                        return Modification.multiple(modifications);
                    }
                };
        return new IfcElementConversionRule(rule);
    }

    private ParsedIfcFileModification makeModification(
            ActionGroup<? extends Action> actionGroup, Action action, IfcLine line) {
        if (action instanceof DeleteAction) {
            return Modification.removePropertyWithName(
                    ((DeleteAction) action).getFeature().getName(), line);
        }
        if (action instanceof AddAction) {
            AddActionGroup addActionGroup = (AddActionGroup) actionGroup;
            String propertySetName = getPropertySetName(addActionGroup.getAddToPropertySet());
            return Modification.addProperty(
                    ((AddAction) action).getFeature(),
                    ((AddAction) action).getValue(),
                    propertySetName,
                    line);
        }
        if (action instanceof ConvertAction) {
            ConvertActionGroup convertActionGroup = (ConvertActionGroup) actionGroup;
            ConvertAction convertAction = (ConvertAction) action;
            String propertySetName = getPropertySetName(convertActionGroup.getAddToPropertySet());
            return Modification.convertProperty(
                    convertAction.getInputFeature(),
                    convertAction.getOutputFeature(),
                    propertySetName,
                    line);
        }
        throw new IllegalStateException(
                "TODO: Cannot generate modification for action of type "
                        + action.getClass().getName());
    }

    // TODO: handle property set IDs: load all property sets of target standard before generating
    // the rule executions
    @NotNull
    private String getPropertySetName(MappingExecutionValue propertySetNameOrId) {
        Optional<String> propertySetName =
                Optional.ofNullable(propertySetNameOrId)
                        .flatMap(MappingExecutionValue::getStringValue);
        if (propertySetName.isPresent()) {
            return propertySetName.get();
        }
        throw new UnsupportedOperationException(
                "TODO: Cannot handle action group using property set id or without any property set information yet");
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
                .map(this::buildRulePredicate)
                .reduce(
                        (p1, p2) ->
                                condition.getConnective() == Connective.AND
                                        ? p1.and(p2)
                                        : p1.or(p2))
                .get();
    }

    private Predicate<IfcLineAndModel> makePredicate(SingleCondition condition) {
        Function<SingleCondition, Predicate<IfcLineAndModel>> fun =
                singleConditionPredicates.get(condition.getPredicate());
        if (fun != null) {
            return fun.apply(condition);
        } else {
            logger.info(
                    "Ignoring condition with predicate {}: not implemented",
                    condition.getPredicate());
        }
        return x -> false;
    }

    private static List<? extends IfcLine> getProperties(IfcLineAndModel ilam, Feature feature) {
        return ilam.getIfcModel()
                .getProperties(
                        ilam.ifcLine,
                        IfcLinePredicates.isPropertyWithName(feature.getName())
                                .or(IfcLinePredicates.isQuantityWithName(feature.getName()))
                                .or(IfcLinePredicates.isEnumValueWithName(feature.getName())));
    }

    private static final Map<
                    MappingPredicate, Function<SingleCondition, Predicate<IfcLineAndModel>>>
            singleConditionPredicates =
                    Map.ofEntries(
                            Map.entry(
                                    MappingPredicate.PRESENT,
                                    condition ->
                                            ilam ->
                                                    !getProperties(ilam, condition.getFeature())
                                                            .isEmpty()),
                            Map.entry(
                                    MappingPredicate.CONTAINS,
                                    getConditionFactory(
                                            IfcLinePredicates
                                                    ::isStringPropertyWithValueContaining)),
                            Map.entry(
                                    MappingPredicate.CONTAINS_NOT,
                                    getConditionFactory(
                                            IfcLinePredicates
                                                    ::isStringPropertyWithValueNotContaining)),
                            Map.entry(
                                    MappingPredicate.MATCHES,
                                    getConditionFactory(
                                            IfcLinePredicates::isStringPropertyWithValueMatching)),
                            Map.entry(
                                    MappingPredicate.EQUALS,
                                    getConditionFactory(IfcLinePredicates::valueEquals)),
                            Map.entry(
                                    MappingPredicate.NOT,
                                    getConditionFactory(
                                            o ->
                                                    IfcLinePredicates.isProperty()
                                                            .and(
                                                                    IfcLinePredicates.valueEquals(o)
                                                                            .negate()))),
                            Map.entry(
                                    MappingPredicate.GREATER_OR_EQUALS,
                                    getConditionFactory(
                                            o ->
                                                    IfcLinePredicates.isNumericProperty()
                                                            .and(
                                                                    IfcLinePredicates
                                                                            .isNumericPropertyWithValueLessThan(
                                                                                    o)
                                                                            .negate()))),
                            Map.entry(
                                    MappingPredicate.GREATER_THAN,
                                    getConditionFactory(
                                            o ->
                                                    IfcLinePredicates.isNumericProperty()
                                                            .and(
                                                                    IfcLinePredicates
                                                                            .isNumericPropertyWithValueLessThanOrEqualTo(
                                                                                    o)
                                                                            .negate()))),
                            Map.entry(
                                    MappingPredicate.LESS_OR_EQUALS,
                                    getConditionFactory(
                                            IfcLinePredicates
                                                    ::isNumericPropertyWithValueLessThanOrEqualTo)),
                            Map.entry(
                                    MappingPredicate.LESS_THAN,
                                    getConditionFactory(
                                            IfcLinePredicates
                                                    ::isNumericPropertyWithValueLessThan)));

    @NotNull
    private static <T> Function<SingleCondition, Predicate<IfcLineAndModel>> getConditionFactory(
            Function<T, Predicate<IfcLine>> predicateGenerator) {
        return condition -> {
            Optional<MappingExecutionValue> conditionValue = condition.getValue();
            if (!condition.getPredicate().isValueless() && conditionValue.isEmpty()) {
                warnAboutMissingConditionValue(condition);
                return x -> false;
            }
            final Predicate<IfcLine> pred =
                    predicateGenerator.apply(conditionValue.get().getValue());
            return ilam -> {
                List<? extends IfcLine> props = getProperties(ilam, condition.getFeature());
                if (props.isEmpty()) {
                    return false;
                }
                warnIfMultipleValues(ilam, condition, props);
                IfcLine prop = props.get(0);
                return pred.test(prop);
            };
        };
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

    private static class IfcLineAndModel {
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
