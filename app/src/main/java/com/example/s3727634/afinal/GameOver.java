package com.example.s3727634.afinal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class GameOver extends AppCompatActivity {
    private Button button;
    private TextView highscore,yourscore;
    SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_over);
        button = (Button)findViewById(R.id.addHint);
        highscore = (TextView)findViewById(R.id.highscore);
        yourscore = (TextView)findViewById(R.id.yourscore);

        String yourScore = "" + setYourScore();
        String highScore = "" + setHighScore();
        highscore.setText(highScore);
        yourscore.setText(yourScore);
        restartCurrentScore();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent gameOver = new Intent(GameOver.this,MainActivity.class);
                startActivity(gameOver);
            }
        });
    }

    private String setHighScore(){
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int high =   sharedPreferences.getInt(MainActivity.HIGH_SCORE,0);
        String highScore = String.valueOf(high);
        return highScore;
    }

    private String setYourScore(){
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int current =   sharedPreferences.getInt(MainActivity.CURRENT_SCORE,0);
        String currentScore = String.valueOf(current);
        return  currentScore;
    }

    private void  restartCurrentScore(){
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(MainActivity.CURRENT_SCORE, 0);
        editor.apply();
    }
}
