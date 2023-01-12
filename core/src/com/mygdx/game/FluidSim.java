package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.List;

public class FluidSim extends ApplicationAdapter {
    public int simSize = 1000;
    public int totalWidth = (int) Math.floor(simSize * 1.2);
    private SpriteBatch batch;
    private Pixmap pixmap;
    private Texture fluidTexture;
    private Sprite fluidSprite;
    private Fluid fluid;
    private ShapeRenderer shapeRenderer;

    @Override
    public void create() {
        batch = new SpriteBatch();
        fluid = new Fluid(simSize, 0.00001f, 0, 0.01f);
        shapeRenderer = new ShapeRenderer();
    }

    @Override
    public void render() {
        long begin = System.currentTimeMillis();
        Gdx.gl.glClearColor(0, 0, 0, 1);
        //basic anti aliasing look at  https://stackoverflow.com/questions/35969253/libgdx-antialiasing
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT | (Gdx.graphics.getBufferFormat().coverageSampling ? GL20.GL_COVERAGE_BUFFER_BIT_NV : 0));

        fluid.advance();
        fluidToSprite();

        batch.begin();
        fluidSprite.setPosition(0, 0);
        fluidSprite.draw(batch);
        batch.end();
        drawUI();

        handleInput();
        long end = System.currentTimeMillis();
        System.out.printf("render took %s ms%n", end-begin);

    }

    public void handleInput() {
        //only left click is looked at
        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            int x = Gdx.input.getX();
            int y = Gdx.input.getY();

            //it is an ui click
            if (x > simSize) {
                if (y > simSize / 2) {
                    fluid.setClickMode(ClickMode.MAKE_PAINT_SOURCE);
                } else {
                    fluid.setClickMode(ClickMode.MAKE_FLOW);
                }
            //it is a simulation click
            } else {
                fluid.click(x, y);
            }
        }
    }

    /**
     * draws the ui elements at the right (currently only the red and green bar)
     */
    public void drawUI() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.rect(simSize, simSize - simSize / 2f, totalWidth - simSize, simSize / 2f);
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(simSize, 0, totalWidth - simSize, simSize / 2f);

        shapeRenderer.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        if(fluid.getClick_mode().equals(ClickMode.MAKE_FLOW)){
            shapeRenderer.rect(simSize, simSize - simSize / 2f, totalWidth - simSize, simSize / 2f);
        }else if(fluid.getClick_mode().equals(ClickMode.MAKE_PAINT_SOURCE)){
            shapeRenderer.rect(simSize, 0, totalWidth - simSize, simSize / 2f);
        }
        shapeRenderer.end();
    }

    /**
     * turns the fluid simulation into a sprite, only the density in the simulation is shown
     */
    public void fluidToSprite() {
        pixmap = new Pixmap(simSize, simSize, Pixmap.Format.RGBA8888);

        pixmap.setColor(Color.BLACK);
        pixmap.fill();

        pixmap.setColor(Color.WHITE);
        List<List<Float>> array = fluid.getDensity();
        for (int i = 0; i < array.size(); i++) {
            for (int j = 0; j < array.get(0).size(); j++) {
                int val = Math.round(array.get(i).get(j) * 52f);
                if (Math.abs(val) > 255) val = 255;
                if (val < 0) val = Math.abs(val);
                String hex = Integer.toHexString(val);

                if (hex.length() == 1) hex = "0" + hex;
                hex = hex + hex + hex;
                int color = Integer.parseInt(hex, 16);
                pixmap.drawPixel(i, j, color);
            }
        }
        if (fluid.getStored_click() != null) {
            pixmap.drawPixel(fluid.getStored_click().getKey(), fluid.getStored_click().getValue());
        }
        fluidTexture = new Texture(pixmap);

        pixmap.dispose();

        fluidSprite = new Sprite(fluidTexture);

    }

    @Override
    public void dispose() {
        batch.dispose();
        fluidTexture.dispose();
    }
}
