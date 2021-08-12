package at.researchstudio.sat.mmsdesktop.service;

import at.researchstudio.sat.mmsdesktop.state.ExtractState;
import at.researchstudio.sat.mmsdesktop.state.LoginState;
import at.researchstudio.sat.mmsdesktop.state.ViewState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ReactiveStateService {
    private final LoginState loginState;
    private final ExtractState extractState;
    private final ViewState viewState;

    @Autowired
    public ReactiveStateService(
            LoginState loginState, ExtractState extractState, ViewState viewState) {
        this.loginState = loginState;
        this.extractState = extractState;
        this.viewState = viewState;
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
}
