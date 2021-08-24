package at.researchstudio.sat.mmsdesktop.service;

import at.researchstudio.sat.mmsdesktop.state.ExtractState;
import at.researchstudio.sat.mmsdesktop.state.LoginState;
import at.researchstudio.sat.mmsdesktop.state.SelectedFeatureState;
import at.researchstudio.sat.mmsdesktop.state.ViewState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ReactiveStateService {
    private final LoginState loginState;
    private final ExtractState extractState;
    private final ViewState viewState;
    private final SelectedFeatureState selectedFeatureState;

    @Autowired
    public ReactiveStateService(
            LoginState loginState,
            ExtractState extractState,
            ViewState viewState,
            SelectedFeatureState selectedFeatureState) {
        this.loginState = loginState;
        this.extractState = extractState;
        this.viewState = viewState;
        this.selectedFeatureState = selectedFeatureState;
    }

    public LoginState getLoginState() {
        return loginState;
    }

    public ExtractState getExtractState() {
        return extractState;
    }

    public ViewState getViewState() {
        return viewState;
    }

    public SelectedFeatureState getSelectedFeatureState() {
        return selectedFeatureState;
    }
}
