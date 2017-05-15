import org.lwjgl.LWJGLException;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) throws LWJGLException {
        final int range = 10;
        final int n = 10;
        final int threads_count = 4;

        List<Point> points = new ArrayList<>();
        PointUtil pointUtil = new PointUtil(points);

//        points.add(new Point(1, 1,1));
//        points.add(new Point(2, 2,2));
//        points.add(new Point(3, 3,3));
//        points.add(new Point(4, 4,4));
//        points.add(new Point(4, 4,5));

        for (int i = 0; i < n; i++) {
            Point point = new Point(getRandom(range), getRandom(range), getRandom(range));
            //Point point = new Point(10, 10,10);
            points.add(point);
            System.out.println(point);
        }

        MyCLClass myCLClass = new MyCLClass(points.size(), pointUtil, threads_count);
        myCLClass.center_weight();
    }

    private static float getRandom(int range) {
        return (float) Math.random() * range;
    }
}