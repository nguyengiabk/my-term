package org.sunspotworld;

import java.awt.BasicStroke;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class RecordArray {
    private Record[] recordArray;
    private int top;
    private String username;
    private int count;
    private int maxNum=15;
    private int maxArrayNum =15;
    public RecordArray(){
        this.recordArray = new Record[maxNum];
        this.top = 0;
    }

    public void addRecord(Record record){
        if(record.getDataIndex()==1) top=0;
        this.recordArray[top++] = record;
    }

    public String inference(Connection dCon){
           try {
                Statement s = dCon.createStatement();
                String query = "truncate temp";
                s.executeUpdate(query);
                for(int i=0;i<maxNum;i++){
                    int j=i+1;
                    query = "insert into temp (dataIndex,ax,ay,az,light,a0,a1,a2,a3) values ("+j+","+this.recordArray[i].getAx()+","+this.recordArray[i].getAy()+","
                                +this.recordArray[i].getAz()+","+this.recordArray[i].getLight()+","+this.recordArray[i].getA0()+","
                                +this.recordArray[i].getA1()+","+this.recordArray[i].getA2()+","+this.recordArray[i].getA3()+")";
                    s.executeUpdate(query);
                }
                String user=null;
                String inferenceUsername=null;
                double min=100.0;
                query = "select user, avg((t1.ax-t2.ax)*(t1.ax-t2.ax)) + avg((t1.ay-t2.ay)*(t1.ay-t2.ay)) + 2*avg((t1.az -t2.az)*(t1.az-t2.az)) + "
                        + "avg((t1.light - t2.light)*(t1.light - t2.light)) +5*(avg((t1.a0-t2.a0)*(t1.a0-t2.a0)) + avg((t1.a1-t2.a1)*(t1.a1-t2.a1)) + "
                        + "avg((t1.a2-t2.a2)*(t1.a2-t2.a2)) + avg((t1.a3-t2.a3)*(t1.a3-t2.a3))) as result"
                        + " from "
                        + "(select temp.dataIndex as dataIndex,ax/max as ax, ay/may as ay, az/maz as az, light/mlight as light, a0/ma0 as a0, a1/ma1 as a1, a2/ma2 as a2, a3/ma3 as a3 "
                        + "from "
                        + "(select max(ax) as max, max(ay) as may, max(az) as maz, max(light) as mlight, max(a0) as ma0, max(a1) as ma1, max(a2) as ma2, max(a3) as ma3 "
                        + "from data) as m, temp) as t1,"
                        + "(select d.user as user, d.dataIndex as dataIndex, d.ax/max as ax, d.ay/may as ay, d.az/maz as az, d.light/mlight as light, d.a0/ma0 as a0, d.a1/ma1 as a1, d.a2/ma2 as a2, d.a3/ma3 as a3"
                        + " from data as d,"
                        + " (select max(ax) as max, max(ay) as may, max(az) as maz, max(light) as mlight, max(a0) as ma0, max(a1) as ma1, max(a2) as ma2, max(a3) as ma3 from data) as m) as t2"
                        + " where t1.dataIndex = t2.dataIndex group by user";
                s.executeQuery(query);
                ResultSet rs = s.getResultSet();
                while(rs.next()){
                    user=rs.getString("user");
                    double result = rs.getDouble("result");
                    System.out.println(user+" "+result);
                    if(result<min) {
                        inferenceUsername = user;
                     //   System.out.println(username);
                        min=result;
                    }
               }
               s.close();
               //System.out.println(username);
               return inferenceUsername;
           } catch (SQLException ex) {
                Logger.getLogger(RecordArray.class.getName()).log(Level.SEVERE, null, ex);
            }
        return null;
    }

    public void setUsername(String username){
        this.username = username;
    }
    
    public void putOnDatabase(Connection dCon){
        int maxCount=0;
        try {
                    Statement s = dCon.createStatement ();
                    String query = "select max(count) from data where user = '"+this.username+"'";
                    s.executeQuery (query);
                    ResultSet rs = s.getResultSet();
                    while (rs.next ()){
                           maxCount  =  rs.getInt ("max(count)");
                            //System.out.println(count);
                    }
                    s.close ();
                 } catch (Exception e) {
                        System.out.println(e);
                        return ;
                 }
        if(maxCount<maxArrayNum){
            this.count = maxCount+1;
            for(int i=0; i<maxNum; i++){
                this.recordArray[i].setUser(this.username, this.count);
                while(this.recordArray[i].putOnDatabase(dCon)==false);
                this.top=0;
             }
        }
        else{
        try{
            Statement s = dCon.createStatement();
            String query = "";
            for(int i=1;i<maxArrayNum;i++){
                int j=i+1;
                query="update data as d,(select * from data where count = "+j+" and user = '"+this.username+"') as t "
                        + "set "
                        + "d.ax = t.ax, "
                        + "d.ay=t.ay,"
                        + " d.az=t.az,"
                        + " d.light = t.light,"
                        + "d.a0=t.a0,"
                        + "d.a1=t.a1,"
                        + "d.a2=t.a2,"
                        + "d.a3=t.a3"
                        + " where d.user='"+this.username+"' and d.count = "+i+"  and d.dataIndex = t.dataIndex";
                System.out.println(query);
                s.executeUpdate(query);
            }
            query = "update data as d, temp as t "
                    + "set"
                    + " d.ax = t.ax, "
                    + "d.ay = t.ay, "
                    + "d.az = t.az,"
                    + " d.light = t.light, "
                    + "d.a0 = t.a0, "
                    + "d.a1 = t.a1,"
                    + " d.a2 = t.a2,"
                    + " d.a3 = t.a3"
                    + " where d.user = '"+this.username+"' and count = "+maxArrayNum +" and d.dataIndex = t.dataIndex";
            System.out.println(query);
            s.executeUpdate(query);
        } catch(Exception e){
            System.out.println(e);
        }
        }
    }
    
    public void drawPressureChart(){
        XYSeries a0Series = new XYSeries("a0");
        XYSeries a1Series = new XYSeries("a1");
        XYSeries a2Series = new XYSeries("a2");
        XYSeries a3Series = new XYSeries("a3");
        int i;
        for(i=0;i<maxNum;i++){
            a0Series.add(i,this.recordArray[i].getA0());
            a1Series.add(i,this.recordArray[i].getA1());
            a2Series.add(i,this.recordArray[i].getA2());
            a3Series.add(i,this.recordArray[i].getA3());
        }
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(a0Series);
        dataset.addSeries(a1Series);
        dataset.addSeries(a2Series);
        dataset.addSeries(a3Series);
        JFreeChart chart = ChartFactory.createXYLineChart("Pressure Chart", "Index", "Value", dataset, PlotOrientation.VERTICAL, true, true, false );
        chart.getXYPlot().getRenderer().setSeriesStroke(0 , new BasicStroke(3.f));
        chart.getXYPlot().getRenderer().setSeriesStroke(1 , new BasicStroke(3.f));
        chart.getXYPlot().getRenderer().setSeriesStroke(2 , new BasicStroke(3.f));
        chart.getXYPlot().getRenderer().setSeriesStroke(3 , new BasicStroke(3.f));
        try {
            String path = "/home/nguyengia/NetBeansProjects/Term-onDesktop/Image/Pressure/"+this.username+"pressure"+this.count+".jpg";
            ChartUtilities.saveChartAsJPEG(new File(path), chart, 800,500);
        } catch (IOException e) {
            System.err.println(e);
        }
    }
    
    public void drawAccChart(){
        XYSeries axSeries = new XYSeries("ax");
        XYSeries aySeries = new XYSeries("ay");
        XYSeries azSeries = new XYSeries("az");
        int i;
        for(i=0;i<maxNum;i++){
            axSeries.add(i,this.recordArray[i].getAx());
            aySeries.add(i,this.recordArray[i].getAy());
            azSeries.add(i,this.recordArray[i].getAz());
        }
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(axSeries);
        dataset.addSeries(aySeries);
        dataset.addSeries(azSeries);
        JFreeChart chart = ChartFactory.createXYLineChart("Acceleration Chart", "Index", "Value", dataset, PlotOrientation.VERTICAL, true, true, false );
        chart.getXYPlot().getRenderer().setSeriesStroke(0 , new BasicStroke(3.f));
        chart.getXYPlot().getRenderer().setSeriesStroke(1 , new BasicStroke(3.f));
        chart.getXYPlot().getRenderer().setSeriesStroke(2 , new BasicStroke(3.f));
        try {
            String path = "/home/nguyengia/NetBeansProjects/Term-onDesktop/Image/Acc/"+this.username+"Acc"+this.count+".jpg";
            ChartUtilities.saveChartAsJPEG(new File(path), chart, 800,500);
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    public void drawLightChart(){
        XYSeries lightSeries = new XYSeries("light");
        int i;
        for(i=0;i<maxNum;i++){
            lightSeries.add(i,this.recordArray[i].getLight());
        }
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(lightSeries);
        JFreeChart chart = ChartFactory.createXYLineChart("Light Chart", "Index", "Value", dataset, PlotOrientation.VERTICAL, true, true, false );
        chart.getXYPlot().getRenderer().setSeriesStroke(0 , new BasicStroke(3.f));
       try {
            String path = "/home/nguyengia/NetBeansProjects/Term-onDesktop/Image/Light/"+this.username+"Light"+this.count+".jpg";
            ChartUtilities.saveChartAsJPEG(new File(path), chart, 800,500);
        } catch (IOException e) {
            System.err.println(e);
        }
    }
}

