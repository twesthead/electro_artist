package com.example.thomas.electro_artist;

/**
 * Created by Thomas on 21/03/2015.
 */
public class LedMatrix {
    protected int nbColumns;
    protected int nbLines;
    protected Led[][] matrix;

    public LedMatrix(int nbLines, int nbColumns){
        this.nbLines=nbLines;
        this.nbColumns=nbColumns;
        this.matrix = new Led[nbLines][nbColumns];
        for (int i = 0; i < nbLines; i++) {
            for (int j = 0; j < nbColumns; j++) {
                this.matrix[i][j]= new Led();
            }
        }
    }

    public void put(){
        for (int i = 0; i < nbLines; i++) {
            for (int j = 0; j < nbColumns; j++) {
                System.out.print(this.matrix[i][j]);
            }
            System.out.println();
        }
    }
}
