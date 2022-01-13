package at.researchstudio.sat.mmsdesktop.gui;

import at.researchstudio.sat.mmsdesktop.gui.about.AboutController;
import at.researchstudio.sat.mmsdesktop.gui.convert.inputfile.SelectInputFileController;
import at.researchstudio.sat.mmsdesktop.gui.convert.perform.PerformConversionController;
import at.researchstudio.sat.mmsdesktop.gui.convert.targetstandard.SelectTargetStandardController;
import at.researchstudio.sat.mmsdesktop.gui.extract.ExtractController;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import net.rgielen.fxweaver.core.FxWeaver;
import org.springframework.stereotype.Component;

@Component
public class ViewState {
    public static final int CONVERT = 0;
    public static final int EXTRACT = 1;
    public static final int OTHER = 2; // not all views need to be represented

    private final ObjectProperty<Node> visibleCenterPane;
    private final FxWeaver fxWeaver;
    private final ResourceBundle resourceBundle;

    private IntegerProperty active;

    public ViewState(FxWeaver fxWeaver) {
        this.fxWeaver = fxWeaver;
        this.resourceBundle = ResourceBundle.getBundle("messages", Locale.getDefault());

        visibleCenterPane =
                new SimpleObjectProperty<>(
                        fxWeaver.loadView(AboutController.class, resourceBundle));
        active = new SimpleIntegerProperty(OTHER);
    }

    public Node getVisibleCenterPane() {
        return visibleCenterPane.get();
    }

    public void switchCenterPane(Class<?> controllerClass) {
        if (SelectInputFileController.class.isAssignableFrom(controllerClass)) {
            active.setValue(CONVERT);
        } else if (SelectTargetStandardController.class.isAssignableFrom(controllerClass)) {
            active.setValue(CONVERT);
        } else if (PerformConversionController.class.isAssignableFrom(controllerClass)) {
            active.setValue(CONVERT);
        } else if (ExtractController.class.isAssignableFrom(controllerClass)) {
            active.setValue(EXTRACT);
        } else {
            active.setValue(OTHER);
        }

        visibleCenterPane.setValue(fxWeaver.loadView(controllerClass, resourceBundle));
    }

    public ObjectProperty<Node> visibleCenterPaneProperty() {
        return visibleCenterPane;
    }

    public IntegerProperty activeProperty() {
        return active;
    }
}
