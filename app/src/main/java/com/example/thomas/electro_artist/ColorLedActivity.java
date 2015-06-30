package com.example.thomas.electro_artist;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;


public class ColorLedActivity extends Activity {

    private ArtModel artModel;
    private SeekBar colorBar;
    private SeekBar brightnessBar;
    private TextView colorTextView;
    private View colorSampleView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_led);
        this.artModel = (ArtModel)getApplication();

        this.colorTextView = (TextView)findViewById(R.id.color_text_view);
        this.colorSampleView = findViewById(R.id.color_sample);
        this.colorBar = (SeekBar)findViewById(R.id.color_bar);
        this.colorBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateColor();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        this.brightnessBar = (SeekBar)findViewById(R.id.brightness_bar);
        this.brightnessBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateColor();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        this.updateColor();
    }

    @Override
    protected void onPause() {
        artModel.colorProgressBarsValues[0]=colorBar.getProgress();
        artModel.colorProgressBarsValues[1]=brightnessBar.getProgress();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        colorBar.setProgress(artModel.colorProgressBarsValues[0]);
        brightnessBar.setProgress(artModel.colorProgressBarsValues[1]);
        updateColor();
    }

    // x=200 => hsv1=0
    // x=100 => hsv1=1     y=-1/(100) x + 2

    private int getColorFromBar(){
        float[] hsv = new float[3];
        hsv[0]=this.colorBar.getProgress();
        if (this.brightnessBar.getProgress()<=100){
            hsv[1]=1;
            hsv[2]=this.brightnessBar.getProgress()*(float)0.01;
        }
        else if(this.brightnessBar.getProgress()>=100) {
            hsv[1]=2 - this.brightnessBar.getProgress()*(float)0.01;
            hsv[2]=1;
        }
        return Color.HSVToColor(hsv);
    }

    private String toStringRGBColor(int color){
        return "\n R: "+Color.red(color)
                +"\n G: "+Color.green(color)
                +"\n B: "+Color.blue(color);
    }


    private void updateColor(){
        this.colorTextView.setText(this.toStringRGBColor(this.getColorFromBar()));
        this.colorSampleView.setBackgroundColor(this.getColorFromBar());
        artModel.setColorLed(this.getColorFromBar());
        if (artModel.bleToolBox.isConnectedToClientDevice) {
            String data = Integer.toString(getColorFromBar());
            artModel.bleToolBox.sendColorDataToClient(data);
        }
    }



}
