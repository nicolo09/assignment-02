package mock.view;

import mock.model.Cube;

public class CubeView {

    private Cube cube;
    private String name;
    private String color;
    private int size;

    public CubeView(String name, String color, int size, Cube cube) {
        this.cube = cube;
        this.name = name;
        this.color = color;
        this.size = size;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public int getSize() {
        return size;
    }

    public Cube getCube() {
        return cube;
    }

}
