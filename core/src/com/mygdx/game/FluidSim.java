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
    public int simSize =200;
    public int totalWidth= (int) Math.floor(simSize *1.2);
    SpriteBatch batch;
    Pixmap pixmap;
    Texture fluidTexture;
    Sprite fluidSprite;
    Fluid fluid;

    Pixmap UIpixmap;
    Texture UItexture;
    Sprite UIsprite;

    ShapeRenderer shapeRenderer;

    @Override
    public void create() {
        batch = new SpriteBatch();
        fluid=new Fluid(simSize,0.000005f,0,0.05f);

        // A Pixmap is basically a raw image in memory as repesented by pixels
        pixmap = new Pixmap(simSize, simSize, Pixmap.Format.RGB888);

        //Fill it red
        pixmap.setColor(Color.BLACK);
        pixmap.fill();

        fluidTexture = new Texture(pixmap);

        //It's the textures responsibility now... get rid of the pixmap
        pixmap.dispose();

        fluidSprite = new Sprite(fluidTexture);

        UIpixmap = new Pixmap(totalWidth-simSize, simSize, Pixmap.Format.RGB888);

        UItexture = new Texture(UIpixmap);

        UIsprite=new Sprite(UItexture);

        shapeRenderer=new ShapeRenderer();
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        //basic anti aliasing look at  https://stackoverflow.com/questions/35969253/libgdx-antialiasing
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT | (Gdx.graphics.getBufferFormat().coverageSampling?GL20.GL_COVERAGE_BUFFER_BIT_NV:0));

        fluid.advance();
        fluidToSprite();

        batch.begin();
        fluidSprite.setPosition(0, 0);
        fluidSprite.draw(batch);
        batch.end();
        drawUI();

        handleInput();

    }

    public void handleInput(){
        if(Gdx.input.isButtonPressed(Input.Buttons.LEFT)){
            int x=Gdx.input.getX();
            int y=Gdx.input.getY();
            System.out.println("x"+x+" y"+y);
            //it is an ui click
            if(x>simSize){
                if(y>simSize/2){
                    fluid.setClickMode(ClickMode.MAKE_PAINT_SOURCE);
                }else{
                    fluid.setClickMode(ClickMode.MAKE_FLOW);
                }
            }else{
                fluid.click(x,y);
            }
        }
    }

    public void drawUI(){
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.rect( simSize, simSize-simSize/2, totalWidth-simSize, simSize/2);
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect( simSize, 0, totalWidth-simSize, simSize/2);
        shapeRenderer.end();
    }

    public void fluidToSprite(){
        pixmap = new Pixmap(simSize, simSize, Pixmap.Format.RGBA8888);

        pixmap.setColor(Color.BLACK);
        pixmap.fill();

        pixmap.setColor(Color.WHITE);
        List<List<Float>> array=fluid.density;
        for(int i =0;i<array.size();i++){
            for (int j = 0; j <array.get(0).size() ; j++) {
                int val=Math.round(array.get(i).get(j)*52f);
                if(Math.abs(val)>255)val=255;
                if(val<0) val=Math.abs(val);
                String hex = Integer.toHexString(val);

                if(hex.length()==1) hex="0"+hex;
                hex=hex+hex+hex;
                int color=Integer.parseInt(hex,16);
                pixmap.drawPixel(i,j,color);
            }
        }
        fluidTexture = new Texture(pixmap);

        pixmap.dispose();

        fluidSprite = new Sprite(fluidTexture);

    }

    public void drawUiElements(){
        pixmap = new Pixmap(simSize,(int)(simSize *0.2), Pixmap.Format.RGBA8888);

        pixmap.setColor(Color.BLACK);
        pixmap.fill();

        pixmap.setColor(Color.WHITE);
        List<List<Float>> array=fluid.density;
    }

    @Override
    public void dispose() {
        batch.dispose();
        fluidTexture.dispose();
    }
}
