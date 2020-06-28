package com.mygdx.game;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

enum ClickMode {
    NONE,
    MAKE_PAINT_SOURCE,
    MAKE_FLOW,
}

//object class used to represent flowpoints in the simulation
class Flow {

    private Pair<Integer, Integer> location;
    private Pair<Float, Float> velocity;

    public Flow(Pair<Integer, Integer> location, Pair<Float, Float> velocity) {
        this.location = location;
        this.velocity = velocity;
    }

    public Pair<Integer, Integer> getLocation() {
        return location;
    }

    public void setLocation(Pair<Integer, Integer> location) {
        this.location = location;
    }

    public Pair<Float, Float> getVelocity() {
        return velocity;
    }

    public void setVelocity(Pair<Float, Float> velocity) {
        this.velocity = velocity;
    }
}

public class Fluid {

    ClickMode click_mode = ClickMode.NONE;

    //used for storing a click which is needed when creating a flow
    Pair<Integer, Integer> stored_click = null;

    List<Pair<Integer, Integer>> paint_sources = new ArrayList<>();

    List<Flow> flows = new ArrayList<>();

    int size;
    float time_change;
    float diffusion;
    float viscosity;

    List<List<Float>> s = new ArrayList<>();
    List<List<Float>> density = new ArrayList<>();

    List<List<Float>> vel_x = new ArrayList<>();
    List<List<Float>> vel_y = new ArrayList<>();

    List<List<Float>> old_vel_x = new ArrayList<>();
    List<List<Float>> old_vel_y = new ArrayList<>();

    public Fluid(int size, float diffusion, float viscosity, float dt) {
        this.size = size;
        this.diffusion = diffusion;
        this.viscosity = viscosity;
        this.time_change = dt;

        for (int size_iterator = 0; size_iterator < size; size_iterator++) {
            s.add(new ArrayList<>(Collections.nCopies(size, 0f)));
            density.add(new ArrayList<>(Collections.nCopies(size, 0f)));
            vel_x.add(new ArrayList<>(Collections.nCopies(size, 0f)));
            vel_y.add(new ArrayList<>(Collections.nCopies(size, 0f)));
            old_vel_x.add(new ArrayList<>(Collections.nCopies(size, 0f)));
            old_vel_y.add(new ArrayList<>(Collections.nCopies(size, 0f)));
        }
    }

    public void addDensity(int x, int y, float amount) {
        this.density.get(x).set(y, this.density.get(x).get(y) + amount);
    }

    public void addVelocity(int x, int y, float amount_x, float amount_y) {
        this.vel_x.get(x).set(y, this.vel_x.get(x).get(y) + amount_x);
        this.vel_y.get(x).set(y, this.vel_y.get(x).get(y) + amount_y);
    }

    public void advance() {
        applySources();

        diffuse(1, old_vel_x, vel_x, viscosity, time_change, 4);
        diffuse(2, old_vel_y, vel_y, viscosity, time_change, 4);

        project(old_vel_x, old_vel_y, vel_x, vel_y, 4);

        advect(1, vel_x, old_vel_x, old_vel_x, old_vel_y, time_change);
        advect(2, vel_y, old_vel_y, old_vel_x, old_vel_y, time_change);

        project(vel_x, vel_y, old_vel_x, old_vel_y, 4);

        diffuse(0, s, density, diffusion, time_change, 4);
        advect(0, density, s, vel_x, vel_y, time_change);
    }

    /**
     * adds flow sources and paint sources to the simulation
      */
    public void applySources(){
        for(Flow flow:flows){
            vel_x.get(flow.getLocation().getKey()).set(flow.getLocation().getValue(),flow.getVelocity().getKey());
            vel_y.get(flow.getLocation().getKey()).set(flow.getLocation().getValue(),flow.getVelocity().getValue());
        }

        for(Pair<Integer,Integer> source:paint_sources){
            addDensity(source.getKey(),source.getValue(),1);
        }

    }

    public void set_boundary(int b, List<List<Float>> array) {
        for (int i = 1; i < size - 1; i++) {
            array.get(i).set(0, b == 2 ? -array.get(i).get(1) : array.get(i).get(1));
            array.get(i).set(size - 1, b == 2 ? -array.get(i).get(size - 2) : array.get(i).get(size - 2));
        }
        for (int i = 1; i < size - 1; i++) {
            array.get(0).set(i, b == 2 ? -array.get(1).get(i) : array.get(1).get(i));
            array.get(size - 1).set(i, b == 2 ? -array.get(size - 2).get(i) : array.get(size - 2).get(i));
        }

        array.get(0).set(0, 0.5f * (array.get(1).get(0) + array.get(0).get(1)));
        array.get(0).set(size - 1, 0.5f * (array.get(1).get(size - 1) + array.get(0).get(size - 2)));
        array.get(size - 1).set(0, 0.5f * (array.get(size - 2).get(0) + array.get(size - 1).get(1)));
        array.get(size - 1).set(size - 1, 0.5f * (array.get(size - 2).get(size - 1) + array.get(size - 1).get(size - 2)));
    }

    public void lin_solve(int b, List<List<Float>> array, List<List<Float>> prev_array, float a, float c, int iter) {
        float cRecip = 1.0f / c;
        for (int j = 1; j < size - 1; j++) {
            for (int i = 1; i < size - 1; i++) {
                array.get(i).set(j, (prev_array.get(i).get(j) +
                        a * (array.get(i + 1).get(j) + array.get(i - 1).get(j) +
                                array.get(i).get(j + 1) + array.get(i).get(j - 1))) * cRecip);
            }
        }
        this.set_boundary(b, array);

    }

    public void diffuse(int b, List<List<Float>> array, List<List<Float>> prev_array, float diff, float dt, int iter) {
        float a = dt * diff * (size - 2) * (size - 2);
        this.lin_solve(b, array, prev_array, a, 1 + 6 * a, iter);
    }

    public void project(List<List<Float>> velocity_x, List<List<Float>> velocity_y, List<List<Float>> p, List<List<Float>> div, int iter) {
        for (int j = 1; j < size - 1; j++) {
            for (int i = 1; i < size - 1; i++) {
                div.get(i).set(j, -0.5f * (velocity_x.get(i + 1).get(j) - velocity_x.get(i - 1).get(j) +
                        velocity_y.get(i).get(j + 1) - velocity_y.get(i).get(j - 1)) / size);
                p.get(i).set(j, 0f);
            }
        }
        this.set_boundary(0, div);
        this.set_boundary(0, p);
        lin_solve(0, p, div, 1, 6, iter);

        for (int i = 1; i < size - 1; i++) {
            for (int j = 1; j < size - 1; j++) {
                velocity_x.get(j).set(i, velocity_x.get(j).get(i) - 0.5f * (p.get(j + 1).get(i) - p.get(j - 1).get(i)) * size);
                velocity_y.get(j).set(i, velocity_y.get(j).get(i) - 0.5f * (p.get(j).get(i + 1) - p.get(j).get(i - 1)) * size);
            }
        }
        this.set_boundary(1, velocity_x);
        this.set_boundary(2, velocity_y);
    }

    public void advect(int b, List<List<Float>> d, List<List<Float>> prev_d, List<List<Float>> velocity_x, List<List<Float>> velocity_y, float dt) {
        float i0, i1, j0, j1;

        float dtx = dt * (size - 2);
        float dty = dt * (size - 2);

        float s0, s1, t0, t1;
        float tmp1, tmp2, x, y;

        float Nfloat = size - 2;
        float ifloat, jfloat;
        int i, j;

        for (j = 1, jfloat = 1; j < size - 1; j++, jfloat++) {
            for (i = 1, ifloat = 1; i < size - 1; i++, ifloat++) {
                tmp1 = dtx * velocity_x.get(i).get(j);
                tmp2 = dty * velocity_y.get(i).get(j);
                x = ifloat - tmp1;
                y = jfloat - tmp2;

                if (x < 0.5f) x = 0.5f;
                if (x > Nfloat + 0.5f) x = Nfloat + 0.5f;
                i0 = (float) Math.floor(x);
                i1 = i0 + 1.0f;
                if (y < 0.5f) y = 0.5f;
                if (y > Nfloat + 0.5f) y = Nfloat + 0.5f;
                j0 = (float) Math.floor(y);
                j1 = j0 + 1.0f;

                s1 = x - i0;
                s0 = 1.0f - s1;
                t1 = y - j0;
                t0 = 1.0f - t1;

                int i0i = Math.round(i0);
                int i1i = Math.round(i1);
                int j0i = Math.round(j0);
                int j1i = Math.round(j1);
                if (j1i == 513) {
                    System.out.println("aaa");
                }


                d.get(i).set(j,
                        s0 * (t0 * prev_d.get(i0i).get(j0i) + t1 * prev_d.get(i0i).get(j1i)) +
                                s1 * (t0 * prev_d.get(i1i).get(j0i) + t1 * prev_d.get(i1i).get(j1i)));
            }
        }
        this.set_boundary(b, d);
    }

    public void click(Integer xpos, Integer ypos) {
        switch (this.click_mode) {
            case MAKE_PAINT_SOURCE:
                Pair<Integer, Integer> source = new Pair<>(xpos, ypos);
                this.paint_sources.add(source);
                break;
            case MAKE_FLOW:
                if(this.stored_click==null){
                    this.stored_click= new Pair<>(xpos, ypos);
                }else{
                    //sometimes libgdx registers multiple clicks, this is caught here
                    if(!xpos.equals(this.stored_click.getKey()) &&!ypos.equals(this.stored_click.getValue())) {
                        float xvel = -(float)( this.stored_click.getKey() - xpos) / (size/10f);
                        float yvel = -(float)(this.stored_click.getValue() - ypos) / (size/10f);
                        System.out.println("making flow with yvel:" + yvel + " and xvel:" + xvel);
                        Pair<Float, Float> vel = new Pair<>(xvel, yvel);
                        Flow flow = new Flow(this.stored_click, vel);
                        this.flows.add(flow);
                        this.stored_click = null;
                    }
                }
                break;
        }

    }

    public void setClickMode(ClickMode mode) {
        this.click_mode = mode;
        this.stored_click=null;
    }
}
