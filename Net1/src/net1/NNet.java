package net1;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.NumberFormatException;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.Random;
import java.util.Scanner;
import java.util.ArrayList;


// implements a modified NeuralNetInterface
public class NNet implements NeuralNetInterface
{
	
	// members of NNet class 
	public int numInputs, numHidden, outDim, epochs;
	public double learningRate, momentumTerm, A, B;
	
	public int dataRows, dataCols;
	
	// dynamic array of Node objects, which are neurons
	public ArrayList<Node> net = new ArrayList<Node>();
	
	// file data
	public ArrayList<ArrayList<Double>> fileData;
	
	public ArrayList<Double> errors, totalerrors;
	
	// method overrides
	@Override
	public double outputFor(double[] X)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void train()
	{	
		// train network using online back propagation
		// 
		ArrayList<Double> inputs = new ArrayList<Double>();
		int i=0,j=0,k=0,l=0,m=0;
		double out, target, gradient;
		
		for(j=0;j<dataCols - 1; j++)
		{
			inputs.add(0.0);
		}
		
		// iterate for a predefined number of epochs
		for( k = 0; k<epochs; k++)
		{
		
			// row iterator
			for(i=0;i<dataRows; i++)
			{
				// column iterator
				for(j=0;j<dataCols - 1; j++)
				{
					inputs.set(j,fileData.get(i).get(j));
				}
				
				// feedforward the input signals
				feedForward(inputs);
				
				// get output of output neuron
				out = net.get(numInputs + numHidden).computeOutput();
				if(Double.isNaN(out)) throw new NumberFormatException("Nan encountered");
				
				// get target value for backprop
				target = fileData.get(i).get(dataCols-1);
				
				// compute error
				double temp = target - out;
				
				errors.add(temp);
				
			
				//System.out.format("Row: %d, target: %10.8f error: %10.8f\n", i,target, temp);
				//System.out.println("Epoch: " + k + " inputs: " + inputs.get(0) + " " + inputs.get(1) + " output: " + out  + " Target: " + target + " Error: " + errors.get(k *dataRows +i));
				
				double sigmoidDerivative = 0, sigmoidDerivativeHidden = 0;
				double hiddenOutput,inputOutput, outputWeight, delta_k=0, delta_j=0;
				
				
				
				/********************************this is where the magic happens**********************************************************/
			
				
				
				
				// calculate gradient term for output neuron weights
				// dE/dW_jk = delta_k * u_k; where delta = (out - target) * g'_k(z_k), where z_k is the input to the output neurons
				// and u_k is the output of the kth hidden neuron
				
				// since g(x) = (b-a)/(1 + e^-x) + a, g'(x) = e^x * (b-a) / (e^x + 1)^2
				sigmoidDerivative =  net.get(numInputs + numHidden).sigmoidDerivativeOutput();
				
				for(l=0;l<numHidden;l++)
				{
					
					hiddenOutput = net.get(numInputs + l).getOutput(); // u_k
					
					delta_k = errors.get(k *dataRows +i)* sigmoidDerivative ; // delta_k = (out - target) * g'_k(z_k)
					gradient = delta_k * hiddenOutput; // delta_k * u_k
					
					// update weights with momentum
					net.get(numInputs + numHidden).updateWeight(learningRate*gradient, l);
				}
				
				// output layer bias update:
				
				net.get(numInputs + numHidden).updateBias(learningRate*delta_k);
				
				
				
				
				// calculate gradient term for hidden neuron weights
				// dE/dW_ij = u_i * g'_j(z_j) * delta_k * w_jk (since there is only one output neuron, there is no
				// summation of delta_l*w_jk over additional outputs)
				// dE/dW_ij = delta_j * u_i
				
				sigmoidDerivative =  net.get(numInputs + numHidden).sigmoidDerivativeOutput();
				for(l=0;l<numHidden;l++)
				{
					for(m=0;m<numInputs; m++)
					{
						inputOutput = net.get(m).getOutput(); // u_i
						
						
						sigmoidDerivativeHidden = net.get(numInputs + l).sigmoidDerivativeOutput(); // g'_j(z_j)
						outputWeight =  net.get(numInputs  + numHidden).getWeight(l); // w_jk 
						delta_k = errors.get(k *dataRows + i ) * sigmoidDerivative;
						delta_j = delta_k * sigmoidDerivativeHidden * outputWeight; // delta_k * w_jk * g'_j(z_j)
						gradient =  inputOutput *delta_j; // delta_j * u_i
						
						// update weights with momentum
						net.get(numInputs + l).updateWeight(learningRate*gradient, m);
					}
					
					// hidden layer bias update:
					
					net.get(numInputs + l).updateBias(learningRate*delta_j);
					
					
				}
				
				
			}
			
			
			//double learningDecay = 100000;
			
			//learningRate = learningRate * Math.exp(-k/learningDecay);
			long seed = System.nanoTime();
			Collections.shuffle(fileData, new Random(seed));
			
			// save training errors to arraylist
			double temp = 0;
			for(i=0;i<dataRows;i++)
			{
				temp += Math.pow(errors.get(k*dataCols + i), 2);
			}
			totalerrors.add(0.5 * temp);
			
			System.out.format("Epoch %d TSE: %10.10f\n", k,0.5*temp);
		
		}
	}

	// used for online NN training for RL agent
	public void train2(double loss, ArrayList<Double> nnIn, int roundNum, boolean saveErr, double max)
	{	
		// train network using online back propagation
		// 
		ArrayList<Double> inputs = new ArrayList<Double>();
		int i=0,j=0,k=0,l=0,m=0;
		double out, target, gradient;
		
		
		// iterate for a predefined number of epochs
		
			
		// feedforward the input signals
		feedForward(nnIn);
		
		// get output of output neuron
		out = net.get(numInputs + numHidden).computeOutput();
		if(Double.isNaN(out)) throw new NumberFormatException("Nan encountered");
		
		// get target value for backprop
		target = loss;
		
		// compute error
		double temp = target - out;
		
		if(saveErr == true)
		{
			try
			{
				FileWriter fileWriter = new FileWriter("errors.csv", true);
				String s;
				s = String.format("%d,%f\n", roundNum, max - out);
				fileWriter.write(s);
				
				
				fileWriter.close();
				
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		
		
		
		//errors.add(temp);
		
	
		//System.out.format("Row: %d, target: %10.8f error: %10.8f\n", i,target, temp);
		//System.out.println("Epoch: " + k + " inputs: " + inputs.get(0) + " " + inputs.get(1) + " output: " + out  + " Target: " + target + " Error: " + errors.get(k *dataRows +i));
		
		double sigmoidDerivative = 0, sigmoidDerivativeHidden = 0;
		double hiddenOutput,inputOutput, outputWeight, delta_k=0, delta_j=0;
		
		
		
		/********************************this is where the magic happens**********************************************************/
	
		
		
		
		// calculate gradient term for output neuron weights
		// dE/dW_jk = delta_k * u_k; where delta = (out - target) * g'_k(z_k), where z_k is the input to the output neurons
		// and u_k is the output of the kth hidden neuron
		
		// since g(x) = (b-a)/(1 + e^-x) + a, g'(x) = e^x * (b-a) / (e^x + 1)^2
		sigmoidDerivative =  net.get(numInputs + numHidden).sigmoidDerivativeOutput();
		
		for(l=0;l<numHidden;l++)
		{
			
			hiddenOutput = net.get(numInputs + l).getOutput(); // u_k
			
			delta_k = temp* sigmoidDerivative ; // delta_k = (out - target) * g'_k(z_k)
			gradient = delta_k * hiddenOutput; // delta_k * u_k
			
			// update weights with momentum
			net.get(numInputs + numHidden).updateWeight(learningRate*gradient, l);
		}
		
		// output layer bias update:
		
		net.get(numInputs + numHidden).updateBias(learningRate*delta_k);
		
		
		
		
		// calculate gradient term for hidden neuron weights
		// dE/dW_ij = u_i * g'_j(z_j) * delta_k * w_jk (since there is only one output neuron, there is no
		// summation of delta_l*w_jk over additional outputs)
		// dE/dW_ij = delta_j * u_i
		
		sigmoidDerivative =  net.get(numInputs + numHidden).sigmoidDerivativeOutput();
		for(l=0;l<numHidden;l++)
		{
			for(m=0;m<numInputs; m++)
			{
				inputOutput = net.get(m).getOutput(); // u_i
				
				
				sigmoidDerivativeHidden = net.get(numInputs + l).sigmoidDerivativeOutput(); // g'_j(z_j)
				outputWeight =  net.get(numInputs  + numHidden).getWeight(l); // w_jk 
				delta_k =temp * sigmoidDerivative;
				delta_j = delta_k * sigmoidDerivativeHidden * outputWeight; // delta_k * w_jk * g'_j(z_j)
				gradient =  inputOutput *delta_j; // delta_j * u_i
				
				// update weights with momentum
				net.get(numInputs + l).updateWeight(learningRate*gradient, m);
			}
			
			// hidden layer bias update:
			
			net.get(numInputs + l).updateBias(learningRate*delta_j);
			
			
		}
		
	}
	
	@Override
	public void save(File argFile) 
	{
		
	}
	
	public void loadWeights() throws IOException
	{
		
		
		int i=numInputs,j=0,k=0,l=0;
		
		
		
		int rows = 0;
		
		File file = new File("C:\\Users\\root\\workspace\\Net1\\weights.csv");
       
        Scanner rowScanner = new Scanner(file);
        Scanner colScanner;

        String line = null;
        
        while (rowScanner.hasNextLine()) 
        {

          line = rowScanner.nextLine();
         
    	  
          colScanner = new Scanner(line).useDelimiter(",");
          while (colScanner.hasNext()) 
          {
            double val =  Float.parseFloat(colScanner.next());
           
            net.get(i).weightVector.set(j, val);
            
           
            j++;
          } 
          colScanner.close();
         i++;
         j=0;
        }
        
        rowScanner.close();
        
    	
	}
	
	public void saveWeights() throws IOException
	{
		FileWriter fileWriter = new FileWriter("weights.csv", false);
		
		int i=0,j=0,k=0,l=0;
		
		for(i=numInputs;i<net.size();i++)
		{
			for(j=0;j<net.get(i).weightVector.size(); j++)
			{
				String s = String.format("%f", net.get(i).weightVector.get(j));
				fileWriter.write(s);
				
				if(j != net.get(i).weightVector.size() - 1)
					fileWriter.write(",");
					
			}
			
			fileWriter.write("\n");
		}
		
		fileWriter.close();
	}
	
	// load a CSV file: format x , y , z , ... , u, where x y z are training inputs and u is an expect output
	public void loadData(String argFileName) throws FileNotFoundException
	{
		int rows = 0;
		
		File file = new File(argFileName);
       
        Scanner rowScanner = new Scanner(file);
        Scanner colScanner;

        String line = null;
        
        while (rowScanner.hasNextLine()) 
        {

          line = rowScanner.nextLine();
         
    	  fileData.add(new ArrayList<Double>());
          colScanner = new Scanner(line).useDelimiter(",");
          while (colScanner.hasNext()) 
          {
            double val =  Float.parseFloat(colScanner.next());
            fileData.get(rows).add(val);
            
            if(rows < 1)
            {
            	dataCols++;
            }
           
          } 
          colScanner.close();
          rows++;
        }
        
        rowScanner.close();
        
    	dataRows = rows;

    	
    	
	}


	@Override
	public void load(String argFileName)
	{
		
		
	}

	@Override
	public double sigmoid(double x)
	{
		return 0;
	}

	@Override
	public double customSigmoid(double x)
	{
		return 0;
	}

	@Override
	public void initializeWeights()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void zeroWeights()
	{
		// TODO Auto-generated method stub
		
	}
	
	// feedforward data and get output
	public void feedForward(ArrayList<Double> argInput)
	{
		int i = 0;
		ArrayList <Double> temp = new ArrayList<Double>();
		temp.add(0.0);
		// feed data into input neurons and compute output
		for( i = 0;i<numInputs; i++)
		{
			temp.set(0,argInput.get(i));
			net.get(i).loadInputs(temp);
		}
		
		ArrayList <Double> inputLayerOutput = new ArrayList<Double>();
		for(i=0;i<numInputs; i++)
		{
			inputLayerOutput.add(net.get(i).computeOutput());
		}
		
		// feedforward to hidden layer
		for(i=numInputs;i < numInputs+numHidden; i++)
		{
			net.get(i).loadInputs(inputLayerOutput);
		}
		
		ArrayList <Double> hiddenLayerOutput = new ArrayList<Double>();
		for(i=numInputs;i < numInputs+numHidden; i++)
		{
			hiddenLayerOutput.add(net.get(i).computeOutput());
		}
		
		// feed to final output layer
		for(i=numInputs+numHidden;i < numInputs+numHidden + outDim; i++)
		{
			net.get(i).loadInputs(hiddenLayerOutput);
		}

		for(i=numInputs+numHidden;i < numInputs+numHidden + outDim; i++)
		{
			net.get(i).computeOutput();
		}
		
	};
	
	// NNet constructor
	public NNet(int argNumInputs, int argNumHidden,
			double argLearningRate,
			double argMomentumTerm,
			double argA,
			double argB,
			int argEpochs, boolean loadW) 
	{
		fileData = new ArrayList<ArrayList<Double>>();
		
		dataRows = 0;
		dataCols = 0;
		
		outDim = 1;
		
		epochs = argEpochs;
		
		numInputs = argNumInputs; // number of input neurons
		numHidden = argNumHidden; // number of hidden neurons
		learningRate = argLearningRate; // learning rate
		momentumTerm = argMomentumTerm; // momentum
		A = argA; // sigmoid asymptote A (-ve x direction)
		B = argB; // sigmoid asymptote B
		
		errors = new ArrayList<Double>();
		totalerrors = new ArrayList<Double>();
		
		// create the neural net
		int i = 0;
		
		// create input layer neurons
		for( i = 0; i< numInputs; i++)
		{
			net.add(new Node(1, A, B, 0, 1,momentumTerm));
		}
		
		// hidden layer
		for( i = 0;i < numHidden; i++)
		{
			net.add(new Node(numInputs, A, B, bias, 0, momentumTerm));
		}
		
		// output layer
		net.add(new Node(numHidden, A, B, bias, 0, momentumTerm));
		
		System.out.println("Neural network created: " + numInputs + " inputs , hidden units: " + numHidden + "\n\n");
	
	
		if(loadW==true)
			try
			{
				loadWeights();
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	public int getRows()
	{
		return dataRows;
	}
	
	public int getCols()
	{
		return dataCols;
	}
	
	
	
}
