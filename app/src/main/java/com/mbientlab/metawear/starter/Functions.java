package com.mbientlab.metawear.starter;

import com.mbientlab.metawear.data.CartesianFloat;

/**
 * Created by Jere on 29.3.2017.
 */

public class Functions {
    public static float AccZ_Without_constantG(CartesianFloat Acc3axis){
        float acc_x = Acc3axis.x();
        float acc_y = Acc3axis.y();
        float acc_z = Acc3axis.z();

        if(acc_x < 0) acc_x = -acc_x;
        if(acc_y < 0) acc_y = -acc_y;

        if(acc_z < 0) {
            return acc_z + 1 - acc_x - acc_y;
        }
        else{
            return acc_z - 1 + acc_x + acc_y;
        }
    }
}
