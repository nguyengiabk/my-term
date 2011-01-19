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
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

public class RecordArray {
    private Record[] recordArray;
    private int top;
    private String username;
    private int count;
    private int maxNum=10;
    public RecordArray(){
        this.recordArray = new Record[10];
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
                String user="";
                String username="";
                double  acc, light, pressure, result;
                double min=100.0;
                query = "select t1.user,t1.ad/t3.aad as acc,t1.ld/t3.ald as light ,t1.pd/t3.apd as pressure, t1.ad/t3.aad*3 + t1.ld/t3.ald + t1.pd/t3.apd*5 as result from "
                        + "(select d.user, avg(sqrt((t.ax-d.ax)*(t.ax-d.ax)+(t.ay-d.ay)*(t.ay-d.ay)+(t.az-d.az)*(t.az-d.az))) as ad,"
                        + "avg(sqrt(t.light-d.light)*(t.light-d.light)) as ld,"
                        + "avg(sqrt((t.a0-d.a0)*(t.a0-d.a0)+(t.a1-d.a1)*(t.a1-d.a1)+(t.a2-d.a2)*(t.a2-d.a2)+(t.a3-d.a3)*(t.a3-d.a3))) as pd"
                        + " from data as d, temp as t where d.dataIndex=t.dataIndex group by user) as t1,"
                        + " (select avg(ad) as aad, avg(ld) as ald, avg(pd) as apd from "
                        + "(select d.user, avg(sqrt((t.ax-d.ax)*(t.ax-d.ax)+(t.ay-d.ay)*(t.ay-d.ay)+(t.az-d.az)*(t.az-d.az))) as ad,"
                        + "avg(sqrt(t.light-d.light)*(t.light-d.light)) as ld,"
                        + "avg(sqrt((t.a0-d.a0)*(t.a0-d.a0)+(t.a1-d.a1)*(t.a1-d.a1)+(t.a2-d.a2)*(t.a2-d.a2)+(t.a3-d.a3)*(t.a3-d.a3))) as pd "
                        + "from data as d, temp as t where d.dataIndex=t.dataIndex group by user) as t2) as t3";
                s.executeQuery(query);
                ResultSet rs = s.getResultSet();
                while(rs.next()){
                    user=rs.getString("user");
                    acc = rs.getDouble("acc");
                    light = rs.getDouble("light");
                    pressure = rs.getDouble("pressure");
                    result = rs.getDouble("result");
                    System.out.println(user+" "+acc+" "+light+" "+pressure+" "+ result);
                    if(result<min) {
                        username = user;
                     //   System.out.println(username);
                        min=result;
                    }
               }
               s.close();
               //System.out.println(username);
               return username;
           } catch (SQLException ex) {
                Logger.getLogger(RecordArray.class.getName()).log(Level.SEVERE, null, ex);
            }
        return null;
    }

    public void setUsername(String username){
        this.username = username;
    }
    
    public void putOnDatabase(Connection dCon){
         try {
                    Statement s = dCon.createStatement ();
                    String query = "select max(count) from data where user = '"+this.username+"'";
                    s.executeQuery (query);
                    ResultSet rs = s.getResultSet();
                    while (rs.next ()){
                            count  =  rs.getInt ("max(count)");
                            //System.out.println(count);
                    }
                    s.close ();
                 } catch (Exception e) {
                        System.out.println(e);
                        return ;
                 }
        this.count = count+1;
        for(int i=0; i<maxNum; i++){
        this.recordArray[i].setUser(this.username, this.count);
        while(this.recordArray[i].putOnDatabase(dCon)==false);
        this.top=0;
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


     private boolean averageCompare(Record[] a, Record[] b){
        double averageAx;
        double averageAy;
        double averageAz;
        int        averageLight;
        float     averageA0;
        float     averageA1;
        float     averageA2;
        float     averageA3;

        return false;
    }
}

