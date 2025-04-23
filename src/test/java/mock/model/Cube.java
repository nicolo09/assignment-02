package mock.model;

public class Cube {

    private Point3D point1;
    private Point3D point2;

    public Cube(Point3D point1, Point3D point2) {
        this.point1 = point1;
        this.point2 = point2;
    }

    public Point3D getPoint1() {
        return point1;
    }

    public Point3D getPoint2() {
        return point2;
    }

}
