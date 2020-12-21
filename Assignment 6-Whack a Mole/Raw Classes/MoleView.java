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

import java.util.Calendar;
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
    final int NUM_CONCURRENT_ACTIVE_MOLES = 2;
    final int NUM_OF_MOLES                = 6;
    final int MAX_FAIL_AMOUNT             = 20;
    int backgroundRed                     = 100;
    int backgroundGreen                   = 100;
    int backgroundBlue                    = 255;
    int timerDelay                        = 2000;
    int circleColor                       = Color.WHITE;
    static String END_GAME                = "END_GAME";
    public static int firstRun;
    boolean isMoleDrawTimerRunning;
    boolean isTimerDecThreadRunning;
    CircleParams[] objectsToDraw;
    int[] lastMole;
    int INITIAL_TIMER;
    int WhackCount;
    int failCount;

    // Two constructors
    public MoleView (Context context) {
        super( context );
        firstRun      = 1;
        WhackCount    = 0;
        failCount     = 0;
        lastMole      = new int[NUM_CONCURRENT_ACTIVE_MOLES];
        INITIAL_TIMER = timerDelay;
        strokePaint   = new Paint();
        strokePaint.setColor( circleColor );
        strokePaint.setAntiAlias( true );
        strokePaint.setStyle( Paint.Style.FILL );
    }
    public MoleView(Context context, AttributeSet attributeset){
        super( context, attributeset );
        firstRun      = 1;
        WhackCount    = 0;
        failCount     = 0;
        lastMole      = new int[NUM_CONCURRENT_ACTIVE_MOLES];
        INITIAL_TIMER = timerDelay;
        strokePaint   = new Paint();
        strokePaint.setColor( circleColor );
        strokePaint.setAntiAlias( true );
        strokePaint.setStyle( Paint.Style.FILL );
    }

    @Override
    protected void onDraw( Canvas canvas ){
        // If we have maxed out our fail count we stop the game
            if (failCount >= MAX_FAIL_AMOUNT) {
                String temp           = MoleView.END_GAME;
                Message message       = MainActivity.handler.obtainMessage( MsgEnum.msg_end_game.ordinal(), temp );
                message.sendToTarget();
                firstRun = 1;
            }

            // Check if it's the first run. If it is, generate the circle's positions. If it's not, create timer threads and draw moles
            switch( firstRun ){
                case 1:
                    resetCounters();
                    Log.e("onDraw", "onDraw() switch case ran case 1");
                    objectsToDraw = generateMoles( canvas );
                    canvas.drawRGB(backgroundRed, backgroundGreen, backgroundBlue);
                    drawMoles(canvas);
                    break;
                case 0:
                    // Draw circles
                    canvas.drawRGB(backgroundRed, backgroundGreen, backgroundBlue);
                    updateCounters();
                    drawActiveMolesTimerThread();
                    drawMoles(canvas);
                    break;
                default:
                    break;
        }
    }

    // Generates each circle position and sets whether to draw them.
    protected CircleParams[] generateMoles(Canvas canvas){
        CircleParams[] objectsToDraw = new CircleParams[NUM_OF_MOLES];
        float width    = canvas.getWidth();
        float height   = canvas.getHeight();
        float leftCol  = width / 3f;               // X position of left column
        float rightCol = 2f * ( width / 3f );      // X position of right column
        float radius   = ( width / 4f ) / 2f;
        float yPos     = height / 4f;
        float cy;
        float cx;

        for (int i = 0; i < NUM_OF_MOLES; i++){
            // Set the width, cx, cy, whether to draw them
            // Set the x position
            if (i < NUM_OF_MOLES / 2)
                cx = leftCol;
            else
                cx =  rightCol;

            cy = yPos * ((i % 3) + 1);  // Sets the y position based on the number of rows I want
            objectsToDraw[i] = new CircleParams(false, cx, cy, radius, i);
        }
        return objectsToDraw;
    }

    // Draws all the objects that are meant to be drawn
    protected void drawMoles( Canvas canvas ){
        boolean draw;
        float radius;
        float cx;
        float cy;
        for (int i = 0; i < NUM_OF_MOLES; i++){
            draw = objectsToDraw[i].getDraw();
            if( draw )
                circleColor = Color.rgb(255, 65, 65);
            else
                circleColor = Color.WHITE;
            strokePaint.setColor( circleColor );
            cx     = objectsToDraw[i].getCx();
            cy     = objectsToDraw[i].getCy();
            radius = objectsToDraw[i].getRadius();
            canvas.drawCircle(cx, cy, radius, strokePaint);
        }
    }

    // Timer Thread to draw new moles
    protected void drawActiveMolesTimerThread(){
        // Check if there is already a thread running
        if ( !isMoleDrawTimerRunning ){
            // Create a number of mole timer threads according to the data member numConcurrentActiveMoles
            isMoleDrawTimerRunning = true;
            for (int i = 0; i < NUM_CONCURRENT_ACTIVE_MOLES; i++){
                final int pos = setRandomMoleActive();
                lastMole[i]   = pos;
            }
            this.postDelayed( startMoleTimer(), timerDelay );
        }
        if ( !isTimerDecThreadRunning ){
            isTimerDecThreadRunning = true;
            this.postDelayed( decTimerVal(), INITIAL_TIMER );
        }
        invalidate();
    }

    // Starts a timer to indicate when to reset the moles
    private Runnable startMoleTimer(){
        return new Runnable(){
            @Override
            public void run(){
                resetDrawAndInc();
                isMoleDrawTimerRunning = false;
                invalidate();
            }
        };
    }

    private Runnable decTimerVal(){
        return new Runnable(){
            @Override
            public void run(){
                setTimerDelay(timerDelay);
                isTimerDecThreadRunning = false;
                Log.e("", "TIMER: " + timerDelay);
            }
        };
    }

    // Reset active moles and increment counters
    private void resetDrawAndInc(){
        for (int i = 0; i < NUM_OF_MOLES; i++){
            if (objectsToDraw[i].getDraw()){
                failCount++;
                objectsToDraw[i].isActiveMole = false;
                objectsToDraw[i].setDraw(false);
            }
        }
        updateCounters();
        invalidate();
    }

    // Updates the timer delay so that it does not scale exponentially
    protected void setTimerDelay(int curDelay){
        if(firstRun == 1)
            return;
        if ( curDelay > 1000 )
            timerDelay -= 100;
        else if ( curDelay > 800 )
            timerDelay -= 20;
        else if ( curDelay > 600 )
            timerDelay -= 10;
        else if ( curDelay > 400 ){
            timerDelay -= 5;
        }
    }

    // Generates a random mole that has not been activated already, activates it, and returns its position
    protected int setRandomMoleActive(){
        Random rand = new Random();
        CircleParams temp;
        // Keep selecting until we get one that wasn't a previous mole, nor is already active
        do {
            int random = rand.nextInt( objectsToDraw.length );
            temp = objectsToDraw[random];
        } while( temp.isActiveMole || isLastChosenMole( temp.index ) );

        final int pos = temp.index;
        objectsToDraw[pos].setDraw(true);
        objectsToDraw[pos].isActiveMole = true;
        return pos;
    }

    // Checks if it is one of the previously active moles
    protected boolean isLastChosenMole(int index){
        for(int i = 0; i < lastMole.length; i++) {
            if ( index == lastMole[i] )
                return true;
        }
        return false;
    }

    // Updates the counters in MainActivity
    protected void updateCounters(){
        String temp     = "Whack Counter: " + WhackCount;
        Message message = MainActivity.handler.obtainMessage( MsgEnum.msg_whack_counter.ordinal(), temp );
        message.sendToTarget();

        temp    = "Miss Counter: " + failCount;
        message = MainActivity.handler.obtainMessage( MsgEnum.msg_miss_counter.ordinal(), temp );
        message.sendToTarget();
    }

    // Reset counters
    protected void resetCounters(){
        timerDelay = 2000;
        WhackCount = 0;
        failCount  = 0;
    }

    // Gets the X and Y coordinates of a touch down event, and checks if:
    // A: The game is active
    // B: Which mole they pressed on. If the mole is active, good press, mole is not active, bad press
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int X = (int) event.getX();
        int Y = (int) event.getY();
        int eventAction = event.getAction();
        if (eventAction == MotionEvent.ACTION_DOWN)
            checkIfMoleHit(X, Y);
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
