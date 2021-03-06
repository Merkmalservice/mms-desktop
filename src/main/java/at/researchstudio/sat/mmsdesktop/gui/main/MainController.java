package at.researchstudio.sat.mmsdesktop.gui.main;

import static at.researchstudio.sat.mmsdesktop.view.components.ProcessState.STEP_COMPLETE;
import static at.researchstudio.sat.mmsdesktop.view.components.ProcessState.STEP_PROCESSING;

import at.researchstudio.sat.mmsdesktop.constants.ViewConstants;
import at.researchstudio.sat.mmsdesktop.gui.ViewState;
import at.researchstudio.sat.mmsdesktop.gui.about.AboutController;
import at.researchstudio.sat.mmsdesktop.gui.component.feature.FeatureBox;
import at.researchstudio.sat.mmsdesktop.gui.component.ifc.IfcLineView;
import at.researchstudio.sat.mmsdesktop.gui.convert.inputfile.SelectInputFileController;
import at.researchstudio.sat.mmsdesktop.gui.convert.perform.PerformConversionController;
import at.researchstudio.sat.mmsdesktop.gui.convert.perform.PerformConversionState;
import at.researchstudio.sat.mmsdesktop.gui.convert.targetstandard.SelectTargetStandardController;
import at.researchstudio.sat.mmsdesktop.gui.extract.ExtractController;
import at.researchstudio.sat.mmsdesktop.gui.login.LoginController;
import at.researchstudio.sat.mmsdesktop.gui.settings.SettingsController;
import at.researchstudio.sat.mmsdesktop.model.auth.UserSession;
import at.researchstudio.sat.mmsdesktop.model.task.LogoutResult;
import at.researchstudio.sat.mmsdesktop.service.AuthService;
import at.researchstudio.sat.mmsdesktop.service.ReactiveStateService;
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

    @FXML private BorderPane selectedIfcLineView;
    @FXML private IfcLineView ifcLineView;

    @FXML private BorderPane selectedChangedIfcLineView;
    @FXML private IfcLineView ifcSourceLineView;
    @FXML private IfcLineView ifcTargetLineView;

    @FXML private BorderPane selectedFeaturePreview;
    @FXML private FeatureBox featureView;

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
        convertButtonSelectInputFile
                .disableProperty()
                .bind(
                        stateService
                                .getConvertState()
                                .getPerformConversionState()
                                .stepPerformConversionStatusProperty()
                                .isEqualTo(STEP_PROCESSING));
        convertButtonSelectTargetStandard
                .stateProperty()
                .bind(
                        stateService
                                .getConvertState()
                                .getTargetStandardState()
                                .stepTargetStandardStatusProperty());
        convertButtonSelectTargetStandard
                .disableProperty()
                .bind(
                        stateService
                                .getConvertState()
                                .getPerformConversionState()
                                .stepPerformConversionStatusProperty()
                                .isEqualTo(STEP_PROCESSING));
        convertButtonPerformConversion
                .stateProperty()
                .bind(
                        stateService
                                .getConvertState()
                                .getPerformConversionState()
                                .stepPerformConversionStatusProperty());

        convertButtonPerformConversion
                .disableProperty()
                .bind(
                        stateService
                                .getConvertState()
                                .getTargetStandardState()
                                .selectedTargetStandardProperty()
                                .isNull()
                                .or(
                                        stateService
                                                .getConvertState()
                                                .getInputFileState()
                                                .stepFileStatusProperty()
                                                .isNotEqualTo(STEP_COMPLETE)));

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
                .getConvertState()
                .getOutputFileState()
                .selectedChangedIfcLineProperty()
                .addListener(
                        ((observableValue, oldValue, selectedIfcLine) -> {
                            selectedChangedIfcLineView.setVisible(Objects.nonNull(selectedIfcLine));
                            selectedChangedIfcLineView.setManaged(Objects.nonNull(selectedIfcLine));
                            ifcSourceLineView.setParsedIfcFile(
                                    stateService
                                            .getConvertState()
                                            .getInputFileState()
                                            .parsedIfcFileProperty());
                            ifcSourceLineView.setIfcLine(selectedIfcLine);
                            ifcTargetLineView.setParsedIfcFile(
                                    stateService
                                            .getConvertState()
                                            .getOutputFileState()
                                            .convertedIfcFileProperty());
                            ifcTargetLineView.setIfcLine(selectedIfcLine);
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
                .addListener(this::enablePerformConversionIfPossible);
        stateService
                .getConvertState()
                .getTargetStandardState()
                .stepTargetStandardStatusProperty()
                .addListener(this::enablePerformConversionIfPossible);
    }

    private void enablePerformConversionIfPossible(
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
                    authService.resetLoginTask();
                });
        loginTask.setOnCancelled(
                t -> {
                    // TODO: Cancelled views
                    stateService.getViewState().switchCenterPane(AboutController.class);
                    authService.resetLoginTask();
                });
        loginTask.setOnFailed(
                t -> {
                    // TODO: Error Handling
                    stateService.getViewState().switchCenterPane(AboutController.class);
                    authService.resetLoginTask();
                });
        new Thread(loginTask).start();
    }

    @FXML
    private void handleLogoutAction(final ActionEvent event) {
        Task<LogoutResult> logoutTask = authService.getLogoutTask();
        stateService.getViewState().switchCenterPane(LoginController.class);
        if (!logoutTask.isRunning()) {
            logoutTask.setOnSucceeded(
                    t -> {
                        stateService.getViewState().switchCenterPane(AboutController.class);
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
        } else {
            logger.info("Logout process still running, action will not be called again");
        }
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
    private void handleSelectInputFileAction(final ActionEvent event) {
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
    private void handleSelectTargetStandardAction(final ActionEvent event) {
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
        if (stateService
                .getConvertState()
                .getPerformConversionState()
                .stepPerformConversionStatusProperty()
                .get()
                .isActive()) {
            stateService
                    .getConvertState()
                    .getPerformConversionState()
                    .stepPerformConversionStatusProperty()
                    .set(ProcessState.STEP_OPEN);
        }
        if (stateService
                .getConvertState()
                .getPerformConversionState()
                .stepPerformConversionStatusProperty()
                .get()
                .isOpen()) {
            stateService
                    .getConvertState()
                    .getPerformConversionState()
                    .stepPerformConversionStatusProperty()
                    .set(ProcessState.STEP_ACTIVE);
        }
    }

    @FXML
    public void handleCloseLineAction(ActionEvent actionEvent) {
        stateService.getConvertState().getInputFileState().closeSelectedIfcLine();
    }

    @FXML
    public void handleCloseSelectedChangedLineAction(ActionEvent actionEvent) {
        stateService.getConvertState().getOutputFileState().closeSelectedChangedIfcLine();
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
