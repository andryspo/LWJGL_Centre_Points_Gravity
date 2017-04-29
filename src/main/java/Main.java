import org.lwjgl.LWJGLException;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) throws LWJGLException {
        final int range = 10;
        final int n = 10000;

        List<Point> points = new ArrayList<>();
        PointUtil pointUtil = new PointUtil(points);

        for (int i = 0; i < n; i++) {
            Point point = new Point(getRandom(range), getRandom(range), getRandom(range));
            points.add(point);
            //System.out.println(point);
        }

        MyCLClass myCLClass = new MyCLClass(points.size(), pointUtil);

        myCLClass.center_weight();
    }

    private static float getRandom(int range) {
        return (float) Math.random() * range;
    }
}