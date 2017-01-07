package hhl;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

public class stateData 
{
	
		
	public int binnedAbsBearing;
	public int binnedEnemyDistance;


	public int binnedOwnLife;
	public int binnedX;
	public int binnedY;

	public int binnedGunHeat;
	
	public double ownX;
	public double ownY;
	public double ownLife;
	public double gunHeat;
	public double enemyDistance;
	public double absBearing;
	
	public double reward;
	
	
	public void saveData(String argFileName, String argFileName2) throws IOException 
	{
		FileWriter fileWriter = new FileWriter(argFileName, true);

		String s = String.format("%d,%d,%d,%d,%d,%d,",binnedAbsBearing, binnedEnemyDistance, binnedOwnLife,
				binnedX, binnedY, binnedGunHeat);
		fileWriter.write(s);
		
	
	
		
		s = String.format("%d\n",reward);
		fileWriter.write(s);
	
		fileWriter.close();
		
		fileWriter = new FileWriter(argFileName2, true);
		
		s = String.format("%0.4f,%0.4f,%0.4f,%0.4f,%0.4f,%0.4f,",absBearing, enemyDistance, ownLife,
				ownX, ownY, gunHeat);
		fileWriter.write(s);
		
	
	
		
		s = String.format("%d\n",reward);
		fileWriter.write(s);
	
		fileWriter.close();
		
		
	}
	

}
