package at.researchstudio.sat.mmsdesktop.gui.project;

import at.researchstudio.sat.merkmalservice.model.Project;
import com.jfoenix.controls.JFXListCell;
import javafx.scene.control.Label;

public class ProjectListCell<P> extends JFXListCell<Project> {
    @Override
    protected void updateItem(Project item, boolean empty) {
        super.updateItem(item, empty);
        if (!empty && item != null) {
            setText(null);
            setGraphic(new Label(item.getName()));
        }
    }
}
