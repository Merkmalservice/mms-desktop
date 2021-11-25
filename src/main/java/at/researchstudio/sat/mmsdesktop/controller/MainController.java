package at.researchstudio.sat.mmsdesktop.controller;

import at.researchstudio.sat.mmsdesktop.constants.ViewConstants;
import at.researchstudio.sat.mmsdesktop.controller.components.FeatureView;
import at.researchstudio.sat.mmsdesktop.controller.components.IfcLineView;
import at.researchstudio.sat.mmsdesktop.model.auth.UserSession;
import at.researchstudio.sat.mmsdesktop.model.task.LogoutResult;
import at.researchstudio.sat.mmsdesktop.service.AuthService;
import at.researchstudio.sat.mmsdesktop.service.ReactiveStateService;
import at.researchstudio.sat.mmsdesktop.state.PerformConversionState;
import at.researchstudio.sat.mmsdesktop.state.ViewState;
import at.researchstudio.sat.mmsdesktop.view.components.JFXStepButton;
import at.researchstudio.sat.mmsdesktop.view.components.ProcessState;
import com.jfoenix.controls.JFXButton;
import java.lang.invoke.MethodHandles;
import java.util.Objects;
import java.util.ResourceBundle;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
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
    @FXML public JFXStepButton convertButtonSelectInputFile;
    @FXML public JFXStepButton convertButtonSelectTargetStandard;
    @FXML public JFXStepButton convertButtonPerformConversion;

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
        mainPane.centerProperty().bind(stateService.getViewState().visibleCenterPaneProperty());

        stateService
                .getViewState()
                .activeProperty()
                .addListener(
                        ((observable, oldValue, newActiveValue) -> {
                            convertButton.setTextFill(ViewConstants.MENU_COLOR_INACTIVE);
                            extractButton.setTextFill(ViewConstants.MENU_COLOR_INACTIVE);

                            switch (newActiveValue.intValue()) {
                                case ViewState.CONVERT:
                                    convertButton.setTextFill(ViewConstants.MENU_COLOR_ACTIVE);
                                    break;
                                case ViewState.EXTRACT:
                                    extractButton.setTextFill(ViewConstants.MENU_COLOR_ACTIVE);
                                    break;
                                case ViewState.OTHER:
                                default:
                                    break;
                            }
                        }));

        convertButtonSelectInputFile
                .visibleProperty()
                .bind(stateService.getViewState().activeProperty().isEqualTo(ViewState.CONVERT));
        convertButtonSelectInputFile
                .managedProperty()
                .bind(stateService.getViewState().activeProperty().isEqualTo(ViewState.CONVERT));
        convertButtonSelectTargetStandard
                .visibleProperty()
                .bind(stateService.getViewState().activeProperty().isEqualTo(ViewState.CONVERT));
        convertButtonSelectTargetStandard
                .managedProperty()
                .bind(stateService.getViewState().activeProperty().isEqualTo(ViewState.CONVERT));
        convertButtonPerformConversion
                .managedProperty()
                .bind(stateService.getViewState().activeProperty().isEqualTo(ViewState.CONVERT));
        convertButtonPerformConversion
                .visibleProperty()
                .bind(stateService.getViewState().activeProperty().isEqualTo(ViewState.CONVERT));

        convertButtonSelectInputFile
                .stateProperty()
                .bind(stateService.getConvertState().getInputFileState().stepFileStatusProperty());
        convertButtonSelectTargetStandard
                .stateProperty()
                .bind(
                        stateService
                                .getConvertState()
                                .getTargetStandardState()
                                .stepTargetStandardStatusProperty());
        convertButtonPerformConversion
                .stateProperty()
                .bind(
                        stateService
                                .getConvertState()
                                .getPerformConversionState()
                                .stepPerformConversionStatusProperty());

        stateService
                .getConvertState()
                .getInputFileState()
                .selectedIfcLineProperty()
                .addListener(
                        ((observableValue, oldValue, selectedIfcLine) -> {
                            selectedIfcLineView.setVisible(Objects.nonNull(selectedIfcLine));
                            selectedIfcLineView.setManaged(Objects.nonNull(selectedIfcLine));
                            ifcLineView.setParsedIfcFile(
                                    stateService
                                            .getConvertState()
                                            .getInputFileState()
                                            .parsedIfcFileProperty());
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
        stateService
                .getConvertState()
                .getInputFileState()
                .stepFileStatusProperty()
                .addListener(this::onRequiredSelectionStateChange);
        stateService
                .getConvertState()
                .getTargetStandardState()
                .stepTargetStandardStatusProperty()
                .addListener(this::onRequiredSelectionStateChange);
    }

    private void onRequiredSelectionStateChange(
            ObservableValue<? extends ProcessState> observable,
            ProcessState oldState,
            ProcessState newState) {

        ProcessState selectFileStatus =
                stateService.getConvertState().getInputFileState().stepFileStatusProperty().get();
        ProcessState selectTargetStandardStatus =
                stateService
                        .getConvertState()
                        .getTargetStandardState()
                        .stepTargetStandardStatusProperty()
                        .get();
        PerformConversionState performConversionState =
                stateService.getConvertState().getPerformConversionState();
        if (selectFileStatus.isComplete() && selectTargetStandardStatus.isComplete()) {
            ProcessState performConversionStatus =
                    performConversionState.stepPerformConversionStatusProperty().get();
            if (performConversionStatus.isDisabled()) {
                performConversionState
                        .stepPerformConversionStatusProperty()
                        .set(ProcessState.STEP_OPEN);
            }
        } else {
            performConversionState
                    .stepPerformConversionStatusProperty()
                    .set(ProcessState.STEP_DISABLED);
        }
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
    private void handleConvertSelectInputFileAction(final ActionEvent event) {
        stateService.getViewState().switchCenterPane(SelectInputFileController.class);
        if (stateService
                .getConvertState()
                .getInputFileState()
                .stepFileStatusProperty()
                .get()
                .isOpen()) {
            stateService
                    .getConvertState()
                    .getInputFileState()
                    .stepFileStatusProperty()
                    .set(ProcessState.STEP_ACTIVE);
        }
        if (stateService
                .getConvertState()
                .getTargetStandardState()
                .stepTargetStandardStatusProperty()
                .get()
                .isActive()) {
            stateService
                    .getConvertState()
                    .getTargetStandardState()
                    .stepTargetStandardStatusProperty()
                    .set(ProcessState.STEP_OPEN);
        }
    }

    @FXML
    private void handleConvertSelectTargetStandardAction(final ActionEvent event) {
        stateService.getViewState().switchCenterPane(SelectTargetStandardController.class);
        if (stateService
                .getConvertState()
                .getInputFileState()
                .stepFileStatusProperty()
                .get()
                .isActive()) {
            stateService
                    .getConvertState()
                    .getInputFileState()
                    .stepFileStatusProperty()
                    .set(ProcessState.STEP_OPEN);
        }
        if (stateService
                .getConvertState()
                .getTargetStandardState()
                .stepTargetStandardStatusProperty()
                .get()
                .isOpen()) {
            stateService
                    .getConvertState()
                    .getTargetStandardState()
                    .stepTargetStandardStatusProperty()
                    .set(ProcessState.STEP_ACTIVE);
        }
    }

    @FXML
    private void handleConvertPerformConversionAction(final ActionEvent event) {
        stateService.getViewState().switchCenterPane(PerformConversionController.class);
    }

    @FXML
    private void handleLoadProjectsAction(final ActionEvent event) {
        stateService.getViewState().switchCenterPane(ProjectsController.class);
    }

    @FXML
    public void handleCloseLineAction(ActionEvent actionEvent) {
        stateService.getConvertState().getInputFileState().closeSelectedIfcLine();
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
