package com.mbientlab.metawear.starter;

/**
 * Created by Petri Kaitajärvi on 15.3.2017.
 */

public class DataBuffer {

    private float[] z_data;
    private int wP = 0, rP = 0;

    public DataBuffer(int size){
        z_data = new float[size];
        wP = 0;
        rP = 0;
    }

    public void stepW() {
        if(++wP > z_data.length){
            wP = 0;
        }
    }

    public void stepR(){
        if(++rP > z_data.length){
            rP = 0;
        }
    }

    public int len(){
        if(wP == rP) return 0;

        if(wP > rP) return (wP-rP);

        else
            return (z_data.length+wP-rP);
    }

    public void addData(float d){
        z_data[wP] = d;
        stepW();;
    }

    public float[] getAllData(){
        float[] tmp = new float[z_data.length];

        for(int i = rP++; ; )
    }
}
