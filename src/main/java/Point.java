public class Point {

    private float x;
    private float y;
    private float weight;

    public Point(float x, float y, float weight) {
        this.x = x;
        this.y = y;
        this.weight = weight;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    @Override
    public String toString() {
        return "Point{" +
                "x=" + x +
                ", y=" + y +
                ", weight=" + weight +
                '}';
    }
}

