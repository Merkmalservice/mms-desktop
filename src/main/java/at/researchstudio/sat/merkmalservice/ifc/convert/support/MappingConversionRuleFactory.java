package at.researchstudio.sat.merkmalservice.ifc.convert.support;

import static java.util.stream.Collectors.toList;

import at.researchstudio.sat.merkmalservice.ifc.ParsedIfcFile;
import at.researchstudio.sat.merkmalservice.ifc.convert.ConversionRule;
import at.researchstudio.sat.merkmalservice.ifc.convert.ConversionRuleFactory;
import at.researchstudio.sat.merkmalservice.ifc.convert.ParsedIfcFileModification;
import at.researchstudio.sat.merkmalservice.ifc.convert.support.modification.Modification;
import at.researchstudio.sat.merkmalservice.ifc.convert.support.rule.IfcElementConversionRule;
import at.researchstudio.sat.merkmalservice.ifc.model.IfcLine;
import at.researchstudio.sat.merkmalservice.ifc.model.element.IfcElementLine;
import at.researchstudio.sat.merkmalservice.ifc.support.IfcElementValueExtractor;
import at.researchstudio.sat.merkmalservice.ifc.support.IfcLinePredicates;
import at.researchstudio.sat.merkmalservice.model.PropertySet;
import at.researchstudio.sat.merkmalservice.model.Standard;
import at.researchstudio.sat.merkmalservice.model.mapping.Mapping;
import at.researchstudio.sat.merkmalservice.model.mapping.MappingExecutionValue;
import at.researchstudio.sat.merkmalservice.model.mapping.MappingPredicate;
import at.researchstudio.sat.merkmalservice.model.mapping.action.Action;
import at.researchstudio.sat.merkmalservice.model.mapping.action.ActionGroup;
import at.researchstudio.sat.merkmalservice.model.mapping.action.add.AddAction;
import at.researchstudio.sat.merkmalservice.model.mapping.action.add.AddActionGroup;
import at.researchstudio.sat.merkmalservice.model.mapping.action.convert.ConvertAction;
import at.researchstudio.sat.merkmalservice.model.mapping.action.convert.ConvertActionGroup;
import at.researchstudio.sat.merkmalservice.model.mapping.action.convert.ExtractAction;
import at.researchstudio.sat.merkmalservice.model.mapping.action.convert.ExtractionSource;
import at.researchstudio.sat.merkmalservice.model.mapping.action.delete.DeleteAction;
import at.researchstudio.sat.merkmalservice.model.mapping.condition.*;
import at.researchstudio.sat.merkmalservice.model.mapping.feature.Feature;
import at.researchstudio.sat.merkmalservice.support.exception.ErrorUtils;
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MappingConversionRuleFactory implements ConversionRuleFactory {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String FALLBACK_PROPERTY_SET_NAME = "NEW_PROPERTIES";
    private final Collection<Mapping> mappings;
    private final Map<String, PropertySet> propertySetById;

    public MappingConversionRuleFactory(
            Collection<Mapping> mappings, List<Standard> standardsWithPropertySets) {
        this.mappings = new ArrayList<>(mappings);
        this.propertySetById =
                standardsWithPropertySets.stream()
                        .flatMap(s -> s.getPropertySets().stream())
                        .collect(Collectors.toMap(p -> p.getId(), Function.identity()));
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
                    public List<ParsedIfcFileModification> applyTo(
                            IfcLine line, ParsedIfcFile ifcModel) {
                        List<ActionGroup<? extends Action>> actionGroups =
                                Optional.ofNullable(mapping.getActionGroups())
                                        .orElse(Collections.emptyList());
                        Stream<ParsedIfcFileModification> modifications =
                                actionGroups.stream()
                                        .flatMap(
                                                group ->
                                                        group.getActions().stream()
                                                                .map(
                                                                        action ->
                                                                                ErrorUtils
                                                                                        .logThrowableMessage(
                                                                                                () ->
                                                                                                        makeModification(
                                                                                                                this,
                                                                                                                group,
                                                                                                                action,
                                                                                                                line))))
                                        .filter(Objects::nonNull);
                        return modifications.collect(Collectors.toList());
                    }
                };
        return new IfcElementConversionRule(rule);
    }

    private ParsedIfcFileModification makeModification(
            Object modificationSource,
            ActionGroup<? extends Action> actionGroup,
            Action action,
            IfcLine line) {
        if (action instanceof DeleteAction) {
            return Modification.removePropertyWithName(
                    modificationSource, ((DeleteAction) action).getFeature().getName(), line);
        }
        if (action instanceof AddAction) {
            AddActionGroup addActionGroup = (AddActionGroup) actionGroup;
            String propertySetName = getPropertySetName(addActionGroup.getAddToPropertySet());
            return Modification.addProperty(
                    modificationSource,
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
                    modificationSource,
                    convertAction.getInputFeature(),
                    convertAction.getOutputFeature(),
                    propertySetName,
                    line);
        }
        if (action instanceof ExtractAction) {
            ConvertActionGroup convertActionGroup = (ConvertActionGroup) actionGroup;
            ExtractAction extractAction = (ExtractAction) action;
            String propertySetName = getPropertySetName(convertActionGroup.getAddToPropertySet());
            return Modification.extractValueIntoProperty(
                    modificationSource,
                    extractAction.getSource(),
                    extractAction.getOutputFeature(),
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
        Optional<MappingExecutionValue> propertySetNameOrIdOpt =
                Optional.ofNullable(propertySetNameOrId);
        if (propertySetNameOrIdOpt.isPresent()) {
            Optional<String> propertySetNameOpt =
                    propertySetNameOrIdOpt.flatMap(MappingExecutionValue::getStringValue);
            if (propertySetNameOpt.isPresent()) {
                return propertySetNameOpt.get();
            }
            Optional<String> propertySetId =
                    propertySetNameOrIdOpt.flatMap(MappingExecutionValue::getIdValue);
            if (propertySetId.isPresent()) {
                PropertySet propertySet = propertySetById.get(propertySetId.get());
                if (propertySet != null) {
                    propertySetNameOpt = Optional.ofNullable(propertySet.getName());
                    if (propertySetNameOpt.isPresent()) {
                        return propertySetNameOpt.get();
                    } else {
                        logger.warn(
                                "Cannot obtain property set name - property set {} does not have a name. Using default",
                                propertySetId.get());
                    }
                } else {
                    logger.warn(
                            "Cannot obtain property set name - no property set found for provided id. Using default",
                            propertySetId.get());
                }
            }
        }
        return getFallbackPropertySetName();
    }

    private String getFallbackPropertySetName() {
        return FALLBACK_PROPERTY_SET_NAME;
    }

    private Predicate<IfcLineAndModel> buildRulePredicate(Condition condition) {
        if (condition instanceof SingleCondition) {
            return makePredicate((SingleCondition) condition);
        } else if (condition instanceof ElementCondition) {
            return makePredicate((ElementCondition) condition);
        }
        if (condition instanceof ConditionGroup) {
            return makePredicate((ConditionGroup) condition);
        } else
            throw new IllegalStateException(
                    "Cannot process condition of type " + condition.getClass().getName());
    }

    private Predicate<IfcLineAndModel> makePredicate(ElementCondition condition) {
        Function<ElementCondition, Predicate<IfcLineAndModel>> fun =
                elementConditionPredicates.get(condition.getPredicate());
        if (fun != null) {
            return fun.apply(condition);
        } else {
            logger.info(
                    "Ignoring element condition with predicate {}: not implemented",
                    condition.getPredicate());
        }
        return x -> false;
    }

    private Predicate<IfcLineAndModel> makePredicate(ConditionGroup condition) {
        return condition.getConditions().stream()
                .map(x -> buildRulePredicate(x))
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
                    "Ignoring single condition with predicate {}: not implemented",
                    condition.getPredicate());
        }
        return x -> false;
    }

    private static List<? extends IfcLine> getProperties(IfcLineAndModel ilam, Feature feature) {
        List<IfcLine> matchingProperties = new ArrayList<>();
        Predicate<IfcLine> predicate =
                IfcLinePredicates.isPropertyWithName(feature.getName())
                        .or(IfcLinePredicates.isQuantityWithName(feature.getName()))
                        .or(IfcLinePredicates.isEnumValueWithName(feature.getName()));
        matchingProperties.addAll(ilam.getIfcModel().getProperties(ilam.ifcLine, predicate));
        matchingProperties.addAll(ilam.getIfcModel().getPropertiesViaType(ilam.ifcLine, predicate));
        return matchingProperties;
    }

    private static Optional<Object> getValue(IfcLineAndModel ilam, ExtractionSource source) {
        switch (source) {
            case ELEMENT_NAME:
                if (ilam.getIfcLine() instanceof IfcElementLine) {
                    return Optional.ofNullable(((IfcElementLine) ilam.getIfcLine()).getName());
                }
                return Optional.empty();
            case ELEMENT_DESCRIPTION:
                if (ilam.getIfcLine() instanceof IfcElementLine) {
                    return Optional.ofNullable(
                            ((IfcElementLine) ilam.getIfcLine()).getDescription());
                }
                return Optional.empty();
            default:
                return Optional.empty();
        }
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

    private static final Map<
                    MappingPredicate, Function<ElementCondition, Predicate<IfcLineAndModel>>>
            elementConditionPredicates =
                    Map.ofEntries(
                            Map.entry(
                                    MappingPredicate.PRESENT,
                                    condition ->
                                            ilam ->
                                                    IfcElementValueExtractor
                                                            .ifcElementValueExtractors
                                                            .get(condition.getSource())
                                                            .apply(
                                                                    ilam.getIfcModel(),
                                                                    ilam.getIfcLine())
                                                            .isPresent()),
                            Map.entry(
                                    MappingPredicate.CONTAINS,
                                    condition ->
                                            ilam ->
                                                    IfcLinePredicates
                                                            .isStringElementValueWithValueContaining(
                                                                    IfcElementValueExtractor
                                                                            .ifcElementValueExtractors
                                                                            .get(
                                                                                    condition
                                                                                            .getSource()),
                                                                    condition
                                                                            .getValue()
                                                                            .get()
                                                                            .getStringValue()
                                                                            .get())
                                                            .test(
                                                                    ilam.getIfcModel(),
                                                                    ilam.getIfcLine())),
                            Map.entry(
                                    MappingPredicate.CONTAINS_NOT,
                                    condition ->
                                            ilam ->
                                                    IfcLinePredicates
                                                            .isStringElementValueWithValueNotContaining(
                                                                    IfcElementValueExtractor
                                                                            .ifcElementValueExtractors
                                                                            .get(
                                                                                    condition
                                                                                            .getSource()),
                                                                    condition
                                                                            .getValue()
                                                                            .get()
                                                                            .getStringValue()
                                                                            .get())
                                                            .test(
                                                                    ilam.getIfcModel(),
                                                                    ilam.getIfcLine())),
                            Map.entry(
                                    MappingPredicate.MATCHES,
                                    condition ->
                                            ilam ->
                                                    IfcLinePredicates
                                                            .isStringElementValueWithValueMatching(
                                                                    IfcElementValueExtractor
                                                                            .ifcElementValueExtractors
                                                                            .get(
                                                                                    condition
                                                                                            .getSource()),
                                                                    condition
                                                                            .getValue()
                                                                            .get()
                                                                            .getStringValue()
                                                                            .get())
                                                            .test(
                                                                    ilam.getIfcModel(),
                                                                    ilam.getIfcLine())),
                            Map.entry(
                                    MappingPredicate.EQUALS,
                                    condition ->
                                            ilam ->
                                                    IfcLinePredicates.isStringElementValueWithValue(
                                                                    IfcElementValueExtractor
                                                                            .ifcElementValueExtractors
                                                                            .get(
                                                                                    condition
                                                                                            .getSource()),
                                                                    condition
                                                                            .getValue()
                                                                            .get()
                                                                            .getStringValue()
                                                                            .get())
                                                            .test(
                                                                    ilam.getIfcModel(),
                                                                    ilam.getIfcLine())),
                            Map.entry(
                                    MappingPredicate.NOT,
                                    condition ->
                                            ilam ->
                                                    IfcLinePredicates
                                                            .isStringElementValueWithDifferentValue(
                                                                    IfcElementValueExtractor
                                                                            .ifcElementValueExtractors
                                                                            .get(
                                                                                    condition
                                                                                            .getSource()),
                                                                    condition
                                                                            .getValue()
                                                                            .get()
                                                                            .getStringValue()
                                                                            .get())
                                                            .test(
                                                                    ilam.getIfcModel(),
                                                                    ilam.getIfcLine())));

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
