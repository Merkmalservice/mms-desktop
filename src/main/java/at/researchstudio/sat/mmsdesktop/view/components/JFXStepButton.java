package at.researchstudio.sat.mmsdesktop.view.components;

import com.jfoenix.controls.JFXButton;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.bootstrapicons.BootstrapIcons;
import org.kordamp.ikonli.javafx.FontIcon;

public class JFXStepButton extends JFXButton {
    private final FontIcon fontIcon;

    private final IntegerProperty state;

    public static final int OPEN = 0;
    public static final int ACTIVE = 1;
    public static final int COMPLETE = 2;
    public static final int DISABLED = 3;
    public static final int FAILED = 4;
    public static final int PROCESSING = 5;

    public JFXStepButton() {
        this.state = new SimpleIntegerProperty(0);

        this.fontIcon = new FontIcon();
        fontIcon.setIconCode(BootstrapIcons.SQUARE);
        fontIcon.setIconSize(16);
        fontIcon.setIconColor(new Color(1, 1, 1, 0.5));
        this.setTextFill(new Color(1, 1, 1, 0.5));
        this.setDisabled(true);
        setGraphic(fontIcon);

        this.state.addListener(
                ((observable, oldValue, newValue) -> {
                    switch (newValue.intValue()) {
                        case ACTIVE:
                            fontIcon.setIconCode(BootstrapIcons.PENCIL_SQUARE);
                            fontIcon.setIconColor(Color.WHITE);
                            this.setTextFill(Color.WHITE);
                            this.setDisabled(false);
                            break;
                        case COMPLETE:
                            fontIcon.setIconCode(BootstrapIcons.CHECK_SQUARE);
                            fontIcon.setIconColor(new Color(1, 1, 1, 0.5));
                            this.setTextFill(new Color(1, 1, 1, 0.5));
                            this.setDisabled(false);
                            break;
                        case FAILED:
                            fontIcon.setIconCode(BootstrapIcons.EXCLAMATION_SQUARE);
                            fontIcon.setIconColor(new Color(1, 1, 1, 0.5));
                            this.setTextFill(new Color(1, 1, 1, 0.5));
                            this.setDisabled(false);
                            break;
                        case DISABLED:
                            fontIcon.setIconCode(BootstrapIcons.SQUARE);
                            fontIcon.setIconColor(new Color(1, 1, 1, 0.5));
                            this.setTextFill(new Color(1, 1, 1, 0.5));
                            this.setDisabled(true);
                            break;
                        case PROCESSING:
                            fontIcon.setIconCode(BootstrapIcons.THREE_DOTS);
                            fontIcon.setIconColor(new Color(1, 1, 1, 0.5));
                            this.setTextFill(new Color(1, 1, 1, 0.5));
                            this.setDisabled(true);
                            break;
                        case OPEN:
                        default:
                            fontIcon.setIconCode(BootstrapIcons.SQUARE);
                            fontIcon.setIconColor(new Color(1, 1, 1, 0.5));
                            this.setTextFill(new Color(1, 1, 1, 0.5));
                            this.setDisabled(false);
                            break;
                    }
                }));
    }

    public IntegerProperty stateProperty() {
        return state;
    }
}
