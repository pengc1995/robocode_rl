package hhl;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Data 
{
	
	public int thisRoundNum;
	public double roundCulmR;
	public int winLose;
	public int culmWinLose;
	
	
	
	
	public void saveRewards(String f) throws IOException
	{
		FileWriter fileWriter = new FileWriter(f, true);

		String s = String.format("%d,",thisRoundNum);
		fileWriter.write(s);
		
		s = String.format("%f,",roundCulmR);
		fileWriter.write(s);
		
		s = String.format("%d,",winLose);
		fileWriter.write(s);
		
		s = String.format("%d,\n",culmWinLose);
		fileWriter.write(s);
	
		fileWriter.close();
	}
	
	Data()
	{
		thisRoundNum=0;
		roundCulmR = 0;
		winLose = 0;
		culmWinLose = 0;
	}
	
	public void saveWins(String f, double wins, int roundNum) throws IOException
	{
		FileWriter fileWriter = new FileWriter(f, true);

		String s = String.format("%d,%f\n",roundNum, wins);
		fileWriter.write(s);
		
	
		fileWriter.close();
	}
	

}
