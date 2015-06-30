package com.example.thomas.electro_artist;

import android.app.Application;
import android.content.Context;

/**
 * Created by Thomas on 21/03/2015.
 */
public class ArtModel extends Application {

    protected final int NB_ROWS = 8;
    protected final int NB_COLUMNS = 8;
    private int[] ledMatrixData;
    private int colorLed;
    protected BleToolBox bleToolBox;

    protected int[] colorProgressBarsValues;

    public ArtModel(){
        super();
        ledMatrixData = new int[NB_ROWS];
        colorProgressBarsValues = new int[2];
        setLedMatrixDataToZero();
        this.bleToolBox = null;
    }


    private void setLedMatrixDataToZero(){
        for (int i = 0; i < NB_ROWS; i++) {
            ledMatrixData[i]=0;
        }
    }
    protected void setLedMatrixData(int[] data){
        for (int i = 0; i < NB_ROWS; i++) {
            ledMatrixData[i]=data[i];
        }
    }

    protected int[] getLedMatrixData() {
        return ledMatrixData;
    }

    protected void setColorLed(int color){
        this.colorLed = color;
    }
    protected BleToolBox getBleToolBox() {
        return bleToolBox;
    }
}
