package at.researchstudio.sat.mmsdesktop.controller;

import com.jfoenix.controls.JFXButton;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.springframework.stereotype.Component;

@Component
public class ConvertController implements Initializable {
    @FXML private JFXButton bPickFile;

    @FXML private JFXButton bPickDirectory;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {}
}
