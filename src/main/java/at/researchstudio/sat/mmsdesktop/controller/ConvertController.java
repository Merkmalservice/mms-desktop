package at.researchstudio.sat.mmsdesktop.controller;

import com.jfoenix.controls.JFXButton;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.ResourceBundle;

@Component
public class ConvertController implements Initializable {
  @FXML private JFXButton pickFile;

  @FXML private JFXButton pickDirectory;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {}
}
