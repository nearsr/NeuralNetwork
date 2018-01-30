import java.util.ArrayList;
import java.util.Arrays;

//This code only supports one layer of hidden neurons
//Sources: 
//https://mattmazur.com/2015/03/17/a-step-by-step-backpropagation-example/
//https://kunuk.wordpress.com/2010/10/11/neural-network-backpropagation-with-java/


public class NeuralNetwork {
	NeuronLayer[] layers;
	double[][] inputs;
	double[][] trueOutputs;
	double[][] checkInputs;
	double[][] checkOutputs;
	int numInputNeurons;
	int numOutputNeurons;
	int numHiddenNeurons;
	double learningRate;
	ImageReader reader;
	ArrayList<TrainingImage> trainingImages;
	int numCheckCorrect = 0;
	int numCheckTotal = 0;
	private int totalNumImages;
	private int numTrainingCutoff;

	boolean allowSunglasses = true;
	boolean runTest = false;


	public static void main(String[] args) {
		NeuralNetwork network = new NeuralNetwork();
	}

	public NeuralNetwork() {

		reader = new ImageReader();
		reader.createTrainingInput(allowSunglasses);
		trainingImages = reader.getTrainingImages();

		if (runTest) {
			makeTestNetwork();
		}
		else {
			makeNetwork();
		}

		if(runTest) {
			int i = 0;
			int bound = 1000;

			while(i < bound) {
				trainInputs();
				i++;
			}
		}
		else {
			trainInputs();
		}

		validateInputs();
	}

	public void makeNetwork() {
		//Number of input and output variables
		//will determine number of input and output neurons
		numInputNeurons = 30*32; //dimensions of img
		numOutputNeurons = 4; //types of moods
		numHiddenNeurons = 3; //recommended for this problem
		//Learning rate determines how far in the direction of steepest descent to change the parameter
		learningRate = 0.3; //.3 is recommended by Machine Learning -Tom Mitchell for this type of problem

		double verificationPercent = .2;
		totalNumImages = reader.getMaxImageId();
		numTrainingCutoff = (int) Math.round(totalNumImages*(1.0-0.2));

		System.out.println("totalNum " + totalNumImages + " num to use for train " + numTrainingCutoff + " num for check " + (totalNumImages - numTrainingCutoff));

		//row, col
		//row represents an input, cols are variables in that input
		//row represents an output, cols are nodes that store vars for that output (in the case of XOR example, 1 output node)
		inputs = new double[numTrainingCutoff][numInputNeurons];
		trueOutputs = new double[numTrainingCutoff][numOutputNeurons];
		//the below are for verification
		checkInputs = new double[totalNumImages - numTrainingCutoff][numInputNeurons];
		checkOutputs = new double[totalNumImages - numTrainingCutoff][numOutputNeurons];

		int i = 0;
		int j = 0;
		for (TrainingImage t : trainingImages) {
			double[] nextInput = t.getNormalizedImage();
			double[] nextOutput = t.getExpectedOutput();

			if (i < numTrainingCutoff) {//add to training input
				inputs[i] = nextInput;
				trueOutputs[i] = nextOutput;
				i++;
			}
			else {//add to check input
				checkInputs[j] = nextInput;
				checkOutputs[j] = nextOutput;
				j++;
			}
		}

		NeuronLayer inputLayer = new NeuronLayer(LayerType.INPUT, numInputNeurons, 0, 0);
		NeuronLayer hiddenLayer = new NeuronLayer(LayerType.HIDDEN, numHiddenNeurons, numInputNeurons, 1);
		NeuronLayer outputLayer = new NeuronLayer(LayerType.OUTPUT, numOutputNeurons, numHiddenNeurons, 2);

		inputLayer.initializeEmpty();
		hiddenLayer.initializeRandomState();
		outputLayer.initializeRandomState();

		//inputLayer.printState();
		hiddenLayer.printState();
		outputLayer.printState();

		layers = new NeuronLayer[3];
		layers[0] = inputLayer;
		layers[1] = hiddenLayer;
		layers[2] = outputLayer;
	}

	public void makeTestNetwork() {
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
		checkInputs = inputs;
		checkOutputs = trueOutputs;

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

	public void validateInputs() {
		double[] x_prev = null; //x = output vector for a layer. Rows represent different neuron's outputs.
		double[] outputs = null;
		double[] hiddenOutputs = null;
		double[] x0 = new double[checkInputs[0].length];

		for(int i = 0; i < checkInputs.length; i++) {//loop through each possible input
			for(int j = 0; j < checkInputs[0].length; j++) {//loop through both neurons in input
				//make each input value the output, xj, of layer 0 neuron j
				//because input nodes simply spit out input values
				x0[j] = checkInputs[i][j];
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
					System.out.println("Final output: " + Arrays.toString(x_prev));
					System.out.println("VALIDATING Expected Output: " + Arrays.toString(checkOutputs[i]) );
					outputs = x_prev;

					if (runTest) {
						double expected = trueOutputs[i][0];
						double actual = outputs[0];
						double actualRounded;

						if (actual > .5) {
							actualRounded = 1.0;
						}
						else {
							actualRounded = 0.0;
						}

						numCheckTotal++;
						if (actualRounded == expected) {
							numCheckCorrect++;
						}
					}
					else {
						//if final layer, check if matches expected values
						int expectedIndex = 0;
						int highestIndex = 0;
						int maxVal = 0;
						for (int k = 0 ; k < checkOutputs[i].length; k++) {
							if (checkOutputs[i][k] == 1.0) {
								expectedIndex = k;
								break;
							}
						}
						for (int a = 0; a < outputs.length; a++) {
							if (outputs[a] > maxVal) {
								maxVal = 0;
								highestIndex = a;
							}
						}

						numCheckTotal++;
						if (highestIndex == expectedIndex) {
							numCheckCorrect++;
						}
					}

				}
				else {//must be hidden layer
					System.out.println("Output length: " + x_prev.length);
					System.out.println("Output: " + Arrays.toString(x_prev));
					hiddenOutputs = x_prev;
				}
			}
		}
		System.out.println("\n\nRunning XOR test: " + runTest);
		System.out.println("Allowing sunglasses: " + allowSunglasses);
		System.out.println("totalNum " + totalNumImages + " num to use for train " + numTrainingCutoff + " num for check " + (totalNumImages - numTrainingCutoff));
		System.out.println("Validations correct: " + numCheckCorrect + " Total number of validations: " + numCheckTotal);
		System.out.println("% of validations correct: " + ((double)numCheckCorrect/numCheckTotal));
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
					System.out.println("Final output: " + Arrays.toString(x_prev));
					System.out.println("Expected Output: " + Arrays.toString(trueOutputs[i] ));
					outputs = x_prev;
				}
				else {//must be hidden layer
					System.out.println("Output length: " + x_prev.length);
					System.out.println("Output: " + Arrays.toString(x_prev));
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
				if(r == 0) continue; //input layer will not change, so don't print it
				layers[r].printState();
			}
			System.out.println("Training iteration complete.");
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
