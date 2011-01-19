package org.sunspotworld;
import java.sql.*;

public class Record {
    private int recordIndex;
    private int sunspot;
    private int dataIndex;
    private double ax;
    private double ay;
    private double az;
    private int light;
    private float a0;
    private float a1;
    private float a2;
    private float a3;
    private String user;
    private int count =0;

    Record(int sunspot, int dataIndex, double ax, double ay, double az, int light, float a0, float a1, float a2, float a3){
        this.sunspot = sunspot;
        this.dataIndex = dataIndex;
        this.ax = ax;
        this.ay = ay;
        this.az = az;
        this.light = light;
        this.a0 = a0;
        this.a1 = a1;
        this.a2 = a2;
        this.a3 = a3;
    }
    public void setUser(String user, int count){
        this.user = user;
        this.count = count;
    }
    public boolean putOnDatabase(Connection dCon){
        if (dCon != null) {
                try {
                    Statement s = dCon.createStatement ();
                    String query = "select max(recordIndex) from data";
                    s.executeQuery (query);
                    ResultSet rs = s.getResultSet();
                    while (rs.next ())
                    {
                        recordIndex =  rs.getInt ("max(recordIndex)");
                        //System.out.println(recordIndex);
                    }
                     recordIndex++;
                    query = "INSERT INTO data (recordIndex, sunspot, dataIndex, ax, ay, az, light, a0, a1, a2, a3, user, count) VALUES ("
                            +this.recordIndex+","+this.sunspot+","+this.dataIndex+","+this.ax+","+this.ay+","+this.az+","
                            +this.light+","+this.a0+","+this.a1+","+this.a2+","+this.a3+",'"+this.user+"',"+this.count+")";
                    //System.out.println(query);
                    s.executeUpdate(query);
                    s.close ();
                    //System.out.println (" rows were inserted");
                    return true;
                } catch (Exception e) {
                    System.out.println(e);
                return false;
            }
        }
    return false;
    }

    public double getAx(){
        return this.ax;
    }

    public double getAy(){
        return this.ay;
    }

    public double getAz(){
        return this.az;
    }

    public int getLight(){
        return this.light;
    }

    public float getA0(){
        return this.a0;
    }
    
    public float getA1(){
        return this.a1;
    }
    
    public float getA2(){
        return this.a2;
    }
    
    public float getA3(){
        return this.a3;
    }

    public String getUser(){
        return this.user;
    }

    public int getCount(){
        return this.count;
    }

    public int getDataIndex(){
        return this.dataIndex;
    }
}
