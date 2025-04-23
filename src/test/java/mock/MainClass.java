package mock;

import mock.model.Cube;
import mock.model.Point3D;
import mock.view.CubeView;

public class MainClass {

    public static void main(String[] args) {
        Cube cube = new Cube(new Point3D(1, 2, 3), new Point3D(4, 5, 6));
        CubeView cubeView = new CubeView("MyCube", "Red", 10, cube);
        cubeView.getColor();
    }

}
