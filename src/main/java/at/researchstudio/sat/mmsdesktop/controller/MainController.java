package at.researchstudio.sat.mmsdesktop.controller;

import at.researchstudio.sat.mmsdesktop.AuthService;
import at.researchstudio.sat.mmsdesktop.model.auth.UserSession;
import at.researchstudio.sat.mmsdesktop.model.task.LogoutResult;
import java.lang.invoke.MethodHandles;
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
import javafx.stage.Stage;
import net.rgielen.fxweaver.core.FxWeaver;
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
    private ResourceBundle resourceBundle;

    @FXML private MenuBar menuBar;
    @FXML private MenuItem menuBarLogin;
    @FXML private MenuItem menuBarLogout;

    @FXML private BorderPane mainPane;

    private final AuthService authService;
    private final FxWeaver fxWeaver;

    @Autowired
    public MainController(AuthService authService, FxWeaver fxWeaver) {
        this.authService = authService;
        this.fxWeaver = fxWeaver;
    }

    @Override
    public void initialize(java.net.URL arg0, ResourceBundle resources) {
        this.resourceBundle = resources;
        menuBar.setFocusTraversable(true);

        menuBarLogin.visibleProperty().bind(authService.loggedInProperty().not());
        menuBarLogout.visibleProperty().bind(authService.loggedInProperty());
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
        switchCenterPane(SettingsController.class);
    }

    @FXML
    private void handleLoginAction(final ActionEvent event) {
        Task<UserSession> loginTask = authService.getLoginTask();
        switchCenterPane(LoginController.class);

        loginTask.setOnSucceeded(
                t -> {
                    switchCenterPane(AboutController.class);
                    UserSession session = loginTask.getValue();
                    authService.userNameProperty().setValue(session.getUsername());
                    authService.loggedInProperty().setValue(true);
                });

        loginTask.setOnCancelled(
                t -> {
                    // TODO: Cancelled views
                    switchCenterPane(AboutController.class);
                    logger.info("Loginprocess Cancelled");
                });

        loginTask.setOnFailed(
                t -> {
                    // TODO: Error Handling
                    switchCenterPane(AboutController.class);
                    logger.info("Loginprocess Failed");
                });

        new Thread(loginTask).start();
    }

    @FXML
    private void handleLogoutAction(final ActionEvent event) {
        Task<LogoutResult> logoutTask = authService.getLogoutTask();

        logoutTask.setOnSucceeded(
                t -> {
                    LogoutResult r = logoutTask.getValue();
                    authService.userNameProperty().setValue("Anonymous");
                    authService.loggedInProperty().setValue(false);
                });

        logoutTask.setOnCancelled(
                t -> {
                    // TODO: Cancelled views
                    logger.info("Logoutprocess Cancelled");
                });

        logoutTask.setOnFailed(
                t -> {
                    // TODO: Error Handling
                    switchCenterPane(AboutController.class);
                });

        new Thread(logoutTask).start();
    }

    /**
     * Handle action related to "Settings" menu item.
     *
     * @param event Event on "Settings" menu item.
     */
    @FXML
    private void handleExtractAction(final ActionEvent event) {
        switchCenterPane(ExtractController.class);
    }

    /**
     * Handle action related to "Settings" menu item.
     *
     * @param event Event on "Settings" menu item.
     */
    @FXML
    private void handleConvertAction(final ActionEvent event) {
        switchCenterPane(ConvertController.class);
    }

    /** Perform functionality associated with "About" menu selection or CTRL-A. */
    private void provideAboutFunctionality() {
        switchCenterPane(AboutController.class);
    }

    private void switchCenterPane(Class controllerClass) {
        mainPane.setCenter(fxWeaver.loadView(controllerClass, resourceBundle));
    }
}
