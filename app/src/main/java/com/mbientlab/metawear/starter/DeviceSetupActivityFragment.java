/*
 * Copyright 2015 MbientLab Inc. All rights reserved.
 *
 * IMPORTANT: Your use of this Software is limited to those specific rights
 * granted under the terms of a software license agreement between the user who
 * downloaded the software, his/her employer (which must be your employer) and
 * MbientLab Inc, (the "License").  You may not use this Software unless you
 * agree to abide by the terms of the License which can be found at
 * www.mbientlab.com/terms . The License limits your use, and you acknowledge,
 * that the  Software may not be modified, copied or distributed and can be used
 * solely and exclusively in conjunction with a MbientLab Inc, product.  Other
 * than for the foregoing purpose, you may not use, reproduce, copy, prepare
 * derivative works of, modify, distribute, perform, display or sell this
 * Software and/or its documentation for any purpose.
 *
 * YOU FURTHER ACKNOWLEDGE AND AGREE THAT THE SOFTWARE AND DOCUMENTATION ARE
 * PROVIDED "AS IS" WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED,
 * INCLUDING WITHOUT LIMITATION, ANY WARRANTY OF MERCHANTABILITY, TITLE,
 * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT SHALL
 * MBIENTLAB OR ITS LICENSORS BE LIABLE OR OBLIGATED UNDER CONTRACT, NEGLIGENCE,
 * STRICT LIABILITY, CONTRIBUTION, BREACH OF WARRANTY, OR OTHER LEGAL EQUITABLE
 * THEORY ANY DIRECT OR INDIRECT DAMAGES OR EXPENSES INCLUDING BUT NOT LIMITED
 * TO ANY INCIDENTAL, SPECIAL, INDIRECT, PUNITIVE OR CONSEQUENTIAL DAMAGES, LOST
 * PROFITS OR LOST DATA, COST OF PROCUREMENT OF SUBSTITUTE GOODS, TECHNOLOGY,
 * SERVICES, OR ANY CLAIMS BY THIRD PARTIES (INCLUDING BUT NOT LIMITED TO ANY
 * DEFENSE THEREOF), OR OTHER SIMILAR COSTS.
 *
 * Should you have any questions regarding your right to use this Software,
 * contact MbientLab Inc, at www.mbientlab.com.
 */

package com.mbientlab.metawear.starter;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.IBinder;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mbientlab.metawear.AsyncOperation;
import com.mbientlab.metawear.Message;
import com.mbientlab.metawear.MetaWearBleService;
import com.mbientlab.metawear.MetaWearBoard;
import com.mbientlab.metawear.RouteManager;
import com.mbientlab.metawear.UnsupportedModuleException;
import com.mbientlab.metawear.data.CartesianFloat;
import com.mbientlab.metawear.module.Accelerometer;

/**
 * A placeholder fragment containing a simple view.
 */
public class DeviceSetupActivityFragment extends Fragment implements ServiceConnection {
    public interface FragmentSettings {
        BluetoothDevice getBtDevice();
    }

    private MetaWearBoard mwBoard= null;
    private FragmentSettings settings;
    private Accelerometer accModule;
    private TextView texts;
    private TextView bpmText, bpmMaxText, bpmMinText;
    private ImageView graph;
    private Handler dataHandler;
    private DataBuffer DB;

    private Bitmap bmp;
    private Canvas canvas;
    public Paint paint, p;

    //private Filter LpFilter;
    private Filter HpFilter;
    private BPM myBPM;

    private int bpmVal = 0;

    private int len;
    private int drawFreq;
    private ImageView image_start,image_stop;

    public DeviceSetupActivityFragment() {
    }

    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Activity owner= getActivity();
        if (!(owner instanceof FragmentSettings)) {
            throw new ClassCastException("Owning activity must implement the FragmentSettings interface");
        }

        settings= (FragmentSettings) owner;
        owner.getApplicationContext().bindService(new Intent(owner, MetaWearBleService.class), this, Context.BIND_AUTO_CREATE);


        len=0;

        DB = new DataBuffer(500);
        drawFreq=0;
        // set paint for graph
        paint = new Paint();
        paint.setColor(Color.rgb(0,0,0));
        paint.setStrokeWidth(3.0f);
        paint.setStyle(Paint.Style.STROKE);

        // set paint for grid
        p = new Paint();
        p.setColor(Color.rgb(2, 72, 14));
        p.setStrokeWidth(5.0f);
        p.setStyle(Paint.Style.STROKE);

        //LpFilter = new Filter(-1.1430f, -0.4128f, 0.6389f, 1.2779f, 0.6389f);
        HpFilter = new Filter(-0.369527f, 0.195816f, 0.391335f, -0.78267f, 0.391335f);
        myBPM = new BPM(0.00016f, 50, 11);


        //Init bitmap
        bmp = Bitmap.createBitmap(1000, 600, Bitmap.Config.ARGB_8888);
        int dpi = getResources().getDisplayMetrics().densityDpi;
        bmp.setDensity(dpi);


        // Canvas
        canvas = new Canvas(bmp);
        canvas.drawColor(getResources().getColor(R.color.bg));




        canvas.drawLine(0, 300, 1000,300, paint);



        dataHandler = new Handler(){
            @Override
            public void handleMessage(android.os.Message msg) {

                bpmText.setText(""+bpmVal);
                bpmMinText.setText(""+myBPM.GetMinBpm());
                bpmMaxText.setText(""+myBPM.GetMaxBpm());
                Log.i("tutorial test", ""+bpmVal);
                graph.setMinimumWidth(graph.getWidth());
                graph.setMinimumHeight(graph.getHeight());
                graph.setImageBitmap(bmp);
            }
        };
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        ///< Unbind the service when the activity is destroyed
        getActivity().getApplicationContext().unbindService(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setRetainInstance(true);
        return inflater.inflate(R.layout.fragment_device_setup, container, false);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mwBoard= ((MetaWearBleService.LocalBinder) service).getMetaWearBoard(settings.getBtDevice());
        ready();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }

    /**
     * Called when the app has reconnected to the board
     */
    public void reconnected() { }

    /**
     * Called when the mwBoard field is ready to be used
     */
    public void ready() {

        try {
            accModule = mwBoard.getModule(Accelerometer.class);
            // Set the output data rate to 25Hz or closet valid value
            accModule.setOutputDataRate(100.f);
        } catch (UnsupportedModuleException e) {
            Snackbar.make(getActivity().findViewById(R.id.device_setup_fragment), e.getMessage(),
                    Snackbar.LENGTH_SHORT).show();
        }
    }
                @Override
                public void onViewCreated(View view, Bundle savedInstanceState) {
                    super.onViewCreated(view, savedInstanceState);
                    texts = (TextView) view.findViewById(R.id.acc_text);
                    bpmText = (TextView) view.findViewById(R.id.textView2);
                    bpmMinText = (TextView) view.findViewById(R.id.textView5);
                    bpmMaxText = (TextView) view.findViewById(R.id.textView7);
                    graph = (ImageView) view.findViewById(R.id.imgGraph);
                    graph.setImageBitmap(bmp);
                    image_start=(ImageView) view.findViewById(R.id.acc_start);
                    image_stop=(ImageView) view.findViewById(R.id.acc_stop);

                    view.findViewById(R.id.acc_start).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            image_stop.setImageResource(R.drawable.ic_action_stop_enabled);
                            image_start.setImageResource(R.drawable.ic_action_start_disabled);
                            accModule.routeData().fromAxes().stream("acc_stream").commit()
                                    .onComplete(new AsyncOperation.CompletionHandler<RouteManager>() {
                                        @Override
                                        public void success(RouteManager result) {
                                            result.subscribe("acc_stream", new RouteManager.MessageHandler() {
                                                @Override
                                                public void process(Message msg) {
                                                    android.os.Message m = dataHandler.obtainMessage();

                                                    float preSample=msg.getData(CartesianFloat.class).z();

                                                    float[] z_data = DB.getAllData();


                                                    float sample = HpFilter.filt(preSample);
                                                    Log.i("tutorial sample",Float.toString(sample));
                                                    DB.addData(sample*10000);

                                                    int temp = myBPM.GetBpm(sample);
                                                    if(temp != -1) {
                                                        bpmVal = temp;
                                                    }


                                                    if (drawFreq>=4){
                                                        // Bitmap
                                                        bmp = Bitmap.createBitmap(1000, 600, Bitmap.Config.ARGB_8888);
                                                        int dpi = getResources().getDisplayMetrics().densityDpi;
                                                        bmp.setDensity(dpi);


                                                        // Canvas
                                                        canvas = new Canvas(bmp);
                                                        canvas.drawColor(getResources().getColor(R.color.bg));



                                                        //float mean= sum/z_data.length;
                                                        for (int i = 0; (i + 1) < z_data.length; i++) {
                                                            canvas.drawLine(i * 2, (float) (300 + z_data[i]), (i + 1) * 2, (float) (300 + z_data[i + 1]), paint);
                                                        }



                                                        dataHandler.sendEmptyMessage(0);
                                                        drawFreq=0;
                                                    }else{
                                                        ++drawFreq;
                                                    }


                                                }
                                            });

                                            accModule.enableAxisSampling();
                                            accModule.start();
                                        }
                                    });
                        }
                    });
                    view.findViewById(R.id.acc_stop).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            image_stop.setImageResource(R.drawable.ic_action_stop_disabled);
                            image_start.setImageResource(R.drawable.ic_action_start_enabled);
                            accModule.stop();
                            accModule.disableAxisSampling();
                            mwBoard.removeRoutes();
                        }
                    });
                    view.findViewById(R.id.refresh).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            myBPM.clear();
                        }
                    });
                }

}
