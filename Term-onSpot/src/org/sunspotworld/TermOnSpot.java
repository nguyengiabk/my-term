package org.sunspotworld;

import com.sun.spot.sensorboard.EDemoBoard;
import com.sun.spot.sensorboard.peripheral.ITriColorLED;
import com.sun.spot.peripheral.radio.RadioFactory;
import com.sun.spot.io.j2me.radiogram.*;
import com.sun.spot.sensorboard.io.IScalarInput;
import com.sun.spot.sensorboard.peripheral.IAccelerometer3D;
import com.sun.spot.util.*;

import java.io.*;
import javax.microedition.io.*;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

public class TermOnSpot extends MIDlet {
    private static final int HOST_PORT = 67;
    private ITriColorLED [] leds = EDemoBoard.getInstance().getLEDs();
    IAccelerometer3D  acc = EDemoBoard.getInstance().getAccelerometer();
    IScalarInput lightSensor =  EDemoBoard.getInstance().getLightSensor();
    private EDemoBoard edemo;
    long now = 0L;
    private double ax;
    private double ay;
    private double az;
    private int light;
    private float a0;
    private float a1;
    private float a2;
    private float a3;
    private int exam=0;
    private static final float vref = (float) 3.0;
    private IScalarInput adc[];

    private float getVoltage(int idx) throws IOException {
     return (float) ((vref * ((float) adc[idx].getValue())) / (float)adc[idx].getRange());
    }

    protected void startApp() throws MIDletStateChangeException {
        RadiogramConnection rCon = null;
        Datagram dg = null;
        new BootloaderListener().start();
        long ourAddr = RadioFactory.getRadioPolicyManager().getIEEEAddress();
        System.out.println("Our radio address = " + IEEEAddress.toDottedHex(ourAddr));
        edemo=EDemoBoard.getInstance();
        adc = edemo.getScalarInputs();

        try {
            rCon = (RadiogramConnection) Connector.open("radiogram://broadcast:" + HOST_PORT);
            dg = rCon.newDatagram(200);
        } catch (Exception e) {
            System.err.println("Caught " + e + " in connection initialization.");
            System.exit(1);
        }
        leds[7].setRGB(255, 255, 255);
        leds[7].setOn();
        while (true) {
           try {
                now = System.currentTimeMillis();
                ax = acc.getAccelX();
                ay = acc.getAccelY();
                az = acc.getAccelZ();
                light = lightSensor.getValue();
                a0 = getVoltage(0);
                if(a0>0.2){
                    leds[0].setRGB(100,0,0);
                    leds[0].setOn();
                }
                else{
                    leds[0].setOff();
                }
                a1 = getVoltage(1);
                if(a1>0.2){
                    leds[1].setRGB(100,0,0);
                    leds[1].setOn();
                }
                else{
                    leds[1].setOff();
                }
                a2 = getVoltage(2);
                if(a2>0.2){
                    leds[2].setRGB(100,0,0);
                    leds[2].setOn();
                }
                else{
                    leds[2].setOff();
                }
                a3 = getVoltage(3);
                if(a3>0.2){
                    leds[3].setRGB(100,0,0);
                    leds[3].setOn();
                }
                else{
                    leds[3].setOff();
                }

                System.out.println(ax+" "+ay+" "+az+" "+light+" "+a0+" "+a1+" "+a2+" "+a3 );
                if(check(a0,a1,a2,a3)==true){
                    exam =1;
                    dg.reset();
                    dg.writeLong(now);
                    dg.writeDouble(ax);
                    dg.writeDouble(ay);
                    dg.writeDouble(az);
                    dg.writeInt(light);
                    dg.writeFloat(a0);
                    dg.writeFloat(a1);
                    dg.writeFloat(a2);
                    dg.writeFloat(a3);
                    rCon.send(dg);
               }else if(exam ==1){
                    exam = 0;
                    for(int i =0; i<5;i++){
                        Utils.sleep(100);
                        dg.reset();
                        dg.writeLong(now);
                        dg.writeDouble(ax);
                        dg.writeDouble(ay);
                        dg.writeDouble(az);
                        dg.writeInt(light);
                        dg.writeFloat(a0);
                        dg.writeFloat(a1);
                        dg.writeFloat(a2);
                        dg.writeFloat(a3);
                        rCon.send(dg);
                    }
               }
                //leds[7].setOff();
                Utils.sleep(20);
            } catch (Exception e) {
                System.err.println("Caught " + e + " while collecting/sending sensor sample.");
            }
        }
    }

     private boolean check(float a0, float a1, float a2, float a3){
        int i=0;
        if(a0>0.2) i++;
        if(a1>0.2) i++;
        if(a2>0.2) i++;
        if(a3>0.2) i++;
        if(i>=2) return true;
        else return false;
    }

    protected void pauseApp() {
        }


    protected void destroyApp(boolean unconditional) throws MIDletStateChangeException {
        for (int i = 0; i < 8; i++) {
            leds[i].setOff();
        }
    }
}
