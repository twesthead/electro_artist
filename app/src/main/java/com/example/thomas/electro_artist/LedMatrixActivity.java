package com.example.thomas.electro_artist;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.InputDevice;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.lang.Math;
import java.util.ArrayList;

// J'ai choisi l'option d'un TableLayout. Il faut que j'aille lire le cours sur OpenClassrooms.
// Comment faire référence à une case (View) en particulier dans le tableau ?
// Idées de trucs : avoir deux modes pour dessiner dans la matrice de LED
    // Le mode Synchronisation par Bouton ou le mode Synchronisation automatique à chaque LED
// Faire une classe LED et une classe Matrice de LED ?
// Trouver un moyen de sauvegarder un dessin qu'on a fait pour le recharger plus tard dans la matrice.

public class LedMatrixActivity extends Activity {

    private ArtModel artModel;
    private TableLayout matrixTableLayout;
    private int nbRows;
    private int nbColumns;
    private LedButton currentlyTouchedLed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.artModel = (ArtModel)this.getApplication();
        this.nbRows = this.artModel.NB_ROWS;
        this.nbColumns = this.artModel.NB_COLUMNS;

        setContentView(R.layout.activity_led_matrix);
//        if (this.nbRows>=this.nbColumns)this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        else this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        // Recuperation du TableLayout du XML et generation de la matrice de boutons

        this.matrixTableLayout = (TableLayout)findViewById(R.id.matrix_table_layout);
        this.matrixTableLayout.addView(this.generateMatrixLayout());

        this.matrixTableLayout.addView(this.displayMatrixData());

        }

    @Override
    protected void onResume() {
        setLedMatrixFromData(artModel.getLedMatrixData());
        super.onResume();
    }

    public class mTableLayout extends TableLayout{
        protected mTableLayout(Context context){
            super(context);
        }
        // Vérifie si une coordonnée sur l'écran se situe quelquepart sur le mTableLayout.
        protected boolean isCoordInBoundaries(float x, float y){
            int[] coord = new int[2];
            this.getLocationOnScreen(coord);
            return (x >= coord[0]
                    && x < coord[0] + this.getWidth()
                    && y >= coord[1]
                    && y < coord[1] + this.getHeight());
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent event){
            return true;
        }
        @Override
        public boolean onTouchEvent(MotionEvent event){
            // On récupère le type d'action qui carractérise l'événement :
            int action = event.getAction();
            // Dans coord, on va récupérer les coordonnées du coin en haut à gauche de la matrice.
            // L'origine du repère est le coin en haut à gauche de l'écran.
            int[] coord = new int[2];
            matrixTableLayout.getLocationOnScreen(coord);
            LedButton newTouchedLed = new LedButton(getContext());
            //
            // On vérifie si l'event a bien lieu dans les limites de la matrice.
            // Si oui, alors on récupère le LedButton qui se situe à ces coordonnées.
            if (this.isCoordInBoundaries(event.getX() + coord[0],event.getY() + coord[1])){
                newTouchedLed = getLedAtCoord(event.getX() + coord[0], event.getY() + coord[1]);
            }

            // Ici, on gère les différents types d'événements. On décide qu'une Led ne peut voir
            // son état modifié qu'une fois par mouvement.
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    currentlyTouchedLed=newTouchedLed;
                    currentlyTouchedLed.switchState();
                    currentlyTouchedLed.wasAlreadyTouchedInThisMotion=true;
                    break;
                case MotionEvent.ACTION_MOVE:
                    //if(currentlyTouchedLed!=newTouchedLed){
                    if(!newTouchedLed.wasAlreadyTouchedInThisMotion){
                        currentlyTouchedLed=newTouchedLed;
                        currentlyTouchedLed.switchState();
                        currentlyTouchedLed.wasAlreadyTouchedInThisMotion=true;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    resetAllBooleansAfterMotionEnded();
            }
            // À chaque mouvement, on peut par exemple mettre à jour le TextView avec les données.
            onChangeInLedMatrix();
            return true;
        }
    }

    public static class LedButton extends Button{
        private int row;
        private int col;
        private boolean state;
        private boolean wasAlreadyTouchedInThisMotion;

        protected LedButton(Context context, int row, int col, boolean state){
            super(context);
            this.row=row;
            this.col=col;
            this.state=state;
            this.setColor(state);
        }
        protected LedButton(Context context){
            super(context);

        }
        protected void switchState(){
            this.state=!this.state;
            this.setColor(this.state);
        }

        protected void setState(boolean state){
            this.state = state;
            this.setColor(this.state);
        }

        protected void setColor(boolean state){
            if (state){
                this.setBackgroundResource(R.drawable.btn_default_pressed_holo_dark);
            }
            else{
                this.setBackgroundResource(R.drawable.btn_default_normal_holo_dark);
            }
        }
        public int toInteger(){
            if (state){
                return 1;
            }
            else {
                return 0;
            }
        }
        protected boolean isCoordInBoundaries(float x, float y){
            int[] coord = new int[2];
            this.getLocationOnScreen(coord);
            return x >= coord[0]
                    && x < coord[0] + this.getWidth()
                    && y >= coord[1]
                    && y < coord[1] + this.getHeight();
        }
    }

    private TableLayout generateMatrixLayout(){
        // Le TableLayout qu'on modifie localement.
        mTableLayout tableLayout = new mTableLayout(this);

        // On crée un tableau de lignes
        ArrayList<TableRow> rows;
        rows = new ArrayList(this.nbRows);
        for (int i = 0; i < this.nbRows ; i++) {
            rows.add(i, new TableRow(this));
            for (int j = 0; j < this.nbColumns; j++) {
                //rows.get(i).addView(new LedButton(this,i,j,false),getLedButtonSize(),getLedButtonSize());
                rows.get(i).addView(new LedButton(this,i,j,false));
            }
            tableLayout.addView(rows.get(i));
        }
        tableLayout.setShrinkAllColumns(true);

        return tableLayout;
    }

    private TextView displayMatrixData(){
        TextView textView = new TextView(this);
        textView.setText(this.matrixDataToString());
        return textView;
    }

    private String matrixDataToString(){
        String temp = "";
        for (int i = 0; i < this.nbRows; i++) {
            temp = temp + (this.getLedMatrixData()[i]) + "\n";
        }
        return temp;
    }

    private void updateMatrixDataView(){
        ((TextView)this.matrixTableLayout.getChildAt(1)).setText(this.matrixDataToString());
    }

    private int getLedButtonSize(){
        return (9*55/this.nbColumns);
    }

    private LedButton getLed(int row, int col){
        return  ((LedButton)((TableRow)((TableLayout)this.matrixTableLayout.getChildAt(0)).getChildAt(row)).getVirtualChildAt(col));
    }

    private LedButton getLedAtCoord(float x, float y){
        for (int i = 0; i < this.nbRows; i++) {
            for (int j = 0; j < this.nbColumns; j++) {
                if (this.getLed(i,j).isCoordInBoundaries(x,y)){
                    return this.getLed(i,j);
                }
            }
        }
        return null;
    }

    private void resetAllBooleansAfterMotionEnded(){
        for (int i = 0; i < this.nbRows; i++) {
            for (int j = 0; j < this.nbColumns; j++) {
                this.getLed(i,j).wasAlreadyTouchedInThisMotion=false;
            }
        }
    }

    protected int[] getLedMatrixData(){  // Integrer l'addition en transformant en 100011101
        int[] data = new int[this.nbRows];
        for (int i = 0; i < this.nbRows; i++) {
            data[i]=0;
            for (int j = 0; j < this.nbColumns; j++) {
                data[i]=data[i]+this.getLed(i,j).toInteger()*(int)Math.pow(2,(this.nbColumns-1-j));
            }
        }
        return data;
    }

    protected String getLedMatrixDataString(){
        String out = "";
        for (int i = 0; i < nbRows; i++) {
            for (int j = 0; j < nbColumns; j++) {
                out = out + getLed(i,j).toInteger();
            }
        }
        return out;
    }
    protected byte[] getLedMatrixDataBytes(){
        byte[] out = new byte[nbRows];
        for (int i = 0; i < nbRows; i++) {
            out[i]=(new Integer(getLedMatrixData()[i])).byteValue();
        }
        return out;
    }


    private void setLedMatrixFromData(int[] data){
        String[] binStringData = new String[data.length];
        for (int i = 0; i < this.nbRows; i++) {
            String format = "%"+this.nbRows+"s";
            binStringData[i]= String.format(format, Integer.toBinaryString(data[i])).replace(' ', '0');
            for (int j = 0; j < this.nbColumns; j++) {
                if (binStringData[i].charAt(j)=='1'){
                    this.getLed(i,j).setState(true);
                }
                else{
                    this.getLed(i,j).setState(false);
                }
            }

        }
    }

    public void onClickErase(View v){
        int[] zeros = new int[nbRows];
        for (int i = 0; i < zeros.length; i++) {
            zeros[i]=0;
        }
        setLedMatrixFromData(zeros);
        onChangeInLedMatrix();
    }

    private void onChangeInLedMatrix(){
        updateMatrixDataView();
        artModel.setLedMatrixData(getLedMatrixData());
        Log.i("LedMatrix","data"+getLedMatrixDataString());

        if (artModel.bleToolBox.isConnectedToClientDevice) {
            artModel.bleToolBox.sendLedMatrixDataToClient(getLedMatrixDataBytes());
        }
    }


}
