package com.mbientlab.metawear.starter;

/**
 * Created by Jere on 4.5.2017.
 */

public class BPM {

    int counter = 0;
    float maxim = 0;
    int index = 0;
    float thr;
    int win;
    int Bpm = -1;
    int buffSize;
    int n = -1;

    DataBuffer Buff;

    public BPM(float Thr, int Window, int buffSize){
        this.thr = Thr;
        this.win = Window;
        this.buffSize = buffSize;

        this.Buff = new DataBuffer(buffSize);
    }

    public int GetBpm (float z) {
        Bpm = -1;
        n++;
        if (counter > 0) {
            if (z > maxim && z > thr) {
                maxim = z;
                index = n;
                counter = win;
            } else {
                counter--;
            }
        }
        else{
            if(maxim>0){
                Buff.addData((float)n);
                Bpm = (buffSize-1)/((int)(Buff.getSampleAt(0)-Buff.getSampleAt(-buffSize+1))*6000);
            }
            else{
                maxim = 0;
                counter = win;
            }
        }
        return Bpm;
    }

}
