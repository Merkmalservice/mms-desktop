package at.researchstudio.sat.merkmalservice.ifc.convert.support.change;

import at.researchstudio.sat.merkmalservice.ifc.convert.ConversionRule;

public class HighlevelChangeFormatter {

    public HighlevelChangeFormatter() {}

    public String format(HighlevelChange change) {
        StringBuilder sb = new StringBuilder();
        sb.append("Entity ")
                .append(change.getEntityId())
                .append(": ")
                .append(change.getType())
                .append(" caused by ")
                .append(formatOriginator(change.getOriginator()))
                .append("\n");
        if (change.getDescription().isPresent()) {
            sb.append("\t").append(change.getDescription().get()).append("\n");
        }
        if (!change.getErrors().isEmpty()) {
            sb.append("\t").append("Errors:\n");
            for (Error error : change.getErrors()) {
                sb.append("\t\t").append(format(error)).append("\n");
            }
        }
        if (!change.getLowlevelChanges().isEmpty()) {
            sb.append("\t").append("Low-level changes:\n");
            for (LowlevelChange subChange : change.getLowlevelChanges()) {
                sb.append("\t\t").append(format(subChange)).append("\n");
            }
        }
        sb.append("\n");
        return sb.toString();
    }

    private String format(LowlevelChange lowlevelChange) {
        return lowlevelChange.toString();
    }

    private String formatOriginator(Object originator) {
        if (originator instanceof ConversionRule) {
            return "Rule " + ((ConversionRule) originator).toString();
        }
        return originator.toString();
    }

    private String format(Error error) {
        StringBuilder sb = new StringBuilder();
        sb.append("Error[message='")
                .append(error.getMessage().orElse(""))
                .append("', throwable=")
                .append(
                        error.getException().stream()
                                .map(t -> t.toString())
                                .findFirst()
                                .orElse("(none)"))
                .append("]");
        return sb.toString();
    }
}
