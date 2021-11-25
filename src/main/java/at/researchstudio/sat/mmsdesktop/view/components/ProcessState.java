package at.researchstudio.sat.mmsdesktop.view.components;

public enum ProcessState {
    STEP_OPEN,
    STEP_ACTIVE,
    STEP_COMPLETE,
    STEP_DISABLED,
    STEP_FAILED,
    STEP_PROCESSING;

    public boolean isOpen() {
        return this == STEP_OPEN;
    }

    public boolean isActive() {
        return this == STEP_ACTIVE;
    }

    public boolean isComplete() {
        return this == STEP_COMPLETE;
    }

    public boolean isDisabled() {
        return this == STEP_DISABLED;
    }

    public boolean isFailed() {
        return this == STEP_FAILED;
    }

    public boolean isProcessing() {
        return this == STEP_PROCESSING;
    }
}
