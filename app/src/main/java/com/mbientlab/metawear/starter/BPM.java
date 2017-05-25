package com.mbientlab.metawear.starter;
import android.util.Log;
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
    int limit =0;
    int maxBpm=0;
    int minBpm=0;


    DataBuffer Buff;

    public BPM(float Thr, int Window, int buffSize){
        this.thr = Thr;
        this.counter=Window;
        this.win = Window;
        this.buffSize = buffSize;

        this.Buff = new DataBuffer(buffSize);
    }

    public int GetBpm (float z) {
        Bpm = -1;
        n++;
        z = z * z;
        Log.i("tutorial BPM sample", "" + z);
        if (counter > 0) {
            if ((z > maxim) && (z > thr)) {
                maxim = z;
                index = n;
                counter = win;
                limit++;

            } else {
                counter--;
            }
        } else {
            if (maxim > 0) {
                Buff.addData((float) index);
                if (limit > buffSize) {
                    Bpm = (buffSize - 1) * 6000 / ((int) (Buff.getSampleAt(0) - Buff.getSampleAt(-buffSize + 1)));
                }


                if (Bpm > maxBpm) {
                    maxBpm = Bpm;
                } else if ((Bpm < minBpm || minBpm == 0) && Bpm > 40) {
                    minBpm = Bpm;
                }

            }

            maxim = 0;
            counter = win;
        }
        if (limit > buffSize) {
            if ((n - Buff.getSampleAt(-buffSize + 1)) > (20 * 100)) {
                Bpm = 0;
            }
        }

        return Bpm;
    }

    public void clear(){
        minBpm=0;
        maxBpm=0;
    }

    public int GetMinBpm(){
        return minBpm;
    }

    public int GetMaxBpm(){
        return maxBpm;
    }
}
