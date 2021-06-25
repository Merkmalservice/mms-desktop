package at.researchstudio.sat.mmsdesktop.controller;

import com.sandec.mdfx.MarkdownView;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.BorderPane;
import org.springframework.stereotype.Component;

@Component
public class AboutController implements Initializable {
    @FXML private BorderPane parentPane;
    @FXML private MarkdownView centerAboutMarkdown;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // TODO: SEE https://github.com/JPro-one/markdown-javafx-renderer for
        // reference
        // centerAboutMarkdown = new MarkdownView("#bla ##blabla");
        centerAboutMarkdown.setMdString(
                "# Merkmalservice Desktop App\n"
                        + "\n"
                        + "## Requirements\n"
                        + "- Java 11 or higher\n"
                        + "\n"
                        + "## Architecture\n"
                        + "- Spring Boot Application\n"
                        + "- JavaFX UI (Version 16)\n"
                        + "- Maven\n"
                        + "- [JFoenix (JFX Component Library)](http://www.jfoenix.com/) \n"
                        + "\n"
                        + "## How to Start (for now)\n"
                        + "1. Checkout Project\n"
                        + "2. `mvn spring-boot:run`\n"
                        + "\n"
                        + "## How to Package as executable jar (for now)\n"
                        + "1. Checkout Project\n"
                        + "2. mvn clean install spring-boot:repackage\n"
                        + "3. execute `target/mms-desktop-spring-boot.jar` directly\n");
    }
}
