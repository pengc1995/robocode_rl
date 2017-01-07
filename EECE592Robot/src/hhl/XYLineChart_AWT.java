package hhl;


import java.util.ArrayList;

import java.awt.Color; 
import java.awt.BasicStroke; 
import org.jfree.chart.ChartPanel; 
import org.jfree.chart.JFreeChart; 
import org.jfree.data.xy.XYDataset; 
import org.jfree.data.xy.XYSeries; 
import org.jfree.ui.ApplicationFrame; 
import org.jfree.ui.RefineryUtilities; 
import org.jfree.chart.plot.XYPlot; 
import org.jfree.chart.ChartFactory; 
import org.jfree.chart.plot.PlotOrientation; 
import org.jfree.data.xy.XYSeriesCollection; 
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;

public class XYLineChart_AWT extends ApplicationFrame 
{
   public XYLineChart_AWT( String applicationTitle, String chartTitle,ArrayList <Double> in, String xlabel, String ylabel  )
   {
      super(applicationTitle);
      JFreeChart xylineChart = ChartFactory.createXYLineChart(
         chartTitle ,
         xlabel ,
         ylabel,
         createDataset(in) ,
         PlotOrientation.VERTICAL ,
         true , true , false);
         
      ChartPanel chartPanel = new ChartPanel( xylineChart );
      chartPanel.setPreferredSize( new java.awt.Dimension( 560 , 367 ) );
      final XYPlot plot = xylineChart.getXYPlot( );
      XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer( );
      renderer.setSeriesPaint( 0 , Color.red );
    
      renderer.setSeriesStroke( 0 , new BasicStroke( 0.1f ) );
      plot.setRenderer( renderer ); 
      setContentPane( chartPanel ); 
   }
   
   private XYDataset createDataset(ArrayList <Double> in )
   {
      final XYSeries errors = new XYSeries( "Abs Errors" );          
      
      for(int i=0;i<in.size(); i++)
      {
    	  double val = in.get(i);
    	  errors.add((double)i,java.lang.Math.abs(val));
      }
      
      final XYSeriesCollection dataset = new XYSeriesCollection( );          
      dataset.addSeries( errors );          
     
      return dataset;
   }

  
}