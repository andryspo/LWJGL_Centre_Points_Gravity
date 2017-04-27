import java.util.List;

public class PointUtil {

    private List<Point> points;

    public PointUtil(List<Point> points) {
        this.points = points;
    }

    public float[] getX() {

        float[] x = new float[points.size()];

        for(int i = 0; i < points.size(); i++) {
            x[i] = points.get(i).getX();
        }

        return x;
    }

    public float[] getY() {

        float[] y = new float[points.size()];

        for(int i = 0; i < points.size(); i++) {
            y[i] = points.get(i).getY();
        }

        return y;
    }

    public float[] getWeight() {

        float[] weight = new float[points.size()];

        for(int i = 0; i < points.size(); i++) {
            weight[i] = points.get(i).getWeight();
        }

        return weight;
    }

    public List<Point> getPoints() {
        return points;
    }

    public void setPoints(List<Point> points) {
        this.points = points;
    }
}
