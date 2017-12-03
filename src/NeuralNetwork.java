import java.util.ArrayList;

public class NeuralNetwork {
	NeuronLayer[] layers;
	double[][] inputs;
	double[][] trueOutputs;
	int numInputNeurons;
	int numOutputNeurons;
	double learningRate;
	
	public static void main(String[] args) {
		NeuralNetwork network = new NeuralNetwork();
	}
	
	public NeuralNetwork() {
		//Number of input and output variables
		//will determine number of input and output neurons
		numInputNeurons = 2;
		numOutputNeurons = 1;
		//Learning rate determines how far in the direction of steepest descent to change the parameter
		learningRate = 0.3; //Recommended by Machine Learning -Tom Mitchell for this type of problem
		
		//row, col
		//row represents an input, cols are variables in that input
		//row represents an output, cols are nodes that store vars for that output (in the case of XOR example, 1 output node)
		inputs = new double[][]{{1, 1}, {1, 0}, {0, 1}, {0, 0}};
	    trueOutputs = new double[][]{{0}, {1}, {1}, {0}};
		
	    makeNetwork();

	}
	
	void makeNetwork() {
		NeuronLayer inputLayer = new NeuronLayer(LayerType.INPUT, numInputNeurons, 0, 0);
		NeuronLayer hiddenLayer = new NeuronLayer(LayerType.HIDDEN, 3, numInputNeurons, 1);
		NeuronLayer outputLayer = new NeuronLayer(LayerType.OUTPUT, numOutputNeurons, 3, 2);
		
		inputLayer.initializeEmpty();
		hiddenLayer.initializeRandomState();
		outputLayer.initializeRandomState();
		
		inputLayer.printState();
		hiddenLayer.printState();
		outputLayer.printState();
		
		layers[0] = inputLayer;
		layers[1] = hiddenLayer;
		layers[2] = outputLayer;
	}
	

}
