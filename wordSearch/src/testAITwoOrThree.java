import java.util.*;
import java.io.*;

public class testAITwoOrThree {
    public static void main(String[] args) {

        double[][] trainingData = {
                {0,0,0,0},{0,0,0,1},{0,0,1,0},{0,0,1,1},
                {0,1,0,0},{0,1,0,1},{0,1,1,0},{0,1,1,1},
                {1,0,0,0},{1,0,0,1},{1,0,1,0},{1,0,1,1},
                {1,1,0,0},{1,1,0,1},{1,1,1,0},{1,1,1,1}}; //outer group is each case, inner group is each input node
        double[][] trainingAnswers = {
                {0,0},{0,0},{0,0},{0,1},
                {0,0},{0,1},{0,1},{1,0},
                {0,0},{0,1},{0,1},{1,0},
                {0,1},{1,0},{1,0},{0,0}}; //outer group is each case, inner group is each output node

        double[][] testingData = {
                {0,0,0,0},{0,0,0,1},{0,0,1,0},{0,0,1,1},
                {0,1,0,0},{0,1,0,1},{0,1,1,0},{0,1,1,1},
                {1,0,0,0},{1,0,0,1},{1,0,1,0},{1,0,1,1},
                {1,1,0,0},{1,1,0,1},{1,1,1,0},{1,1,1,1}};
        double[][] testingAnswers = {
                {0,0},{0,0},{0,0},{0,1},
                {0,0},{0,1},{0,1},{1,0},
                {0,0},{0,1},{0,1},{1,0},
                {0,1},{1,0},{1,0},{0,0}};

        int trainingIterations = 10000;
        double learningRate = 0.0005;

        //structure of network
        int numInputs = 4;
        int[] nodeStructure = {15,15,15,15,2};

        double[][][] weights = {/**change if number of layers changes**/
                randomizeArray(new double[nodeStructure[0]][numInputs]),
                randomizeArray(new double[nodeStructure[1]][nodeStructure[0]]),
                randomizeArray(new double[nodeStructure[2]][nodeStructure[1]]),
                randomizeArray(new double[nodeStructure[3]][nodeStructure[2]]),
                randomizeArray(new double[nodeStructure[4]][nodeStructure[3]])};//outer groups for each layer (starting from layer after input), middle for each node, inner for each connection
        double[] constants = randomizeArray(new double[nodeStructure.length]); //constants of each layer (starting from input layer)

        double[][] weightedAverages =  {/**change if number of layers changes**/
                new double[nodeStructure[0]],
                new double[nodeStructure[1]],
                new double[nodeStructure[2]],
                new double[nodeStructure[3]],
                new double[nodeStructure[4]]};//outer is each layer, inner is each node
        double[][] activations =  copyOf(weightedAverages);//weightedAverages passed through reLU
        double[][] derivActivations = copyOf(weightedAverages);

        double[][][] derivWeights = copyOf(weights);

        double[] derivConstants = copyOf(constants);



        //TRAINING
        for (int currentIteration = 0; currentIteration < trainingIterations; currentIteration++) {
            System.out.println("***************ITERATION "+currentIteration+"***************\n");
            for (int currentCase = 0; currentCase < trainingData.length; currentCase++) {
                System.out.println("-----TRAINING CASE "+currentCase+"-----\n");
                //forward propogation
                //System.out.println("Forward Propogation: ");
                forwardPropogation(trainingData[currentCase], weights, constants, weightedAverages, activations);
                //System.out.println();
                /*for (int currentLayer = 0; currentLayer < activations.length; currentLayer++) {
                    System.out.println("constant in Layer "+currentLayer+": " + constants[currentLayer]);
                    for (int currentNode = 0; currentNode < activations[currentLayer].length; currentNode++) {//run through each node of the current layer
                        if (currentLayer == 0) {
                            for (int currentConnection = 0; currentConnection < trainingData[currentCase].length; currentConnection++) {//go through each node of the input
                                System.out.println("weight in Layer " + currentLayer + " Node " + currentNode + " Connection " + currentConnection + ": " + weights[currentLayer][currentNode][currentConnection]+" ");
                            }
                        }
                        else {//if current layer != 0
                            for (int currentConnection = 0; currentConnection < weights[currentLayer][currentNode].length; currentConnection++) {//go through each node of the input
                                System.out.println("weight in Layer " + currentLayer + " Node " + currentNode + " Connection " + currentConnection + ": " + weights[currentLayer][currentNode][currentConnection]+" ");
                            }
                        }
                    }
                    System.out.println();
                }*/

                //calculate cost
                System.out.println("Cost Before: " + getCost(activations, trainingAnswers[currentCase])+"\n");//checking initial cost

                //backward propogation
                //System.out.println("Back Propogation: ");
                backPropogation(trainingData[currentCase], weights, weightedAverages, activations, derivWeights, derivConstants, derivActivations, trainingAnswers[currentCase]);
                /*for (int currentLayer = 0; currentLayer < activations.length; currentLayer++) {
                    System.out.println("derivConstant in Layer "+currentLayer+": " + derivConstants[currentLayer]);
                    for (int currentNode = 0; currentNode < activations[currentLayer].length; currentNode++) {//run through each node of the current layer
                        if (currentLayer == 0) {
                            for (int currentConnection = 0; currentConnection < trainingData[currentCase].length; currentConnection++) {//go through each node of the input
                                System.out.println("derivWeight in Layer " + currentLayer + " Node " + currentNode + " Connection " + currentConnection + ": " + derivWeights[currentLayer][currentNode][currentConnection]+" ");
                            }
                        }
                        else {//if current layer != 0
                            for (int currentConnection = 0; currentConnection < weights[currentLayer][currentNode].length; currentConnection++) {//go through each node of the input
                                System.out.println("derivWeight in Layer " + currentLayer + " Node " + currentNode + " Connection " + currentConnection + ": " + derivWeights[currentLayer][currentNode][currentConnection]+" ");
                            }
                        }
                    }
                    System.out.println();
                }*/

                //gradient descent
                //System.out.println("Gradient Descent: ");
                gradientDescent(weights, constants, derivWeights, derivConstants, learningRate);

                /*forwardPropogation(trainingData[currentCase], weights, constants, weightedAverages, activations);//checking effect of gradient descent
                System.out.println();*/
                System.out.println("Cost After: " + getCost(activations, trainingAnswers[currentCase]) + "\n");//checking later cost
            }
        }

        //TESTING
        System.out.println("TESTING");

        //display weights and bias for current testing case
        for (int currentLayer = 0; currentLayer < activations.length; currentLayer++) {
            System.out.println("constant in Layer "+currentLayer+": " + constants[currentLayer]);
            for (int currentNode = 0; currentNode < activations[currentLayer].length; currentNode++) {//run through each node of the current layer
                if (currentLayer == 0) {
                    for (int currentConnection = 0; currentConnection < numInputs; currentConnection++) {//go through each node of the input
                        System.out.println("weight in Layer " + currentLayer + " Node " + currentNode + " Connection " + currentConnection + ": " + weights[currentLayer][currentNode][currentConnection]+" ");
                    }
                }
                else {//if current layer != 0
                    for (int currentConnection = 0; currentConnection < weights[currentLayer][currentNode].length; currentConnection++) {//go through each node of the input
                        System.out.println("weight in Layer " + currentLayer + " Node " + currentNode + " Connection " + currentConnection + ": " + weights[currentLayer][currentNode][currentConnection]+" ");
                    }
                }
            }
            System.out.println();
        }

        double averageWrongness = 0;
        for (int currentCase = 0; currentCase < testingData.length; currentCase++) {
            forwardPropogation(testingData[currentCase],weights, constants, weightedAverages, activations);

            for (int currentNode = 0; currentNode < activations[activations.length-1].length; currentNode++) {//go through all nodes of the last layer
                System.out.println("Case "+currentCase+" Layer "+(activations.length-1)+" Node "+currentNode+" Activation: "+activations[activations.length-1][currentNode]);//print out the activations of the last layer
            }

            for (int currentNode = 0; currentNode < activations[activations.length-1].length; currentNode++) {
                averageWrongness += Math.abs(activations[activations.length - 1][currentNode] - testingAnswers[currentCase][currentNode]);
            }
        }
        averageWrongness/= testingData.length*activations[activations.length-1].length;
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
            forwardPropogateLayer(activations[currentLayer-1], weights[currentLayer], constants[currentLayer], currentLayer, weightedAverages, activations);
        }
        /*for (int currentNode = 0; currentNode < activations[activations.length-1].length; currentNode++) {//go through all nodes of the last layer
            System.out.println("Layer "+(activations.length-1)+" Node "+currentNode+" Activation: "+activations[activations.length-1][currentNode]);//print out the activations of the last layer
        }*/
    }
    public static void backPropogation (double[] input, double[][][] weights, double[][] weightedAverages, double[][] activations, double[][][] derivWeights, double[] derivConstants, double[][] derivActivations, double[] answers) {
        for (int currentLayer = weightedAverages.length-1; currentLayer >= 0; currentLayer--) {//run through each layer backwards starting from last layer
            for (int currentNode = 0; currentNode < weightedAverages[currentLayer].length; currentNode++) {//go through each node of the current layer
                //SOLVING DERIV ACTIVATIONS
                if (currentLayer == weightedAverages.length-1) {//if on the last layer
                    derivActivations[currentLayer][currentNode] = 2*(activations[currentLayer][currentNode]-answers[currentNode]);
                }
                else {//if not on last layer
                    derivActivations[currentLayer][currentNode] = 0; //make sure derivActivations isn't accumulating btwn each training case
                    for (int currentNodeOfNextLayer = 0; currentNodeOfNextLayer < weightedAverages[currentLayer+1].length; currentNodeOfNextLayer++) {//go through each node of the next layer
                        if (weightedAverages[currentLayer+1][currentNodeOfNextLayer]!=0) {//if the weighted average != 0
                            derivActivations[currentLayer][currentNode] += derivActivations[currentLayer + 1][currentNodeOfNextLayer] * weights[currentLayer+1][currentNodeOfNextLayer][currentNode];
                        }
                    }
                }

                //SOLVING DERIV WEIGHTS/CONSTANTS
                if (weightedAverages[currentLayer][currentNode] <= 0) {//if the weighted average of the current node is less than or equal to 0
                    derivConstants[currentLayer] = 0;//derivative of the current layer constant for the current node equals 0
                    for (int currentConnection = 0; currentConnection < weights[currentLayer][currentNode].length; currentConnection++) {
                        derivWeights[currentLayer][currentNode][currentConnection] = 0;//derivative of the weight from the current connection of the previous layer to the current node of the next layer equals 0
                    }
                }
                else {//if the weighted average is greater than 0
                    derivConstants[currentLayer] = derivActivations[currentLayer][currentNode];
                    for (int currentConnection = 0; currentConnection < weights[currentLayer][currentNode].length; currentConnection++) {
                        if (currentLayer == 0) {
                            derivWeights[currentLayer][currentNode][currentConnection] = 2 * input[currentConnection] * derivActivations[currentLayer][currentNode];
                        }
                        else {//if current layer isn't 0
                            derivWeights[currentLayer][currentNode][currentConnection] = activations[currentLayer-1][currentConnection] *  derivActivations[currentLayer][currentNode];
                        }
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
                    weights[currentLayer][currentNode][currentConnection]-=learningRate*derivWeights[currentLayer][currentNode][currentConnection];
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
    public static double[] randomizeArray (double[] arr) {
        for (int a = 0; a < arr.length; a++) {
            arr[a] = Math.random()*2-1;
        }
        return arr;
    }
    public static double[][] randomizeArray (double[][] arr) {
        for (int a = 0; a < arr.length; a++) {
            arr[a] = randomizeArray(arr[a]);
        }
        return arr;
    }
    public static double[] copyOf (double[] arr) {
        double[] ans = new double[arr.length];
        for (int a = 0; a < arr.length; a++) {
            ans[a] = arr[a];
        }
        return ans;
    }
    public static double[][] copyOf (double[][] arr) {
        double[][] ans = new double[arr.length][];
        for (int a = 0; a < arr.length; a++) {
            ans[a] = copyOf(arr[a]);
        }
        return ans;
    }
    public static double[][][] copyOf (double[][][] arr) {
        double[][][] ans = new double[arr.length][][];
        for (int a = 0; a < arr.length; a++) {
            ans[a] = copyOf(arr[a]);
        }
        return ans;
    }
}