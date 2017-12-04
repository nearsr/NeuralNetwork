import java.text.DecimalFormat;
import java.util.Random;

//Java is rows, cols
//See machine learning slides 14 for variable usage

public class NeuronLayer {
	LayerType type;
	int n; //number of neurons
	int l; //layer number l
	int n_prev; //number of neurons in previous layer (nl-1)
	
	//weigh is input weights,
	//thesh is output threshold/bias
	double[][] weights; //WL = nl * nl-1  matrix whose row describes the weights for a unit in later l
	double[] threshes; //ThetaL = nl * 1 vector whose entries correspond to the biases of the units in layer l
	
	Random rand = new Random(l); //use layer num as seed
	DecimalFormat df = new DecimalFormat("#.####");
	
	public NeuronLayer(LayerType type, int numNeurons, int numNeuronsPrevLayer, int layerDepth) {
		this.type = type;
		this.n = numNeurons;
		this.l = layerDepth;
		this.n_prev = numNeuronsPrevLayer;
		
		weights = new double[n][n_prev]; //row represents a neuron, col is that neuron's input weights
		threshes = new double[n]; //row represents a neuron's threshold
	}
	
	public void initializeRandomState() {
		if(type == LayerType.INPUT) {
			System.out.println("Input layer does not need weights/thresh. It simply replicates inputs.");
			System.exit(1);
		}
		
		for (int i =0 ; i < n; i++) {
			for (int j =0; j < n_prev; j++) {
				weights[i][j] = rand.nextDouble();
			}
		}
		
		for (int i =0 ; i < n; i++) {
			threshes[i] = rand.nextDouble();
		}
		
	}
	
	public void initializeEmpty() {		
		for (int i =0 ; i < n; i++) {
			for (int j =0; j < n_prev; j++) {
				weights[i][j] = 1;
			}
		}
		
		for (int i =0 ; i < n; i++) {
			threshes[i] = 1;
		}
		
	}
	
	public double[][] getWeights() {
		return weights;
	}

	public void setWeights(double[][] weights) {
		this.weights = weights;
	}

	public double[] getThreshes() {
		return threshes;
	}

	public void setThreshes(double[] threshes) {
		this.threshes = threshes;
	}

	public LayerType getType() {
		return type;
	}

	public int getNumNeurons() {
		return n;
	}

	public int getLayerNum() {
		return l;
	}

	public int getNumNeuronsPrev() {
		return n_prev;
	}

	public void printState() {
		System.out.println("Layer " + l);
		for (int i =0 ; i < n; i++) {//loop through neurons slower
			System.out.print("Neuron " + i + ": ");
			for (int j =0; j < n_prev; j++) { //loop through input weights faster
				System.out.print("w" + j + " = " + df.format(weights[i][j]) + " ");
			}
			System.out.println("theta = " + df.format(threshes[i]));
		}
		System.out.println();
	}
}
