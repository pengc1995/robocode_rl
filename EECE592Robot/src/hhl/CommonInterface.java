package hhl;

import java.io.File;
import java.io.FileNotFoundException;

public interface CommonInterface
{

	public double outputFor(double [] X);
	public void train();
	public void save(String argFileName);
	public void load(String argFileName) throws FileNotFoundException;
	
	
	
}
