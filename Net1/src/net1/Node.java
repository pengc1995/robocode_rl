package net1;
import java.util.ArrayList;
import java.util.Random;

// Atomic neuron object
public class Node
{
	public ArrayList<Double> inputVector = new ArrayList<Double>();
	public ArrayList<Double> weightVector = new ArrayList<Double>();

	public double nOut, nBias, momentum;
	
	
	ArrayList<Double> prevW;
	public int inputNeuron;
	
	
	public int numInputs;
	
	public double A, B; // sigmoid parameters
	
	public double getOutput()
	{
		return nOut;
	}
	
	
	// take dot product of weights and inputs and feed to sigmoid, store output
	public double computeOutput()
	{
		
		if(inputNeuron == 1)
		{
			nOut = inputVector.get(0);
			return nOut;
		}
		
		int i = 0;
		double product = 0;
		
		
		
		
		for (i=0;i<numInputs;i++)
		{
			
			product += (inputVector.get(i) * weightVector.get(i));
		}
		
		product += nBias;
		
		// sigmoid output calculation
		nOut = (B-A) / (1 + java.lang.Math.exp(-product)) + A;
		return nOut;
	}
	
	// derivative of the general sigmoid (b-a)/(1+e^-x) + a is (b-a)/(1+e^x)^2
	public double sigmoidDerivativeOutput()
	{
		int i = 0;
		double product = 0, out=0;
		for (;i<numInputs;i++)
		{
			
			product += (inputVector.get(i) * weightVector.get(i));
		}
		
		product += nBias;
		
		out = java.lang.Math.exp(product) * (B-A) / java.lang.Math.pow((java.lang.Math.exp(product) + 1), 2);
		return out;
	}
	
	// Node constructor initializes the neuron
	public Node(int argNumInputs, double argA, double argB, double argBias, int isInput, double argMomentum)
	{
		inputNeuron = isInput;
		A = argA;
		B = argB;
		nOut = 0;
		prevW = new ArrayList<Double>();
		
		momentum = argMomentum;
		
		Random generator = new Random();
		double randNum;
		
		numInputs = argNumInputs; // init number of inputs for this neuron
		int i = 0;
		
		// initialize weights (include bias weight) to random values between -0.5 and 0.5
		for (i=0;i<numInputs ;i++)
		{
			randNum = generator.nextDouble() - 0.5;
			weightVector.add(randNum);
		}
		
		nBias = generator.nextDouble() - 0.5;
		
		// initialize inputs to 0
		for (i=0;i<numInputs;i++)
		{
			
			inputVector.add(0.0);
		}
		
		for(i=0;i<numInputs+1;i++)
		{
			prevW.add(0.0);
		}
		
		
	}
	
	
	public double getWeight(int index)
	{
		return weightVector.get(index);
	}
	
	// functions to modify private members
	public void loadInputs(ArrayList<Double> in)
	{
		int i=0;
		for(i=0;i<numInputs; i++)
		{
			inputVector.set(i, in.get(i));
		}
		
	}
	
	public void updateWeight(double val, int index)
	{
		
		
		
		
		double prevDeltaW = prevW.get(index);
		double deltaW = val + momentum * prevDeltaW;
		double result = weightVector.get(index) + deltaW;
		
		prevW.set(index, deltaW);
		weightVector.set(index,result);
	}
	
	public void updateBias(double val)
	{
		double prevDeltaB = prevW.get(numInputs);;
		double deltaB = val + momentum * prevDeltaB;
		nBias  = nBias + deltaB;
		
		prevW.set(numInputs,deltaB );
	}
	
}
