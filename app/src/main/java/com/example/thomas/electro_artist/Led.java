package com.example.thomas.electro_artist;

import android.widget.Button;

/**
 * Created by Thomas on 21/03/2015.
 */
public class Led {
    // Attributs
    public boolean state;
    // Constructeur
    public Led(){
        this.state=false;
    }

    // Methodes
    public void turnOn() {
        this.state = true;
    }
    public void turnOff() {
        this.state = false;
    }

    @Override
    public String toString(){
        if (state) {
            return "1";
        }
        else {
            return "0";
        }
    }
}
