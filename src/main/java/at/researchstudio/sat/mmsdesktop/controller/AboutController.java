package at.researchstudio.sat.mmsdesktop.controller;

import com.sandec.mdfx.MarkdownView;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.BorderPane;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;

@Component
@FxmlView("about.fxml")
public class AboutController implements Initializable {
    @FXML private BorderPane parentPane;
    @FXML private MarkdownView centerAboutMarkdown;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // TODO: SEE https://github.com/JPro-one/markdown-javafx-renderer for
        // reference
        // centerAboutMarkdown = new MarkdownView("#bla ##blabla");
        centerAboutMarkdown.setMdString(
                "### Merkmalservice Desktop App\n"
                        + "\n"
                        + "#### Version\n"
                        + "- 0.0.2\n"
                        + "#### Usage\n"
                        + "##### Extract Features from IFC:\n"
                        + "- Add to List:\n"
                        + "    - Select single Files\n"
                        + "    - Select all Files in a Folder\n"
                        + "- Optional: Remove all Files again\n"
                        + "- Parse:\n"
                        + "    - All Features will be parsed to the output File\n"
                        + "- Export:\n"
                        + "    - JSON (with or without Unique Values in Description)\n"
                        + "    - Logfile\n"
                        + "- Check Log Output\n"
                        + "##### View Extracted Features from IFC:\n"
                        + "- View Features in Table\n"
                        + "- View Specific Features and all unique extracted Values\n"
                        + "#### Support\n"
                        + " > sat@researchstudio.at");
    }
}
