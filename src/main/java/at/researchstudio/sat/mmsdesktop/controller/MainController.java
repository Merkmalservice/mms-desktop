package at.researchstudio.sat.mmsdesktop.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.MenuBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

@Component
public class MainController implements Initializable {
  @Value("classpath:/about.fxml")
  private Resource aboutResource;

  @Value("classpath:/convert.fxml")
  private Resource convertResource;

  @Value("classpath:/extract.fxml")
  private Resource extractResource;

  @Value("classpath:/settings.fxml")
  private Resource settingsResource;

  @FXML private MenuBar menuBar;

  @FXML private BorderPane mainPane;

  /**
   * Handle action related to input (in this case specifically only responds to keyboard event
   * CTRL-A).
   *
   * @param event Input event.
   */
  public void handleKeyInput(KeyEvent event) {
    if (event instanceof KeyEvent) {
      final KeyEvent keyEvent = event;
      if (keyEvent.isControlDown() && keyEvent.getCode() == KeyCode.A) {
        provideAboutFunctionality();
      }
    }
  }

  /**
   * Handle action related to "About" menu item.
   *
   * @param event Event on "About" menu item.
   */
  @FXML
  private void handleAboutAction(final ActionEvent event) {
    provideAboutFunctionality();
  }

  /**
   * Handle action related to "Exit" menu item.
   *
   * @param event Event on "Exit" menu item.
   */
  @FXML
  private void handleExitAction(final ActionEvent event) {
    Stage stage = (Stage) menuBar.getScene().getWindow();
    //TODO: Clean up if necessary
    stage.close();
  }

  /**
   * Handle action related to "Settings" menu item.
   *
   * @param event Event on "Settings" menu item.
   */
  @FXML
  private void handleSettingsAction(final ActionEvent event) {
    switchCenterPane(settingsResource);
  }

  /**
   * Handle action related to "Settings" menu item.
   *
   * @param event Event on "Settings" menu item.
   */
  @FXML
  private void handleExtractAction(final ActionEvent event) {
    switchCenterPane(extractResource);
  }

  /**
   * Handle action related to "Settings" menu item.
   *
   * @param event Event on "Settings" menu item.
   */
  @FXML
  private void handleConvertAction(final ActionEvent event) {
    switchCenterPane(convertResource);
  }

  /** Perform functionality associated with "About" menu selection or CTRL-A. */
  private void provideAboutFunctionality() {
    switchCenterPane(aboutResource);
  }

  @Override
  public void initialize(java.net.URL arg0, ResourceBundle resources) {
    menuBar.setFocusTraversable(true);
  }

  private void switchCenterPane(Resource resource) {
    try {
      FXMLLoader fxmlLoader = new FXMLLoader(resource.getURL());
      fxmlLoader.setResources(ResourceBundle.getBundle("messages", Locale.getDefault())); //set Default Locale to System default

      mainPane.setCenter(fxmlLoader.load());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
