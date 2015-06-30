package com.example.thomas.electro_artist;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((ArtModel)getApplication()).bleToolBox = new BleToolBox(this);
//        this.artModel = (ArtModel)this.getApplication();
//        this.artModel.bleToolBox = new BleToolBox(this);
        setContentView(R.layout.activity_main);

    }

    public void onClickToMatrix(View v){
        Intent intent = new Intent(this,LedMatrixActivity.class);
        startActivity(intent);
    }

    public void onClickToBle(View v){
        Intent intent = new Intent(this,BleActivity.class);
        startActivity(intent);
    }

    public void onClickToColorLed(View v){
        Intent intent = new Intent(this,ColorLedActivity.class);
        startActivity(intent);
    }
    public void onClickToTestBench(View v){
        Intent intent = new Intent(this,TestBenchActivity.class);
        startActivity(intent);
    }

}
