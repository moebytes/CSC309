package com.example.assignment6;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/*
    Author :    Peter Caylor
    Class  :    CSC 309
    Date   :    10/12/2020
    Purpose:    Make a whack-a-mole game with an async timer
 */

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create button event listener
        startButton = findViewById( R.id.start_game );
        startButton.setOnClickListener( new View.OnClickListener() {
            public void onClick(View v) {
                View view = findViewById(R.id.MoleView);
                whack_counter = findViewById(R.id.whack_counter);
                miss_counter  = findViewById(R.id.missCounter);
                startButton.setEnabled( false );
                MoleView.firstRun = 0;
                view.invalidate();
            }
        });
    }

    static TextView whack_counter;
    static TextView miss_counter;
    static Button startButton;
    @SuppressLint("HandlerLeak")
    // Receives messages from MoleView for updating the Whack/Miss counter, and re-enabling the button when the game is done
    public static Handler handler = new Handler() {
        public void handleMessage( Message message ) {
            super.handleMessage( message );
            if (whack_counter == null || miss_counter == null  || startButton == null)
                return;
            String new_text;
            // Set whack count
            if ( message.what == MsgEnum.msg_whack_counter.ordinal() ) {
                new_text = message.obj.toString();
                whack_counter.setText(new_text);
            }
            // Set miss count
            if ( message.what == MsgEnum.msg_miss_counter.ordinal() ) {
                new_text = message.obj.toString();
                miss_counter.setText(new_text);
            }
            // End game, make the start game button visible
            if ( message.what == MsgEnum.msg_end_game.ordinal() ) {
                new_text = message.obj.toString();
                if (new_text.equals(MoleView.END_GAME)){
                    startButton.setEnabled(true);
                }
            }
        }
    };

}

