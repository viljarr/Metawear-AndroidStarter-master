package com.mbientlab.metawear.starter;

/**
 * Created by Petri Kaitajärvi on 15.3.2017.
 */

public class DataBuffer {

    private float[] z_data;
    private int zeroSample = 0;

    public DataBuffer(int size){
        z_data = new float[size];
        zeroSample = 0;
    }

    private void stepZS() {
        if(++zeroSample > z_data.length){
            zeroSample = 0;
        }
    }

    public void addData(float d){
        z_data[zeroSample] = d;
        stepZS();
    }

    public float getSampleAt(int index) {
        if (index > 0)
            return (z_data[zeroSample]);

        int i = zeroSample + index;
        if (i < 0)
            return (z_data[z_data.length + i]);
        else
            return (z_data[i]);
    }

    public float[] getAllData() {
        float[] tmp = new float[z_data.length];

        for (int i = 0; i < z_data.length; i++) {
            tmp[i] = getSampleAt(-i);
        }
    }
}
