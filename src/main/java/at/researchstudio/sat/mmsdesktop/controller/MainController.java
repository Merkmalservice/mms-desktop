package at.researchstudio.sat.mmsdesktop.controller;

import at.researchstudio.sat.mmsdesktop.controller.components.FeatureView;
import at.researchstudio.sat.mmsdesktop.controller.components.IfcLineView;
import at.researchstudio.sat.mmsdesktop.model.auth.UserSession;
import at.researchstudio.sat.mmsdesktop.model.task.LogoutResult;
import at.researchstudio.sat.mmsdesktop.service.AuthService;
import at.researchstudio.sat.mmsdesktop.service.ReactiveStateService;
import at.researchstudio.sat.mmsdesktop.view.components.JFXStepButton;
import com.jfoenix.controls.JFXButton;
import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.ResourceBundle;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import net.rgielen.fxweaver.core.FxmlView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@FxmlView("main.fxml")
public class MainController implements Initializable {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final AuthService authService;
    private final ReactiveStateService stateService;

    public JFXButton convertButton;
    public JFXButton extractButton;
    public JFXStepButton convertButtonStepFile;
    public JFXStepButton convertButtonStepProject;
    public JFXStepButton convertButtonStepConvert;

    @FXML private MenuBar menuBar;
    @FXML private MenuItem menuBarLogin;
    @FXML private MenuItem menuBarLogout;
    @FXML private BorderPane mainPane;
    @FXML private JFXButton projectsButton;

    @FXML private BorderPane selectedIfcLineView;
    @FXML private IfcLineView ifcLineView;

    @FXML private BorderPane selectedFeaturePreview;
    @FXML private FeatureView featureView;

    @Autowired
    public MainController(AuthService authService, ReactiveStateService stateService) {
        this.stateService = stateService;
        this.authService = authService;
    }

    @Override
    public void initialize(java.net.URL arg0, ResourceBundle resources) {
        menuBar.setFocusTraversable(true);
        menuBarLogin.visibleProperty().bind(stateService.getLoginState().loggedInProperty().not());
        menuBarLogout.visibleProperty().bind(stateService.getLoginState().loggedInProperty());
        projectsButton
                .disableProperty()
                .bind(stateService.getLoginState().loggedInProperty().not());
        mainPane.centerProperty().bind(stateService.getViewState().visibleCenterPaneProperty());

        stateService
                .getViewState()
                .convertViewProperty()
                .addListener(
                        ((observable, oldValue, newValue) -> {
                            if (newValue) {
                                convertButton.setTextFill(Color.WHITE);
                            } else {
                                convertButton.setTextFill(new Color(1, 1, 1, 0.5));
                            }
                        }));
        convertButtonStepFile
                .visibleProperty()
                .bind(stateService.getViewState().convertViewProperty());
        convertButtonStepProject
                .visibleProperty()
                .bind(stateService.getViewState().convertViewProperty());
        convertButtonStepConvert
                .visibleProperty()
                .bind(stateService.getViewState().convertViewProperty());
        convertButtonStepFile
                .managedProperty()
                .bind(stateService.getViewState().convertViewProperty());
        convertButtonStepProject
                .managedProperty()
                .bind(stateService.getViewState().convertViewProperty());
        convertButtonStepConvert
                .managedProperty()
                .bind(stateService.getViewState().convertViewProperty());

        convertButtonStepFile
                .stateProperty()
                .bind(stateService.getConvertState().stepFileStatusProperty());
        convertButtonStepProject
                .stateProperty()
                .bind(stateService.getConvertState().stepProjectStatusProperty());
        convertButtonStepConvert
                .stateProperty()
                .bind(stateService.getConvertState().stepConvertStatusProperty());

        stateService
                .getViewState()
                .extractViewProperty()
                .addListener(
                        ((observable, oldValue, newValue) -> {
                            if (newValue) {
                                extractButton.setTextFill(Color.WHITE);
                            } else {
                                extractButton.setTextFill(new Color(1, 1, 1, 0.5));
                            }
                        }));

        stateService
                .getConvertState()
                .selectedIfcLineProperty()
                .addListener(
                        ((observableValue, oldValue, selectedIfcLine) -> {
                            selectedIfcLineView.setVisible(Objects.nonNull(selectedIfcLine));
                            selectedIfcLineView.setManaged(Objects.nonNull(selectedIfcLine));
                            ifcLineView.setParsedIfcFile(
                                    stateService.getConvertState().parsedIfcFileProperty());
                            ifcLineView.setIfcLine(selectedIfcLine);
                        }));

        stateService
                .getSelectedFeatureState()
                .featureProperty()
                .addListener(
                        (observable, oldFeature, newFeature) -> {
                            featureView.setFeature(newFeature);
                            if (Objects.nonNull(newFeature)) {
                                selectedFeaturePreview.setVisible(true);
                                selectedFeaturePreview.setManaged(true);
                            } else {
                                selectedFeaturePreview.setVisible(false);
                                selectedFeaturePreview.setManaged(false);
                            }
                        });
    }

    /**
     * Handle action related to input (in this case specifically only responds to keyboard event
     * CTRL-A).
     *
     * @param event Input event.
     */
    public void handleKeyInput(KeyEvent event) {
        if (event != null && event.isControlDown() && event.getCode() == KeyCode.A) {
            provideAboutFunctionality();
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
        // TODO: Clean up if necessary
        stage.close();
    }

    /**
     * Handle action related to "Settings" menu item.
     *
     * @param event Event on "Settings" menu item.
     */
    @FXML
    private void handleSettingsAction(final ActionEvent event) {
        stateService.getViewState().switchCenterPane(SettingsController.class);
    }

    @FXML
    private void handleLoginAction(final ActionEvent event) {
        Task<UserSession> loginTask = authService.getLoginTask();
        stateService.getViewState().switchCenterPane(LoginController.class);
        loginTask.setOnSucceeded(
                t -> {
                    stateService.getViewState().switchCenterPane(AboutController.class);
                    stateService.getLoginState().setUserSession(loginTask.getValue());
                    authService.resetLoginTask();
                });
        loginTask.setOnCancelled(
                t -> {
                    // TODO: Cancelled views
                    stateService.getViewState().switchCenterPane(AboutController.class);
                    stateService.getLoginState().setUserSession(null);
                    authService.resetLoginTask();
                });
        loginTask.setOnFailed(
                t -> {
                    // TODO: Error Handling
                    stateService.getViewState().switchCenterPane(AboutController.class);
                    stateService.getLoginState().setUserSession(null);
                    authService.resetLoginTask();
                });
        new Thread(loginTask).start();
    }

    @FXML
    private void handleLogoutAction(final ActionEvent event) {
        Task<LogoutResult> logoutTask = authService.getLogoutTask();
        logoutTask.setOnSucceeded(
                t -> {
                    stateService.getViewState().switchCenterPane(AboutController.class);
                    stateService.getLoginState().setUserSession(null);
                    authService.resetLogoutTask();
                });
        logoutTask.setOnCancelled(
                t -> {
                    // TODO: Cancelled views
                    stateService.getViewState().switchCenterPane(AboutController.class);
                    authService.resetLogoutTask();
                });
        logoutTask.setOnFailed(
                t -> {
                    // TODO: Error Handling
                    stateService.getViewState().switchCenterPane(AboutController.class);
                    authService.resetLogoutTask();
                });
        new Thread(logoutTask).start();
    }

    @FXML
    private void handleExtractFromIfcAction(final ActionEvent event) {
        stateService.getViewState().switchCenterPane(ExtractController.class);
    }

    /**
     * Handle action related to "Settings" menu item.
     *
     * @param event Event on "Settings" menu item.
     */
    @FXML
    private void handleConvertAction(final ActionEvent event) {
        stateService.getViewState().switchCenterPane(ConvertController.class);
    }

    @FXML
    private void handleLoadProjectsAction(final ActionEvent event) {
        stateService.getViewState().switchCenterPane(ProjectsController.class);
    }

    @FXML
    public void handleCloseLineAction(ActionEvent actionEvent) {
        stateService.getConvertState().closeSelectedIfcLine();
    }

    @FXML
    public void handleCloseSelectedFeatureAction(ActionEvent actionEvent) {
        stateService.getSelectedFeatureState().clearSelectedFeature();
    }

    /** Perform functionality associated with "About" menu selection or CTRL-A. */
    private void provideAboutFunctionality() {
        stateService.getViewState().switchCenterPane(AboutController.class);
    }
}
