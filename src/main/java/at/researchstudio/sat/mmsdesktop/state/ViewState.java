package at.researchstudio.sat.mmsdesktop.state;

import at.researchstudio.sat.mmsdesktop.controller.AboutController;
import at.researchstudio.sat.mmsdesktop.controller.ConvertController;
import at.researchstudio.sat.mmsdesktop.controller.ExtractController;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import net.rgielen.fxweaver.core.FxWeaver;
import org.springframework.stereotype.Component;

@Component
public class ViewState {
    private final ObjectProperty<Node> visibleCenterPane;
    private final FxWeaver fxWeaver;
    private final ResourceBundle resourceBundle;

    private BooleanProperty convertView;
    private BooleanProperty extractView;

    public ViewState(FxWeaver fxWeaver) {
        this.fxWeaver = fxWeaver;
        this.resourceBundle = ResourceBundle.getBundle("messages", Locale.getDefault());

        visibleCenterPane =
                new SimpleObjectProperty<>(
                        fxWeaver.loadView(AboutController.class, resourceBundle));

        convertView = new SimpleBooleanProperty(false);
        extractView = new SimpleBooleanProperty(false);
    }

    public Node getVisibleCenterPane() {
        return visibleCenterPane.get();
    }

    public void switchCenterPane(Class controllerClass) {
        convertView.setValue(false);
        extractView.setValue(false);

        if (ConvertController.class.isAssignableFrom(controllerClass)) {
            convertView.setValue(true);
        } else if (ExtractController.class.isAssignableFrom(controllerClass)) {
            extractView.setValue(true);
        }

        visibleCenterPane.setValue(fxWeaver.loadView(controllerClass, resourceBundle));
    }

    public ObjectProperty<Node> visibleCenterPaneProperty() {
        return visibleCenterPane;
    }

    public BooleanProperty convertViewProperty() {
        return convertView;
    }

    public BooleanProperty extractViewProperty() {
        return extractView;
    }
}
