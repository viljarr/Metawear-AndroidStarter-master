package com.mbientlab.metawear.starter;

/**
 * Created by Jere on 21.3.2017.
 */

public class Filter {
    float x_0 = 0, x_1 = 0, x_2 = 0;
    float y_0 = 0, y_1 = 0, y_2 = 0;

    float a1, a2, b0, b1, b2;

    public Filter(float A1, float A2, float B0, float B1, float B2){
        a1 = A1;
        a2 = A2;
        b0 = B0;
        b1 = B1;
        b2 = B2;
    }

    public float filt(float sample){
        x_0 = sample;
        y_0 = b0*x_0 + b1*x_1 + b2*x_2 + a1*y_1 + a2*y_2;

        y_2 = y_1;
        y_1 = y_0;

        x_2 = x_1;
        x_1 = x_0;

        return y_0;
    }
}
