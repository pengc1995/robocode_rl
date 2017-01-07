// Author: Haihan Lan 496414100
// EECE592 Coursework 1
// HelloWorld is the entry point to the program

package net1;


import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import org.jfree.ui.RefineryUtilities;


public class HelloWorld 
{

	public static void main(String[] args) throws IOException 
	{
		boolean loadW = false;
		
		int epochs = 3000;
		int inputs = 15;
		int hidden = 8;
		int i=0,j=0;
		
		
		
		double gamma = 0.009;
		double mu = 0.99;
		
		double A = -1;
		double B = 1;
		
		int macroIteration = 1, errorIndex=0;
		
		String fileName = "C:/robocode/t4.csv";
		
		ArrayList<Double> errorsList = new ArrayList<Double>();
		
		
		double m;
		double s;
		
		// train on XOR problem, binary sigmoid, gamma = 0.2
		for(i=0;i<macroIteration; i++)
		{
			
			
			// create new neural net object
		
			NNet net1 = new NNet(inputs,hidden,gamma,mu,A,B, epochs, loadW);
			

			
		    // read in CSV data file
			net1.loadData(fileName);
			
			System.out.println("File " + fileName + " Successfully loaded\n\nBegin training, epochs = " + epochs + "\n\n");
			
			// apply backprop until total error condition is met
			net1.train();
			
			
			System.out.println("training complete...\n\n");
			
			net1.saveWeights();
			
			// output the errors in a plot
			XYLineChart_AWT chart = new XYLineChart_AWT("Plot", "Total Error vs Training epochs", net1.totalerrors, "Epochs", "Total error" );
		     chart.pack( );          
		     RefineryUtilities.centerFrameOnScreen( chart );          
		     chart.setVisible( true );
			
		     
		   
		     
		    String resultfile = "";
		    resultfile = String.format("%d.csv", i);
		    
		    PrintWriter writer = new PrintWriter(resultfile, "UTF-8");
		    writer.println("TSE,epoch");
		    for(j=net1.totalerrors.size()-1;j>=0;j--)
		    {
		    	
		    	
		    	
		    	
		    	
		    	if(java.lang.Math.abs(net1.totalerrors.get(j)) >= 0.05 )
		    	{
		    		
		    		
		    		
		    		errorIndex=j+1;
		    		errorsList.add((double)( errorIndex ));
		    		//errorsList.add((double)( errorIndex / net1.getRows()));
		    		
		    		
		    		
		    		break;
		    	}
		    	
		    }
		    
		    for(j=0;j<epochs;j++)
		    {
		    	writer.format("%f,%d\n", net1.totalerrors.get(j), j);
		    }
	
		    writer.close();
		      
		}
		
		 
		
		for(i=0;i<errorsList.size();i++)
		{
			if(Math.abs(errorsList.get(i)) >= epochs)
			{
				errorsList.remove(i);
				i=0;
				continue;
			}
		}
		
	    m = mean(errorsList);
	    s = std(errorsList);
	    
	    /*for(i=0;i<errorsList.size();i++)
		{
			if(Math.abs(errorsList.get(i) - m) >= s)
			{
				errorsList.remove(i);
				i=0;
				continue;
			}
		}
		
	    
	    m = mean(errorsList);
	    s = std(errorsList);*/
	    
	 
	    
	    
	    System.out.println((double)errorsList.size()/macroIteration * 100.0 + "% converged\n" ); 
	    
	    System.out.format("Macroiteration 1 done: mean epochs until < 0.05 total error: %8.2f epochs, StD: %8.2f epochs min: %8.2f max: %8.2f\n\n", m,s, minVal(errorsList), maxVal(errorsList)); 
	    
		
	
	}
	
	// statistical functions
	public static double mean(ArrayList <Double> in)
	{
		int i; double sum=0;
		for(i=0;i<in.size(); i++)
		{
			sum += in.get(i);
		}
		
		return sum/in.size();
		
	}
	
	public static double var(ArrayList <Double> in)
	{
		double m = mean(in);
		double sum=0;
		
		
		int i;
		for (i=0;i<in.size();i++)
		{
			sum += java.lang.Math.pow(m - in.get(i), 2.0);
		}
		
		return sum/in.size();
		
	}
	
	public static double std(ArrayList <Double> in)
	{
		double v = var(in);
		return java.lang.Math.sqrt(v);
	}

	
	public static double minVal (ArrayList<Double> list) 
	{
		
		double temp = Collections.min(list);
		  return (temp); 
		  
	}
	
	public static double maxVal (ArrayList<Double> list) 
	{
		double temp = Collections.max(list);
		  return (temp); 
	}
}
