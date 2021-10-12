package at.researchstudio.sat.mmsdesktop.controller.components;

import at.researchstudio.sat.mmsdesktop.service.ReactiveStateService;
import com.jfoenix.controls.JFXListView;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.ListChangeListener;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.AnchorPane;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@FxmlView("featureTableComponent.fxml")
public class FeatureSetComponentController implements Initializable {
    private final ReactiveStateService stateService;

    @FXML public AnchorPane parentPane;
    @FXML private JFXListView<FeatureSetControl> featureSetList;

    @Autowired
    public FeatureSetComponentController(ReactiveStateService stateService) {
        this.stateService = stateService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resourceBundle) {
        this.parentPane
                .visibleProperty()
                .bind(stateService.getExtractState().extractedFeatureSetsPresentProperty());
        this.parentPane
                .managedProperty()
                .bind(stateService.getExtractState().extractedFeatureSetsPresentProperty());

        this.featureSetList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        this.featureSetList.getSelectionModel().selectAll();

        this.featureSetList
                .getSelectionModel()
                .getSelectedItems()
                .addListener(
                        (ListChangeListener<FeatureSetControl>)
                                listener ->
                                        stateService
                                                .getExtractState()
                                                .getFilteredExtractedFeatures()
                                                .setPredicate(
                                                        feature -> {
                                                            List<? extends FeatureSetControl>
                                                                    selectedList =
                                                                            listener.getList();

                                                            if (selectedList != null
                                                                    && !selectedList.isEmpty()) {
                                                                for (FeatureSetControl fsc :
                                                                        selectedList) {
                                                                    if (fsc.getFeatureSet()
                                                                            .getFeatures()
                                                                            .contains(feature)) {
                                                                        return true;
                                                                    }
                                                                }
                                                            }
                                                            return false;
                                                        }));
    }

    public SortedList<FeatureSetControl> getSortedFeatureSets() {
        return stateService.getExtractState().getSortedFeatureSets();
    }
}
