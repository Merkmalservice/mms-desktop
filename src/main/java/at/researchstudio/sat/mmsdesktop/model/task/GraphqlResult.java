package at.researchstudio.sat.mmsdesktop.model.task;

import at.researchstudio.sat.merkmalservice.model.Project;

import java.util.ArrayList;
import java.util.List;

public class GraphqlResult {
    private ArrayList<Project> projects;

    public GraphqlResult(ArrayList<Project> projects) {
        this.projects = projects;
    }

    public List<Project> getProjects() {
        return projects;
    }

    public void setProjects(ArrayList<Project> projects) {
        this.projects = projects;
    }
}
