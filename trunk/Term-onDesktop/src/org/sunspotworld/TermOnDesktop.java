package org.sunspotworld;

import com.sun.spot.io.j2me.radiogram.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.DateFormat;
import javax.microedition.io.*;
import java.sql.*;
import javax.swing.JOptionPane;



public class TermOnDesktop {

    private static final int HOST_PORT = 67;
    RadiogramConnection rCon;
    Datagram dg;
    DateFormat fmt = DateFormat.getTimeInstance();
    java.sql.Connection dCon = null;
    int dataIndex = 0;
    int maxNum =15;
    RecordArray recordArray;

    public void run() throws Exception {
      try {
            rCon = (RadiogramConnection) Connector.open("radiogram://:" + HOST_PORT);
            dg = rCon.newDatagram(rCon.getMaximumLength());
        } catch (Exception e) {
             System.err.println("setUp caught " + e.getMessage());
             return;
        }

      try {
            String userName = "nguyengia";
            String password = "manutd";
            String url = "jdbc:mysql://localhost/TERM";
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            dCon = DriverManager.getConnection(url, userName, password);
            System.out.println("Database connection established");
        } catch (Exception e) {
            System.err.println(e + "Cannot connect to database server");
            return;
        }
       
        recordArray = new RecordArray();

       while (true) {
           FileWriter log = new FileWriter("log/log.txt",true);
            try {
                rCon.receive(dg);
                int sunspot=0;
                String addr = dg.getAddress();
                if(addr.equals("0014.4F01.0000.66B2")){
                    sunspot = 1;
                }else if(addr.equals("0014.4F01.0000.5955")){
                    sunspot = 2;
                }
                if(sunspot!=0){
                    long now = dg.readLong();
                    double ax =dg.readDouble();
                    double ay =dg.readDouble();
                    double az =dg.readDouble();
                    int light =dg.readInt();
                    float a0 = dg.readFloat();
                    float a1 = dg.readFloat();
                    float a2 = dg.readFloat();
                    float a3 = dg.readFloat();
                    if((check(a0,a1,a2,a3)==true)&&(dataIndex<maxNum)){
                        dataIndex ++;
                        recordArray.addRecord(new Record(sunspot, dataIndex, ax, ay, az, light, a0, a1, a2, a3));
                   }
                    else if (check(a0,a1,a2,a3)==false){
                        dataIndex = 0;
                    }
                    else if(dataIndex==maxNum){
                        String username =null;
                        BufferedWriter out = new BufferedWriter(log);
                        if((username=recordArray.inference(dCon))!=null){
                        int confirm = JOptionPane.showConfirmDialog(null, "Are you "+username+"?" );
                        if(confirm != 0){
                                 username = (String) JOptionPane.showInputDialog("What you're name?",null);
                                 out.write(username+" false\n");
                            }
                         else{
                                out.write(username+" true\n");
                         }
                         }
                        else {
                                    username = (String) JOptionPane.showInputDialog("What you're name?",null);
                                    out.write(username+" no result \n");
                            }
                        if(username!=null){
                        recordArray.setUsername(username);
                        recordArray.putOnDatabase(dCon);
                        recordArray.drawPressureChart();
                        recordArray.drawAccChart();
                        recordArray.drawLightChart();
                        }
                        dataIndex++;
                        out.close();
                    }
                }
            } catch (Exception e) {
                System.err.println("Caught " + e +  " while reading sensor data.");
                throw e;
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

    
    public static void main(String[] args) throws Exception {
        TermOnDesktop app = new TermOnDesktop();
        app.run();
    }
}
