package at.researchstudio.sat.mmsdesktop.service;

import at.researchstudio.sat.mmsdesktop.gui.ViewState;
import at.researchstudio.sat.mmsdesktop.gui.component.featuretable.SelectedFeatureState;
import at.researchstudio.sat.mmsdesktop.gui.convert.ConvertState;
import at.researchstudio.sat.mmsdesktop.gui.extract.ExtractState;
import at.researchstudio.sat.mmsdesktop.gui.login.LoginState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ReactiveStateService {
    private final LoginState loginState;
    private final ExtractState extractState;
    private final ConvertState convertState;
    private final ViewState viewState;
    private final SelectedFeatureState selectedFeatureState;

    @Autowired
    public ReactiveStateService(
            LoginState loginState,
            ExtractState extractState,
            ConvertState convertState,
            ViewState viewState,
            SelectedFeatureState selectedFeatureState) {
        this.loginState = loginState;
        this.extractState = extractState;
        this.viewState = viewState;
        this.convertState = convertState;
        this.selectedFeatureState = selectedFeatureState;
    }

    public LoginState getLoginState() {
        return loginState;
    }

    public ExtractState getExtractState() {
        return extractState;
    }

    public ConvertState getConvertState() {
        return convertState;
    }

    public ViewState getViewState() {
        return viewState;
    }

    public SelectedFeatureState getSelectedFeatureState() {
        return selectedFeatureState;
    }
}
