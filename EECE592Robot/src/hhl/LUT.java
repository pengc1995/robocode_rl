package hhl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

import robocode.RobocodeFileWriter;

public class LUT implements LUTInterface 
{
	
	

	public ArrayList<ArrayList<Double>> QTable;
	
	
	int dimStates;
	int numActions;
	
	int policy;
	double quantization;
	boolean construct = false;
	
	
	@Override
	public double outputFor(double[] X) 
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void train()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void save(String argFileName) 
	{
		try
		{
			FileWriter fileWriter = new FileWriter(argFileName, false);
			
			for(int i=0;i<QTable.size(); i++)
			{
				for(int j=0;j<QTable.get(i).size(); j++)
				{
					String s = String.format("%f,",QTable.get(i).get(j));
					fileWriter.write(s);
				}
				
				fileWriter.write("\n");
			}
			fileWriter.close();
		}
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void save2(String argFileName) 
	{
		try
		{
			FileWriter fileWriter = new FileWriter(argFileName, false);
			
			for(int i=0;i<QTable.size(); i++)
			{
				for(int j=0;j<QTable.get(i).size(); j++)
				{
					String s = String.format("%f,",QTable.get(i).get(j));
					fileWriter.write(s);
				}
				
				fileWriter.write("\n");
			}
			fileWriter.close();
		}
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	@Override
	public void load(String argFileName) throws FileNotFoundException 
	{
		FileReader file = new FileReader(argFileName);
	       
        Scanner rowScanner = new Scanner(file);
        Scanner colScanner;

        String line = null;
        
        int rows = 0;
        int cols = 0;
        
        while (rowScanner.hasNextLine()) 
        {

          line = rowScanner.nextLine();

          colScanner = new Scanner(line).useDelimiter(",");
          cols = 0;
          
          while (colScanner.hasNext()) 
          {
            double val =  Float.parseFloat(colScanner.next());
            QTable.get(rows).set(cols, val);
            
            cols++;
           
          } 
          colScanner.close();
          rows++;
        }
        
        rowScanner.close();
		
        try 
        {
			file.close();
		}
        catch (IOException e) 
        {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public void saveTraining(String argFileName) 
	{
		
	}

	@Override
	public void initializeLUT() 
	{
		Random r = new Random();
		int i=0,j=0;
	
		QTable = new ArrayList<ArrayList<Double>>();
		
		// add rows to QTable (rows are states)
		for(i = 0; i < dimStates; i++) 
		{
			QTable.add(new ArrayList<Double>());
		}
		
		for(i=0;i<dimStates;i++)
		{
			for(j=0;j<numActions;j++)
			{
				QTable.get(i).add(0.0);
			}
		}

	}

	// hash the state into a unique integer
	@Override
	public int indexFor(double[] X) 
	{
		// TODO Auto-generated method stub
		return 0;
	}
	
	// quantize float in range [0,1] to integer in range
	public int quantize(double in, double min, double max, double quantization)
	{
		double temp = (in - min) / (max - min);
	
		return (int) (temp * quantization);
	
	}
	
	public double unQuantize(double in, double min, double max, double quantization)
	{
		double temp = (in - min) / (max - min);
	
		return (temp * quantization);
	
	}
	
	public int quantizeDist(double in)
	{
		int ret = 0;
		if(in >= 150)
		{
			ret = 2;
		}
		else if(in>= 100)
		{
			ret = 1;
		}
		else
		{
			ret = 0;
		}
		
		return ret;
	}
	
	public int decideAction(int[] state)
	{
		return 0;
	}
	
	LUT(int _dimStates, int _numActions, boolean loadFile, String _file)
	{
		construct = true;
		dimStates = _dimStates;
		numActions = _numActions;
		initializeLUT();
		if(loadFile == true)
		{
			try 
			{
				load(_file);
			} 
			catch (FileNotFoundException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	}
	
	

}
