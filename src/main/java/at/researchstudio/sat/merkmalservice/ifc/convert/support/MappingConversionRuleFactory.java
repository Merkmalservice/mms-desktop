package at.researchstudio.sat.merkmalservice.ifc.convert.support;

import static java.util.stream.Collectors.toList;

import at.researchstudio.sat.merkmalservice.ifc.ParsedIfcFile;
import at.researchstudio.sat.merkmalservice.ifc.convert.ConversionRule;
import at.researchstudio.sat.merkmalservice.ifc.convert.ConversionRuleFactory;
import at.researchstudio.sat.merkmalservice.ifc.convert.ParsedIfcFileModification;
import at.researchstudio.sat.merkmalservice.ifc.convert.support.modification.Modification;
import at.researchstudio.sat.merkmalservice.ifc.model.IfcLine;
import at.researchstudio.sat.merkmalservice.ifc.model.element.IfcBuiltElementLine;
import at.researchstudio.sat.merkmalservice.model.Feature;
import at.researchstudio.sat.merkmalservice.model.mapping.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class MappingConversionRuleFactory implements ConversionRuleFactory {
    private Collection<Mapping> mappings;

    public MappingConversionRuleFactory(Collection<Mapping> mappings) {
        this.mappings = new ArrayList<>(mappings);
    }

    @Override
    public Collection<ConversionRule> getRules() {
        return mappings.stream().map(m -> toConversionRule(m)).collect(toList());
    }

    private ConversionRule toConversionRule(Mapping mapping) {
        Predicate<IfcLineAndModel> ruleCondition = buildRulePredicate(mapping.getCondition());
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
        return ifcLineAndModel ->
                IfcBuiltElementLine.class.isAssignableFrom(ifcLineAndModel.getIfcLine().getClass());
        /*
        if (condition instanceof SingleCondition) {
            return makePredicate((SingleCondition) condition);
        } else if (condition instanceof ConditionGroup) {
            return makePredicate((ConditionGroup) condition);
        }
        else throw new IllegalStateException("Cannot process condition of type " + condition.getClass().getName());

         */
    }

    private Predicate<IfcLineAndModel> makePredicate(ConditionGroup condition) {
        return a -> true;
    }

    private Predicate<IfcLineAndModel> makePredicate(SingleCondition condition) {
        return ilam -> {
            Feature relatedFeature = ilam.getIfcModel().getRelatedFeature(ilam.getIfcLine());
            if (condition.getPredicate() == MappingPredicate.PRESENT){
                if (relatedFeature != null){
                    if (condition.getFeature().getName().equals(relatedFeature.getName())){
                        return true;
                    }
                    return false;
                }
            }
            return false;
        };
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
