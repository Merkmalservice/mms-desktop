package at.researchstudio.sat.mmsdesktop.model.task;

import java.util.ArrayList;

public class GraphqlResult {
    private ArrayList<ProjectResult> projects;

    public GraphqlResult(ArrayList<ProjectResult> projects) {
        this.projects = projects;
    }

    public ArrayList<ProjectResult> getProjects() {
        return projects;
    }

    public void setProjects(ArrayList<ProjectResult> projects) {
        this.projects = projects;
    }
}
