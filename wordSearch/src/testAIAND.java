import java.util.*;
import java.io.*;

public class testAIAND {
    public static void main(String[] args) {
        //simple AND neural network
        //2 layers
        //2 input nodes, 1 constant node
        //1 calculating node
        //1 output


        double[][] trainingData = {{0,0},{0,1},{1,0},{1,1}}; //outer group is each case, inner group is each input node
        double[][] trainingAnswers = {{0},{0},{0},{1}}; //outer group is each case, inner group is each output node

        double[][] testingData = {{0,0},{0,1},{1,0},{1,1}};
        double[][] testingAnswers = {{0},{0},{0},{1}};

        int trainingIterations = 100;
        double learningRate = 0.1;

        /**randomize weights and constants later**/
        double[][][] weights = {{{0.5, 0.5}}};//outer groups for each layer (starting from layer after input), middle for each node, inner for each connection
        double[] constants = {0.5}; //constants of each layer (starting from input layer)

        double[][] weightedAverages =  {{0}};//outer is each layer, inner is each node
        double[][] activations = {{0}};//weightedAverages passed through reLU

        double[][] derivActivations = {{0}};
        double[][][] derivWeights = {{{0,0}}};
        double[] derivConstants = {0};



        //TRAINING
        for (int currentIteration = 0; currentIteration < trainingIterations; currentIteration++) {
            System.out.println("Iteration "+currentIteration+"\n");
            for (int currentCase = 0; currentCase < trainingData.length; currentCase++) {
                System.out.println("Training Case "+currentCase);
                //forward propogation
                forwardPropogation(trainingData[currentCase], weights, constants, weightedAverages, activations);
                System.out.println("Cost: " + getCost(activations, trainingAnswers[currentCase]));//checking initial cost

                //backward propogation
                backPropogation(trainingData[currentCase], weights, constants, weightedAverages, activations, derivWeights, derivConstants, derivActivations, trainingAnswers[currentCase]);
                /*for (int currentLayer = 0; currentLayer < activations.length; currentLayer++) {//should have another for loop for nodes in output layer
                    System.out.println("derivConstants in Layer " + currentLayer + ": " + derivConstants[currentLayer]);
                    if (currentLayer == 0) {
                        for (int currentConnection = 0; currentConnection < trainingData[currentCase].length; currentConnection++) {
                            System.out.println("derivWeights in Layer " + currentLayer + " Connection " + currentConnection + ": " + derivWeights[currentLayer][0][currentConnection]);
                        }
                    }
                }*/

                //gradient descent
                gradientDescent(weights, constants, derivWeights, derivConstants, learningRate);

                forwardPropogation(trainingData[currentCase], weights, constants, weightedAverages, activations);//checking effect of gradient descent
                System.out.println("Cost: " + getCost(activations, trainingAnswers[currentCase]) + "\n");//checking later cost
            }
        }

        //TESTING
        double averageWrongness = 0;
        for (int currentCase = 0; currentCase < testingData.length; currentCase++) {
            forwardPropogation(testingData[currentCase],weights, constants, weightedAverages, activations);
            /**add for loop to check each node of the answer**/
            averageWrongness+=Math.abs(activations[activations.length-1][0]-testingAnswers[currentCase][0]);
        }
        averageWrongness/= testingData.length;
        System.out.println(averageWrongness+" average difference from actual");
    }

    public static void forwardPropogateLayer (double[] input, double[][] layerWeights, double layerConstant, int currentLayer, double[][] weightedAverages, double[][] activations) {//finding prediction for our given weights and inputs, one layer
        //inputs, each input
        //weights, outer layer for each node, inner layer for each connection

        for (int currentNode = 0; currentNode < layerWeights.length; currentNode++){ //go through each node on the current layer
            double weightedAverage = 0;//weighted average of the current node of the current layer
            for (int currentConnection = 0; currentConnection < layerWeights[currentNode].length; currentConnection++) {//go through each connection going into this node
                weightedAverage += input[currentConnection] * layerWeights[currentNode][currentConnection]; //add product of weights and previous inputs
            }
            weightedAverage+=layerConstant;//add the layer constant

            weightedAverages[currentLayer][currentNode] = weightedAverage; //store the weighted average in the weighted average matrix
            activations[currentLayer][currentNode] = reLU(weightedAverage);//store the reLU of the weighted average in the activations matrix
        }
    }
    public static void forwardPropogation (double[] input, double[][][] weights, double[] constants, double[][] weightedAverages, double[][] activations) {
        forwardPropogateLayer(input, weights[0], constants[0],0, weightedAverages, activations);//forward prop input layer (layer 0)
        for (int currentLayer = 1; currentLayer < activations.length; currentLayer++) {//go through each layer after input (layer 1-activations.length)
            forwardPropogateLayer(activations[currentLayer], weights[currentLayer], constants[currentLayer], currentLayer, weightedAverages, activations);
        }
        for (int currentNode = 0; currentNode < activations[activations.length-1].length; currentNode++) {//go through all nodes of the last layer
            System.out.println("Layer "+(activations.length-1)+" Node "+currentNode+" Activation: "+activations[activations.length-1][currentNode]);//print out the activations of the last layer
        }
    }
    public static void backPropogation (double[] input, double[][][] weights, double[] constants, double[][] weightedAverages, double[][] activations, double[][][] derivWeights, double[] derivConstants, double[][] derivActivations, double[] answers) {
        int currentLayer = weightedAverages.length-1;/**place into for loop later to go through all layers, starting at last layer, ending at layer after input**/

        for (int currentNode = 0; currentNode < weightedAverages[currentLayer].length; currentNode++){//go through each node of the current layer
            if (weightedAverages[currentLayer][currentNode]<=0) {//if the weighted average of the current node is less than or equal to 0
                derivConstants[currentLayer] = 0;//derivative of the current layer constant for the current node equals 0
                for (int currentConnection = 0; currentConnection < weights[currentLayer][currentNode].length; currentConnection++) {
                    derivWeights[currentLayer][currentNode][currentConnection] = 0;//derivative of the weight from the current connection of the previous layer to the current node of the next layer equals 0
                }
            }
            else {//if the weighted average is greater than 0
                derivConstants[currentLayer] = 2 * (weightedAverages[currentLayer][currentNode] - answers[currentNode]);
                for (int currentConnection = 0; currentConnection < weights[currentLayer][currentNode].length; currentConnection++) {
                    if (currentLayer == 0) {
                        derivWeights[currentLayer][currentNode][currentConnection] = 2 * input[currentConnection] * (weightedAverages[currentLayer][currentNode] - answers[currentNode]);
                    }
                    else {

                    }
                }
            }
        }
    }
    public static void gradientDescent (double[][][] weights, double[] constants, double[][][] derivWeights, double[] derivConstants, double learningRate) {
        for (int currentLayer = 0; currentLayer < weights.length; currentLayer++) {
            constants[currentLayer]-=learningRate*derivConstants[currentLayer];
            for (int currentNode = 0; currentNode < weights[currentLayer].length; currentNode++) {
                for (int currentConnection = 0; currentConnection < weights[currentLayer][currentNode].length; currentConnection++) {
                    if (currentLayer == 0) {
                        weights[currentLayer][currentNode][currentConnection]-=learningRate*derivWeights[currentLayer][currentNode][currentConnection];
                    }
                    else {
                        /**do stuff for other layers**/
                    }
                }
            }
        }
    }
    public static double getCost (double[][] activations, double[] correctAnswer) {//get cost of the current weights and constants for the current training example
        double cost = 0;
        for (int currentNode = 0; currentNode < correctAnswer.length; currentNode++) {
            cost+=Math.pow(activations[activations.length-1][currentNode]-correctAnswer[currentNode],2);
        }
        return cost;
    }
    public static double reLU (double inputs) {
        return Math.max(0,inputs);
    }
}