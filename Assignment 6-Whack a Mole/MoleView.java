package com.example.assignment6;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import java.util.Random;

/*
    Author :    Peter Caylor
    Class  :    CSC 309
    Date   :    10/12/2020
    Purpose:    The view class used to draw on the main activity screen
                We will use this class to draw circles on the screen to represent the moles
 */

public class MoleView extends View {
    Paint strokePaint;
    public static int firstRun;
    final int numOfMoles         = 6;
    final int MAX_FAIL_AMOUNT    = 20;
    int backgroundRed            = 100;
    int backgroundGreen          = 100;
    int backgroundBlue           = 255;
    int timerDelay               = 2000;
    int numConcurrentActiveMoles = 2;
    int numCurrentThreads        = 0;
    int circleColor              = Color.WHITE;
    Runnable[] runThreads        = new Runnable[numConcurrentActiveMoles];
    static String END_GAME       = "END_GAME";
    int WhackCount;
    int failCount;
    CircleParams[] objectsToDraw;

    // Two constructors
    public MoleView (Context context) {
        super( context );
        firstRun     = 1;
        WhackCount   = 0;
        failCount    = 0;
        strokePaint  = new Paint();
        strokePaint.setColor( circleColor );
        strokePaint.setAntiAlias( true );
        strokePaint.setStyle( Paint.Style.FILL );
    }
    public MoleView(Context context, AttributeSet attributeset){
        super( context, attributeset );
        firstRun    = 1;
        WhackCount  = 0;
        failCount   = 0;
        strokePaint = new Paint();
        strokePaint.setColor( circleColor );
        strokePaint.setAntiAlias( true );
        strokePaint.setStyle( Paint.Style.FILL );
    }

    @Override
    protected void onDraw( Canvas canvas ){
        // If we have maxed out our fail count we stop the game
        if (failCount >= MAX_FAIL_AMOUNT) {
            resetCounters();
            updateCounters();
            String temp = MoleView.END_GAME;
            Message message = MainActivity.handler.obtainMessage(2, temp);
            message.sendToTarget();
        }

        // Check if it's the first run. If it is, generate the circle's positions. If it's not, create timer threads and draw moles
        switch( firstRun ){
            case 1:
                resetCounters();
                updateCounters();
                Log.e("onDraw", "onDraw() switch case ran case 1");
                objectsToDraw = generateMoles( canvas );
                canvas.drawRGB(backgroundRed, backgroundGreen, backgroundBlue);
                drawMoles(canvas);
                break;
            case 0:
                // Draw circles
                canvas.drawRGB(backgroundRed, backgroundGreen, backgroundBlue);
                createTimerThreads();
                drawMoles(canvas);
                break;
            default:
                break;
        }
    }

    // Generates each circle position and sets whether to draw them.
    protected CircleParams[] generateMoles(Canvas canvas){
        CircleParams[] objectsToDraw = new CircleParams[numOfMoles];
        float width     = canvas.getWidth();
        float height    = canvas.getHeight();
        float leftCol   = width / 3f;               // X position of left column
        float rightCol  = 2f * ( width / 3f );      // X position of right column
        float radius    = ( width / 4f ) / 2f;
        float yPos      = height / 4f;
        float cx;
        float cy        = 0;

        for (int i = 0; i < numOfMoles; i++){
            // Set the width, cx, cy, whether to draw them
            // Set the x position
            if (i < numOfMoles / 2)
                cx = leftCol;
            else
                cx =  rightCol;

            cy = yPos * ((i % 3) + 1);  // Sets the y position based on the number of rows I want
            objectsToDraw[i] = new CircleParams(false, cx, cy, radius, i);
        }
        return objectsToDraw;
    }

    // Draws all the objects that are meant to be drawn
    protected void drawMoles(Canvas canvas ){
        float cx     = 0;
        float cy     = 0;
        float radius = 0;
        boolean draw;
        for (int i = 0; i < numOfMoles; i++){
            draw = objectsToDraw[i].getDraw();

            if( draw )
                circleColor = Color.rgb(255, 65, 65);
            else
                circleColor = Color.WHITE;
            strokePaint.setColor( circleColor );

            cx      = objectsToDraw[i].getCx();
            cy      = objectsToDraw[i].getCy();
            radius  = objectsToDraw[i].getRadius();
            canvas.drawCircle(cx, cy, radius, strokePaint);
        }
    }

    // When we start the game, set 3 timers, associate them with a position on the board
    // Use the timers to reset the mole and make a new one
    protected void createTimerThreads(){
        int createNumOfThreads = runThreads.length - numCurrentThreads;
        for(int i = 0; i < createNumOfThreads; i++){
            //Get a random position
            final int pos = setRandomMoleActive();
            //Log.e("RUNNABLE", "POSITION: " + pos);
            if(numCurrentThreads < 2){
                numCurrentThreads++;
                runThreads[i] = new Runnable() {
                    @Override
                    public void run() {
                        if ( (WhackCount + failCount) % 2 == 0)
                            timerDelay = (int)(timerDelay * 0.98);

                        if( objectsToDraw[pos].getDraw()){
                            failCount++;
                            objectsToDraw[pos].isActiveMole = false;
                            objectsToDraw[pos].setDraw(false);
                        }
                        numCurrentThreads--;
                        invalidate();
                    }
                };
            }
            updateCounters();
            this.postDelayed(runThreads[i], timerDelay);
        }
    }

    // Generates a random mole that has not been activated already, activates it, and returns its position
    protected int setRandomMoleActive(){
        Random rand = new Random();
        CircleParams temp;
        int random;
        do {
            random  = rand.nextInt( objectsToDraw.length );
            temp = objectsToDraw[random];
        } while( temp.isActiveMole );

        final int pos = temp.index;
        objectsToDraw[pos].setDraw(true);
        objectsToDraw[pos].isActiveMole = true;
        return pos;
    }

    // Updates the counters in MainActivity
    protected void updateCounters(){
        if (firstRun == 1)
            return;
        String temp = "Whack Counter: " + WhackCount;
        Message message = MainActivity.handler.obtainMessage(0, temp);
        message.sendToTarget();
        temp = "Miss Counter: " + failCount;
        message = MainActivity.handler.obtainMessage(1, temp);
        message.sendToTarget();
    }

    // Reset counters
    protected void resetCounters(){
        timerDelay        = 2000;
        numCurrentThreads = 0;
        WhackCount        = 0;
        failCount         = 0;
        firstRun          = 1;
    }

    // Gets the X and Y coordinates of a touch down event, and checks if:
    // A: The game is active
    // B: Which mole they pressed on. If the mole is active, good press, mole is not active, bad press
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int X = (int) event.getX();
        int Y = (int) event.getY();
        int eventAction = event.getAction();

        if (eventAction == MotionEvent.ACTION_DOWN) {
            //Log.e("onTouchEvent",  "ACTION_DOWN AT COORDS "+"X: "+X+" Y: "+Y);
            checkIfMoleHit(X, Y);
        }
        return true;
    }

    // When the user presses a location on the screen, see if they pressed a circle
    // If they pressed a circle, see if it is a mole. If it is an active mole, increment the whack count, set a random mole to isActiveMole and setDraw to true, and invalidate()
    protected void checkIfMoleHit(int xCoord, int yCoord){
        if (firstRun == 1)
            return;
        int correctMole = compareMoleXY(xCoord, yCoord);
        if (correctMole == -1) {
            Log.e("MOLECHECK", "No Mole Found for (" + xCoord + "," + yCoord + ")");
            return;
        }
        objectsToDraw[correctMole].toString();
        if( objectsToDraw[correctMole].isActiveMole){
            objectsToDraw[correctMole].isActiveMole = false;
            objectsToDraw[correctMole].setDraw(false);
            WhackCount++;
        }
        else{
            failCount++;
        }
        updateCounters();
        invalidate();
    }

    // Check if both the xCoord and yCoord are within the radius of the circle's x/y
    protected int compareMoleXY(int xCoord, int yCoord){
        float checkXCoord;
        float checkYCoord;
        float radius;
        for(int i = 0; i < objectsToDraw.length; i++){
            checkXCoord = objectsToDraw[i].getCx();
            checkYCoord = objectsToDraw[i].getCy();
            radius      = objectsToDraw[i].getRadius();

            if ( xCoord <= checkXCoord + radius && xCoord >= checkXCoord - radius
              && yCoord <= checkYCoord + radius && yCoord >= checkYCoord - radius ){
                return i;
            }
        }
        return -1;
    }
}
