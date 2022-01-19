package at.researchstudio.sat.mmsdesktop.view.components;

import static at.researchstudio.sat.mmsdesktop.view.components.ProcessState.STEP_ACTIVE;

import at.researchstudio.sat.mmsdesktop.constants.ViewConstants;
import com.jfoenix.controls.JFXButton;
import javafx.beans.property.SimpleObjectProperty;
import org.kordamp.ikonli.bootstrapicons.BootstrapIcons;
import org.kordamp.ikonli.javafx.FontIcon;

public class JFXStepButton extends JFXButton {
    private final FontIcon fontIcon;

    private final SimpleObjectProperty<ProcessState> state;

    public JFXStepButton() {
        this.state = new SimpleObjectProperty<>(STEP_ACTIVE);

        this.fontIcon = new FontIcon();
        fontIcon.setIconCode(BootstrapIcons.SQUARE);
        fontIcon.setIconSize(16);
        fontIcon.setIconColor(ViewConstants.MENU_COLOR_INACTIVE);
        this.setTextFill(ViewConstants.MENU_COLOR_INACTIVE);
        this.setDisabled(true);
        setGraphic(fontIcon);

        this.state.addListener(((observable, oldValue, newValue) -> styleForState(newValue)));
        styleForState(this.state.get());
    }

    private void styleForState(ProcessState processState) {
        switch (processState) {
            case STEP_ACTIVE:
                fontIcon.setIconCode(BootstrapIcons.PENCIL_SQUARE);
                fontIcon.setIconColor(ViewConstants.MENU_COLOR_ACTIVE);
                JFXStepButton.this.setTextFill(ViewConstants.MENU_COLOR_ACTIVE);
                JFXStepButton.this.setDisabled(false);
                break;
            case STEP_COMPLETE:
                fontIcon.setIconCode(BootstrapIcons.CHECK_SQUARE);
                fontIcon.setIconColor(ViewConstants.MENU_COLOR_ACTIVE);
                JFXStepButton.this.setTextFill(ViewConstants.MENU_COLOR_ACTIVE);
                JFXStepButton.this.setDisabled(false);
                break;
            case STEP_FAILED:
                fontIcon.setIconCode(BootstrapIcons.EXCLAMATION_SQUARE);
                fontIcon.setIconColor(ViewConstants.MENU_COLOR_ACTIVE);
                JFXStepButton.this.setTextFill(ViewConstants.MENU_COLOR_ACTIVE);
                JFXStepButton.this.setDisabled(false);
                break;
            case STEP_DISABLED:
                fontIcon.setIconCode(BootstrapIcons.SQUARE);
                fontIcon.setIconColor(ViewConstants.MENU_COLOR_INACTIVE);
                JFXStepButton.this.setTextFill(ViewConstants.MENU_COLOR_INACTIVE);
                JFXStepButton.this.setDisabled(true);
                break;
            case STEP_PROCESSING:
                fontIcon.setIconCode(BootstrapIcons.THREE_DOTS);
                fontIcon.setIconColor(ViewConstants.MENU_COLOR_ACTIVE);
                JFXStepButton.this.setTextFill(ViewConstants.MENU_COLOR_ACTIVE);
                JFXStepButton.this.setDisabled(false);
                break;
            case STEP_OPEN:
            default:
                fontIcon.setIconCode(BootstrapIcons.SQUARE);
                fontIcon.setIconColor(ViewConstants.MENU_COLOR_ACTIVE);
                JFXStepButton.this.setTextFill(ViewConstants.MENU_COLOR_ACTIVE);
                JFXStepButton.this.setDisabled(false);
                break;
        }
    }

    public SimpleObjectProperty<ProcessState> stateProperty() {
        return state;
    }
}
