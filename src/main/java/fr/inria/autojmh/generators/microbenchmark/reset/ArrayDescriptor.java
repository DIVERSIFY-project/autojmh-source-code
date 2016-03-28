package fr.inria.autojmh.generators.microbenchmark.reset;

import java.util.ArrayList;

/**
 * Class to describe arrays
 *
 * Created by marodrig on 02/02/2016.
 */
public class ArrayDescriptor {

    private ArrayList<Integer> dimentions;
    private ArrayList<Integer> data;

    public ArrayList<Integer> getDimentions() {
        return dimentions;
    }

    public void setDimentions(ArrayList<Integer> dimentions) {
        this.dimentions = dimentions;
    }

    public ArrayList<Integer> getData() {
        return data;
    }

    public void setData(ArrayList<Integer> data) {
        this.data = data;
    }
}
