package at.researchstudio.sat.mmsdesktop.model.task;

public class DataResult {
    private GraphqlResult data;

    public DataResult(GraphqlResult data) {
        this.data = data;
    }

    public GraphqlResult getData() {
        return data;
    }

    public void setData(GraphqlResult data) {
        this.data = data;
    }
}
