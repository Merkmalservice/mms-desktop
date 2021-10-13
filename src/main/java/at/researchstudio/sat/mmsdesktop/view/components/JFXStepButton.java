package at.researchstudio.sat.mmsdesktop.view.components;

import at.researchstudio.sat.mmsdesktop.constants.ViewConstants;
import at.researchstudio.sat.mmsdesktop.state.ConvertState;
import com.jfoenix.controls.JFXButton;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.kordamp.ikonli.bootstrapicons.BootstrapIcons;
import org.kordamp.ikonli.javafx.FontIcon;

public class JFXStepButton extends JFXButton {
    private final FontIcon fontIcon;

    private final IntegerProperty state;

    public JFXStepButton() {
        this.state = new SimpleIntegerProperty(0);

        this.fontIcon = new FontIcon();
        fontIcon.setIconCode(BootstrapIcons.SQUARE);
        fontIcon.setIconSize(16);
        fontIcon.setIconColor(ViewConstants.MENU_COLOR_INACTIVE);
        this.setTextFill(ViewConstants.MENU_COLOR_INACTIVE);
        this.setDisabled(true);
        setGraphic(fontIcon);

        this.state.addListener(
                ((observable, oldValue, newValue) -> {
                    switch (newValue.intValue()) {
                        case ConvertState.STEP_ACTIVE:
                            fontIcon.setIconCode(BootstrapIcons.PENCIL_SQUARE);
                            fontIcon.setIconColor(ViewConstants.MENU_COLOR_ACTIVE);
                            this.setTextFill(ViewConstants.MENU_COLOR_ACTIVE);
                            this.setDisabled(false);
                            break;
                        case ConvertState.STEP_COMPLETE:
                            fontIcon.setIconCode(BootstrapIcons.CHECK_SQUARE);
                            fontIcon.setIconColor(ViewConstants.MENU_COLOR_INACTIVE);
                            this.setTextFill(ViewConstants.MENU_COLOR_INACTIVE);
                            this.setDisabled(false);
                            break;
                        case ConvertState.STEP_FAILED:
                            fontIcon.setIconCode(BootstrapIcons.EXCLAMATION_SQUARE);
                            fontIcon.setIconColor(ViewConstants.MENU_COLOR_INACTIVE);
                            this.setTextFill(ViewConstants.MENU_COLOR_INACTIVE);
                            this.setDisabled(false);
                            break;
                        case ConvertState.STEP_DISABLED:
                            fontIcon.setIconCode(BootstrapIcons.SQUARE);
                            fontIcon.setIconColor(ViewConstants.MENU_COLOR_INACTIVE);
                            this.setTextFill(ViewConstants.MENU_COLOR_INACTIVE);
                            this.setDisabled(true);
                            break;
                        case ConvertState.STEP_PROCESSING:
                            fontIcon.setIconCode(BootstrapIcons.THREE_DOTS);
                            fontIcon.setIconColor(ViewConstants.MENU_COLOR_INACTIVE);
                            this.setTextFill(ViewConstants.MENU_COLOR_INACTIVE);
                            this.setDisabled(true);
                            break;
                        case ConvertState.STEP_OPEN:
                        default:
                            fontIcon.setIconCode(BootstrapIcons.SQUARE);
                            fontIcon.setIconColor(ViewConstants.MENU_COLOR_INACTIVE);
                            this.setTextFill(ViewConstants.MENU_COLOR_INACTIVE);
                            this.setDisabled(false);
                            break;
                    }
                }));
    }

    public IntegerProperty stateProperty() {
        return state;
    }
}
