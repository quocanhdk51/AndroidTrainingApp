package com.example.s3727634.afinal;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;




public class MainActivity extends AppCompatActivity {

    private final static String TAG = "MainActivity";

    private int clickCounter= 0;
    private int maxclickOnChar;
    private String[] keys ;
    private String textAnswer;
    private final int level = 0;
    public static final String PREF = "mypref";
    public static final String LIFE = "life";
    public static final String HINT = "hint";
    public static final String HIGH_SCORE = "high_score";
    public static final String CURRENT_SCORE = "cscore";
    //public static final String LEVEL = "level";
    private TextView textScreen, textLife, textHint;
    private Button button;
    SharedPreferences sharedPreferences;
    private ImageView imageView;

    private DatabaseHelper mDatabaseHelper;

    //Pedometer declarations
    TextView mStepCounter;
    int steps;
    private BroadcastReceiver mStepsReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            steps = intent.getIntExtra("Steps", steps);
            mStepCounter.setText(Integer.toString(steps));
            setTextHint();
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDatabaseHelper = new DatabaseHelper(this);

        //Map functionality
        Button mapBtn = findViewById(R.id.mapBtn);
        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mapIntent = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(mapIntent);
            }
        });

        //Database functionality
        Button btnHistory = (Button)findViewById(R.id.historyBtn);
        btnHistory.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
                startActivity(intent);
            }
        });

        //Initialize step counter
        mStepCounter = findViewById(R.id.step_counter_value);
        mStepCounter.setText(Integer.toString(mDatabaseHelper.getStep()));
        steps = mDatabaseHelper.getStep();
        Intent mStepServiceIntent = new Intent(MainActivity.this, StepsDetectorService.class);
        startService(mStepServiceIntent);

        textLife = (TextView)findViewById(R.id.life);
        textHint = (TextView)findViewById(R.id.hint);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isFirstRun = sharedPreferences.getBoolean("FIRSTRUN", true);
        if(isFirstRun){
            ResetLives();
            ResetHints();
            SetCurrentScore();
            DefaultScore();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("FIRSTRUN",false);
            editor.commit();
        }

        //getCurrentLevel();
        setTextHint();
        setTextLife();
        imageView = (ImageView)findViewById(R.id.photos);
        textAnswer = Anagram.randomWord();
        setImage(textAnswer.toLowerCase());
        maxclickOnChar = textAnswer.length();
        keys = Anagram.shuffleWord(textAnswer);
        for (String key : keys) {
            addView(((LinearLayout) findViewById(R.id.layoutParent)), key, ((EditText) findViewById(R.id.editText)));
        }
        button = (Button)findViewById(R.id.useHint);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int currentHints = getCurrentHints();
                if(currentHints > 0){
                    Toast toast = Toast.makeText(getApplicationContext(),
                            textAnswer,Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 400);
                    toast.show();
                    minusHints();

                }else{
                    Toast toast = Toast.makeText(getApplicationContext(),
                           "You have No Hints Now.",Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 400);
                    toast.show();
                }
                setTextHint();
            }
        });



    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(mStepsReceiver, new IntentFilter("StepsUpdates"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(MainActivity.this).unregisterReceiver(mStepsReceiver);
    }

    private void addView(LinearLayout viewParent, final String text, final EditText editText) {
        LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        linearLayoutParams.rightMargin = 30;
        final TextView textView = new TextView(this);
        textView.setLayoutParams(linearLayoutParams);
        textView.setGravity(Gravity.CENTER);
        textView.setText(text);
        textView.setClickable(true);
        textView.setFocusable(true);
        textView.setTextSize(32);

        textScreen = (TextView) findViewById(R.id.textScreen);

        textView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(clickCounter < maxclickOnChar) {
                    if (clickCounter== 0){
                        editText.setText("");
                    }

                    editText.setText(editText.getText().toString() + text);
                    textView.setClickable(false);
                    textView.setTextSize(20);
                    textView.setTextColor(Color.RED);
                    clickCounter++;

                    if (clickCounter == maxclickOnChar) {
                        checkAnswer();
                    }
                }
            }
        });


        viewParent.addView(textView);


    }


    private void checkAnswer() {
        clickCounter = 0;
        EditText editText = findViewById(R.id.editText);
        LinearLayout linearLayout = findViewById(R.id.layoutParent);
        if(editText.getText().toString().equals(textAnswer)) {
            textAnswer = Anagram.randomWord();
            maxclickOnChar = textAnswer.length();
            keys = Anagram.shuffleWord(textAnswer);
            setImage(textAnswer.toLowerCase());
            updateCurrentScore();
            Toast.makeText(MainActivity.this, "Correct", Toast.LENGTH_SHORT).show();



            editText.setText("");





        } else {
            int currentLives = getCurrentLives();
            if(currentLives > 1) {
                Toast.makeText(MainActivity.this, "Wrong", Toast.LENGTH_SHORT).show();
                updateHighScore();
                minusLives();
            }else{
                updateHighScore();

                ResetLives();
                textAnswer = Anagram.randomWord();
                maxclickOnChar = textAnswer.length();
                keys = Anagram.shuffleWord(textAnswer);
                setImage(textAnswer.toLowerCase());
                Intent gameOver = new Intent(MainActivity.this,GameOver.class);
                startActivity(gameOver);
            }
            editText.setText("");
        }
        setTextLife();
        linearLayout.removeAllViews();
        for (String key : keys) {
            addView(linearLayout, key, editText);
        }

    }

    private void setImage(String imageName){
        Resources res = getResources();
        //String imageName = "apple";
        int resID = res.getIdentifier(imageName, "drawable", getPackageName());
        Drawable drawable = res.getDrawable(resID );
        imageView.setImageDrawable(drawable );
    }





    private void ResetLives(){
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(LIFE, 3);
        editor.apply();
    }

    private int getCurrentLives(){
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int value = sharedPreferences.getInt(LIFE,0);
        return value;
    }

    private void minusLives(){
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int value = sharedPreferences.getInt(LIFE,0);
        value = value - 1;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(LIFE, value);
        editor.apply();

    }

    private void ResetHints(){
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(HINT, 3);
        editor.apply();
        //Toast.makeText(MainActivity.this,"Reset Life",Toast.LENGTH_LONG).show();
    }

    private void minusHints(){
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int value = sharedPreferences.getInt(HINT,0);
        value = value - 1;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(HINT, value);
        editor.apply();

    }

    private int getCurrentHints(){
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int value = sharedPreferences.getInt(HINT,0);
        return value;
    }

    private void setTextLife(){
        int i = getCurrentLives();
        String s = "Life : " + String.valueOf(i);
        textLife.setText(s);

    }

    private void setTextHint(){
        int i = getCurrentHints();
        String s = "Hint : " + String.valueOf(i);
        textHint.setText(s);

    }

    private void DefaultScore(){
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(HIGH_SCORE, 0);
        editor.apply();
    }

    private void SetCurrentScore(){
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(CURRENT_SCORE, 0);
        editor.apply();
    }

    private void updateCurrentScore(){
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int value = sharedPreferences.getInt(CURRENT_SCORE,0);
        value = value + 1;
        int words = mDatabaseHelper.getWords() + 1;
        mDatabaseHelper.setWords(Integer.toString(words));
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(CURRENT_SCORE, value);
        editor.apply();
    }

    private void updateHighScore(){
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int high = sharedPreferences.getInt(HIGH_SCORE,0);
        int current = sharedPreferences.getInt(CURRENT_SCORE,0);
        if(current - high > 0){
            int s0= current;
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(HIGH_SCORE, s0);
            editor.apply();


        }
    }





}