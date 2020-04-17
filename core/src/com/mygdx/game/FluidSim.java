package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.List;

public class FluidSim extends ApplicationAdapter {
    SpriteBatch batch;
    Pixmap pixmap;
    Texture texture;
    Sprite sprite;
    Fluid fluid;

    @Override
    public void create() {
        batch = new SpriteBatch();
        fluid=new Fluid(256,0.3f,0,0.005f);

        // A Pixmap is basically a raw image in memory as repesented by pixels
        pixmap = new Pixmap(256,256, Pixmap.Format.RGB888);

        //Fill it red
        pixmap.setColor(Color.BLACK);
        pixmap.fill();

//        //Draw two lines forming an X
//        pixmap.setColor(Color.BLACK);
//        pixmap.drawLine(0, 0, pixmap.getWidth()-1, pixmap.getHeight()-1);
//        pixmap.drawLine(0, pixmap.getHeight()-1, pixmap.getWidth()-1, 0);
//
//        //Draw a circle about the middle
//        pixmap.setColor(Color.YELLOW);
//        pixmap.drawCircle(pixmap.getWidth()/2, pixmap.getHeight()/2, pixmap.getHeight()/2 - 1);


        texture = new Texture(pixmap);

        //It's the textures responsibility now... get rid of the pixmap
        pixmap.dispose();

        sprite = new Sprite(texture);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        fluid.addDensity(10,10,1000);
        fluid.addVelocity(10,10,100f,100f);
        fluid.addVelocity(10,100,100f,-150f);
        fluid.advance();
        fluidToSprite();

        batch.begin();
        sprite.setPosition(0, 0);
        sprite.draw(batch);
        batch.end();
    }

    public void fluidToSprite(){
        pixmap = new Pixmap(256,256, Pixmap.Format.RGBA8888);

        pixmap.setColor(Color.BLACK);
        pixmap.fill();

        pixmap.setColor(Color.WHITE);
        List<List<Float>> array=fluid.vel_y;
        for(int i =0;i<array.size();i++){
            for (int j = 0; j <array.get(0).size() ; j++) {
                int val=Math.round(array.get(i).get(j)*256f);
                if(Math.abs(val)>255)val=255;
                if(val<0) val=Math.abs(val);
                String hex = Integer.toHexString(val);
//                if(!hex.equals("100")){
//                    System.out.println(hex);
//                }

                if(hex.length()==1) hex="0"+hex;
                hex=hex+hex+hex;
                int color=Integer.parseInt(hex,16);
                pixmap.drawPixel(i,j,color);
            }
        }
        texture = new Texture(pixmap);

        pixmap.dispose();

        sprite = new Sprite(texture);

    }

    @Override
    public void dispose() {
        batch.dispose();
        texture.dispose();
    }
}
