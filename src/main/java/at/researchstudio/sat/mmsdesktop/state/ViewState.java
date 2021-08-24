package at.researchstudio.sat.mmsdesktop.state;

import at.researchstudio.sat.mmsdesktop.controller.AboutController;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import net.rgielen.fxweaver.core.FxWeaver;
import org.springframework.stereotype.Component;

@Component
public class ViewState {
    private final ObjectProperty<Node> visibleCenterPaneProperty;
    private final FxWeaver fxWeaver;
    private final ResourceBundle resourceBundle;

    public ViewState(FxWeaver fxWeaver) {
        this.fxWeaver = fxWeaver;
        this.resourceBundle = ResourceBundle.getBundle("messages", Locale.getDefault());

        visibleCenterPaneProperty =
                new SimpleObjectProperty<>(
                        fxWeaver.loadView(AboutController.class, resourceBundle));
    }

    public Node getVisibleCenterPaneProperty() {
        return visibleCenterPaneProperty.get();
    }

    public void switchCenterPane(Class controllerClass) {
        visibleCenterPaneProperty.setValue(fxWeaver.loadView(controllerClass, resourceBundle));
    }

    public ObjectProperty<Node> visibleCenterPanePropertyProperty() {
        return visibleCenterPaneProperty;
    }
}
