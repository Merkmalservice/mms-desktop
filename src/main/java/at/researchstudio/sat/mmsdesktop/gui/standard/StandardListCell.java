package at.researchstudio.sat.mmsdesktop.gui.standard;

import at.researchstudio.sat.merkmalservice.model.Organization;
import at.researchstudio.sat.merkmalservice.model.Standard;
import com.jfoenix.controls.JFXListCell;
import java.util.Optional;
import javafx.scene.control.Label;

public class StandardListCell<P> extends JFXListCell<Standard> {
    @Override
    protected void updateItem(Standard item, boolean empty) {
        super.updateItem(item, empty);
        if (!empty && item != null) {
            setText(null);
            setGraphic(
                    new Label(
                            Optional.ofNullable(item.getOrganization())
                                    .map(Organization::getName)
                                    .orElse("PROJECT STANDARD")));
            // TODO: THIS IS NOT THE CORRECT WAY OF HANDLING THE PROJECTSTANDARDNAME it should be
            // projectname::orgname (if its a standard from an organization) or standardname (if it
            // is a projectstandard)
        }
    }
}
