import java.util.ArrayList;

//This code only supports one layer of hidden neurons
//Sources: 
//https://mattmazur.com/2015/03/17/a-step-by-step-backpropagation-example/
//https://kunuk.wordpress.com/2010/10/11/neural-network-backpropagation-with-java/


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

		ImageReader reader = new ImageReader();
		byte[] img = reader.readImage("faces/kawamura/kawamura_left_happy_sunglasses_4.pgm");
		
	    makeNetwork();
	    
	    int i = 0;
	    int bound = 1;
	    
	    while(i < bound) {
	    	 trainInputs();
	    	 i++;
	    }
	   

	}
	
	public void makeNetwork() {
		//Number of input and output variables
		//will determine number of input and output neurons
		numInputNeurons = 2;
		numOutputNeurons = 1;
		//Learning rate determines how far in the direction of steepest descent to change the parameter
		learningRate = 0.8; //.3 is recommended by Machine Learning -Tom Mitchell for this type of problem
		//row, col
		//row represents an input, cols are variables in that input
		//row represents an output, cols are nodes that store vars for that output (in the case of XOR example, 1 output node)
		inputs = new double[][]{{1, 1}, {1, 0}, {0, 1}, {0, 0}};
	    trueOutputs = new double[][]{{0}, {1}, {1}, {0}};
	    
		NeuronLayer inputLayer = new NeuronLayer(LayerType.INPUT, numInputNeurons, 0, 0);
		NeuronLayer hiddenLayer = new NeuronLayer(LayerType.HIDDEN, 3, numInputNeurons, 1);
		NeuronLayer outputLayer = new NeuronLayer(LayerType.OUTPUT, numOutputNeurons, 3, 2);
		
		inputLayer.initializeEmpty();
		hiddenLayer.initializeRandomState();
		outputLayer.initializeRandomState();
		
		inputLayer.printState();
		hiddenLayer.printState();
		outputLayer.printState();
		
		layers = new NeuronLayer[3];
		layers[0] = inputLayer;
		layers[1] = hiddenLayer;
		layers[2] = outputLayer;
	}
		
	public void trainInputs() {
		double[] x_prev = null; //x = output vector for a layer. Rows represent different neuron's outputs.
		double[] outputs = null;
		double[] hiddenOutputs = null;
		double[] x0 = new double[inputs[0].length];
		
		for(int i = 0; i < inputs.length; i++) {//loop through each possible input
			for(int j = 0; j < inputs[0].length; j++) {//loop through both neurons in input
				//make each input value the output, xj, of layer 0 neuron j
				//because input nodes simply spit out input values
				x0[j] = inputs[i][j];
			}
			//the input layer's input directly becomes output
			x_prev = x0;
			System.out.println("\nInput " + i + ": " + "x[0] = " + x0[0] + "; x[1] = " + x0[1]);
			
			//find next output by processing previous layer's output
			//The output neurons in layers[size-1] will give outputs that
			//represent the final variable output values
			for (int L = 1; L < layers.length; L++) {//go through all layers
				System.out.println("Layer " + L);
				//x is output vector to hold new outputs we will calculate
				//one output for each neuron in the layer
				double[] x = new double[layers[L].getNumNeurons()];
				double[][] weights = layers[L].getWeights();
				double[] thetas = layers[L].getThreshes();
				
				for (int neuron = 0; neuron < x.length; neuron++) {//go through all neurons in layer
					double[] W = weights[neuron];
					double theta = thetas[neuron];
					x[neuron] = activationFunction(dotProduct(W,x_prev) + theta);
				}
				//The output of this layer will become the "previous output"
				//aka input for the next layer
				x_prev = x;
				
				if (L == layers.length-1) {//if final output layer
					System.out.println("Output length (should be one): " + x_prev.length);
					System.out.println("Final output: " + x_prev[0]);
					System.out.println("Expected Output: " + trueOutputs[i][0] );
					outputs = x_prev;
				}
				else {//must be hidden layer
					System.out.println("Output length: " + x_prev.length);
					System.out.println("Output: " + x_prev[0]);
					hiddenOutputs = x_prev;
				}
			}

			//Back propagation
			for (int neuron = 0; neuron < layers[2].getNumNeurons(); neuron++) {//go through all neurons in output layer
				for (int connection = 0; connection < layers[2].getWeights()[neuron].length; connection++) {// go through all connecting weights
					double[][] weights = layers[2].getWeights();
					double[] W = weights[neuron];
					double currentWeight = W[connection];

					double targetOut =  trueOutputs[i][neuron];
					double out = outputs[neuron];
					//output of hidden neuron which connects to current weight
					//thus, we want the output out the previous layer neuron
					//that is numbered the same as "connection"
					double hiddenOut = hiddenOutputs[connection];

					double partialDerivative = -(targetOut - out)*out*(1-out)*hiddenOut;
					double deltaWeight = -learningRate * partialDerivative;
					double newWeight = currentWeight + deltaWeight;
					
					layers[2].getWeights()[neuron][connection] = newWeight;
					
					//ak = out
					//ai = hiddenOut
				}
			}
			
			//Hidden layer is a bit different
			for (int neuron = 0; neuron < layers[1].getNumNeurons(); neuron++) {//go through all neurons in hidden layer
				for (int connection = 0; connection < layers[1].getWeights()[neuron].length; connection++) {// go through all connecting weights
					double[][] weights = layers[1].getWeights();
					double[] W = weights[neuron];
					double currentWeight = W[connection];

					//output of current hidden neuron
					double hiddenOut = hiddenOutputs[neuron]; //aj, now our outputs are from hidden layer
					//this next one is like hiddenOut in previous step
					double firstOut = inputs[i][connection]; //ai
					
					double sumKoutputs = 0;
					for (int outNeuron = 0; outNeuron < layers[2].getNumNeurons(); outNeuron++) {
						double out =  outputs[outNeuron]; //ak
						double targetOut =  trueOutputs[i][outNeuron];
						//wjk = get weight of connection between current hidden layer neuron and
						//		current output neuron
						double wjk = layers[2].getWeights()[outNeuron][neuron];

						sumKoutputs = sumKoutputs+ (-(targetOut-out) * out * (1-out) * wjk);
					}

					double partialDerivative = hiddenOut * (1-hiddenOut) * firstOut * sumKoutputs;
					double deltaWeight = -learningRate * partialDerivative;
					double newWeight = currentWeight + deltaWeight;
					
					layers[1].getWeights()[neuron][connection] = newWeight;
					
					//ai = inputNeuron.getOutput ==> inputs
					//aj = hiddenNeuron.getOutput ==>hiddenOutputs
					//ak = outputNeuron.getOutput ==> out
					//wjk = get weight of connection between current hidden layer neuron and
					//		current output neuron
				}
			}			
			
			for (int r = 0; r<layers.length; r++) {
				layers[r].printState();
			}
		}
	}
	
	public double dotProduct(double[] W, double[] x_prev) {
		double finalSum = 0;
		if (W.length != x_prev.length) {
			System.out.println("Matrix mismatch in dot product.");
			System.exit(1);
		}
		
		for (int i = 0; i < W.length; i++) {
			finalSum+= W[i]*x_prev[i];
		}
		
		return finalSum;
	}
	
	public double activationFunction(double t) {
		//Sigmoidal by default
		return 1/(1 + Math.exp(-t));
	}
	
	

}
