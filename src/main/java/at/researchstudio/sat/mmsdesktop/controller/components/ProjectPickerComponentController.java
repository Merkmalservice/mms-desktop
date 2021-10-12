package at.researchstudio.sat.mmsdesktop.controller.components;

import at.researchstudio.sat.mmsdesktop.service.ReactiveStateService;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@FxmlView("projectPickerComponent.fxml")
public class ProjectPickerComponentController implements Initializable {
    private final ReactiveStateService stateService;

    @Autowired
    public ProjectPickerComponentController(ReactiveStateService stateService) {
        this.stateService = stateService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resourceBundle) {}
}
