package com.example.assignment6;

/*
    Author :    Peter Caylor
    Class  :    CSC 309
    Date   :    10/12/2020
    Purpose:    This contains the parameters for drawing each circle object in the array.

                boolean toDraw
                float   cx, cy, radius
 */

import android.util.Log;

public class CircleParams {

    private boolean draw;
    public boolean  isActiveMole;
    private float   cx;
    private float   cy;
    private float   radius;
    public int      index;

    public CircleParams(boolean draw, float cx, float cy, float radius, int index){
        this.draw         = draw;
        this.cx           = cx;
        this.cy           = cy;
        this.radius       = radius;
        this.index        = index;
        this.isActiveMole = false;
    }

    // Print out the object for testing
    @Override
    public String toString(){
        Log.e("", "CircleParams.toString()\n" +
                             "Index  : " + index + "\n" +
                             "Draw  : " + draw + "\n" +
                             "Active: " + isActiveMole + "\n" +
                             "CX    : " + cx + "\n" +
                             "CY    : " + cy + "\n" +
                             "Radius: " + radius + "\n");
        return "";
    }

    // Getters
    public boolean getDraw(){
        return this.draw;
    }
    public float getCx(){
        return this.cx;
    }
    public float getCy(){
        return this.cy;
    }
    public float getRadius(){
        return this.radius;
    }

    // Setters
    public void setDraw(boolean draw){
        this.draw = draw;
    }
    public void setCx(float cx){
        this.cx = cx;
    }
    public void setCy(float cy){
        this.cy = cy;
    }
    public void setRadius(float radius){
        this.radius = radius;
    }

}
