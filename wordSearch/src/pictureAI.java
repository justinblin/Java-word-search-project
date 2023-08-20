import java.util.*;
import java.io.*;

public class pictureAI {
    public static void main(String[] args) throws IOException{

        boolean learning = false; /**if training the AI, set true; if using the AI, set false**/
        boolean findingMetaVariables = false;/**if looking for regularization and learning rate, set true; if just single use, set false**/

        /**USE VARIABLES AND META-VARIABLES**/

        /**change when add more use pictures in "inputs" folder**/
        int numYLetters = 14;
        int numXLetters  = 12;

        int trainingIterations = 700;//700

        double learningRate = 0.0023;//0.0023

        double[] learningRateChange = {0.00006, 0.00006, 0.000005};//start, end, iteration size

        double regularizationConstant = 0.000115;//0.00011
        double regularizationIncrease = 1.1;//1.1
        /**nan cases*/
        //high regularization + high learning rate/starting weights too high => nan cost, cost becomes very big, rounds to infinity
        //high learning rate + low regularization => nan cost, unable to converge, cost diverges to infinity

        /**underfit - training cost high, low testing accuracy*/
        //high regularization (not enough for nan) => activations all 0
        //low learning rate => random activations, around the same
        //outputs same thing for all or most cases, pushes to constant
        //maybe not learned the pattern yet? try increasing training iterations

        /**overfit - training cost low, low testing accuracy*/
        //high learning rate (not enough for nan) + low regularization => testing activations unrelated to answer

        //(0.003, 0.00015, 1.12) --- 91.15
        //(0.0035, 0.00015, 1.12) --- 95
        //(0.0039, 0.00015, 1.12) --- 93.08, 91.15, 89.62, 91.15
        //(0.0039, 0.000151, 1.1205) --- 92.69
        //(0.0039, 0.000151, 1.1205) --- 90.38
        //(0.0039, 0.000152, 1.12) --- 87.31
        //(0.0039, 0.000155, 1.12) --- 92.31
        //(0.0039, 0.00016, 1.15) --- 84.62
        //(0.0039, 0.0002, 1.5) --- 90.38, consistent mistaking some letter 1 for letter 2
        //(0.0039, 0.0005, 1.5) --- 89.62, some mistaking some letter 1 for letter 2, some overregularization

        double[] regularizationChange = {0.000105, 0.000115, 0.000005};//start, end, iteration size
        /**--------------------*--------------------**/

        /**STRUCTURE OF THE NETWORK**/
        int numInputs = 625;
        int[] nodeStructure = {100,100,100,100,26};

        double[][][] weights = new double[nodeStructure.length][][];//outer groups for each layer (starting from layer after input), middle for each node, inner for each connection
        weights[0] = randomizeArray(new double[nodeStructure[0]][numInputs]);
        for (int currentLayer = 1; currentLayer < nodeStructure.length; currentLayer++) {
            weights[currentLayer] = randomizeArray(new double[nodeStructure[currentLayer]][nodeStructure[currentLayer-1]]);
        }
        double[] constants = randomizeArray(new double[nodeStructure.length]); //constants of each layer (starting from input layer)

        double[][] weightedAverages =  new double[nodeStructure.length][];//outer is each layer, inner is each node
        for (int currentLayer = 0; currentLayer < nodeStructure.length; currentLayer++) {
            weightedAverages[currentLayer] = new double[nodeStructure[currentLayer]];
        }

        double[][] activations =  copyOf(weightedAverages);//weightedAverages passed through reLU
        double[][] derivActivations = copyOf(weightedAverages);

        double[][][] derivWeights = copyOf(weights);

        double[] derivConstants = copyOf(constants);
        /**--------------------*--------------------**/


        if (learning == true) { //if teaching the AI
            Scanner scan = new Scanner(System.in);
            System.out.println("findingMetaVariables: " + findingMetaVariables+"\n*****-----enter anything to continue-----*****");
            scan.next();
            if (findingMetaVariables) {
                //FINDING META-VARIABLES
                double bestCorrect = 0;
                double bestLearningRate = 0;
                double bestRegularization = 0;
                ArrayList<double[]> currentCorrects = new ArrayList<double[]>();

                for (double currentLearningRate = learningRateChange[0]; currentLearningRate < learningRateChange[1] + learningRateChange[2] / 2; currentLearningRate += learningRateChange[2]) {
                    //System.out.println(regularizationChange[0]+" "+regularizationChange[1]+" "+regularizationChange[2]);//TROUBLESHOOTING
                    for (double currentRegularization = regularizationChange[0]; currentRegularization < regularizationChange[1] + regularizationChange[2] / 2; currentRegularization += regularizationChange[2]) {
                        System.out.println("currentLearningRate: " + currentLearningRate + " currentRegularization: " + currentRegularization);
                        double currentCorrect = trainingTestingAI(trainingIterations, currentLearningRate, currentRegularization, regularizationIncrease, weights, derivWeights, constants, derivConstants, weightedAverages, activations, derivActivations);
                        System.out.println("currentLearningRate: " + currentLearningRate + " currentRegularization: " + currentRegularization + "\n");

                        double[] tempCorrects = {currentLearningRate, currentRegularization, currentCorrect};
                        currentCorrects.add(tempCorrects);

                        if (bestCorrect < currentCorrect) {
                            bestLearningRate = currentLearningRate;
                            bestRegularization = currentRegularization;
                            bestCorrect = currentCorrect;
                        }
                    }
                }
                System.out.println("\nBest Learning Rate: " + bestLearningRate + " Best Regularization: " + bestRegularization + " Best Correct: " + bestCorrect + "\n");

                for (int currentCase = 0; currentCase < currentCorrects.size(); currentCase++) {
                    System.out.println("Current Learning Rate: " + currentCorrects.get(currentCase)[0] + " Current Regularization: " + currentCorrects.get(currentCase)[1] + " Current Correct: " + currentCorrects.get(currentCase)[2]);
                }
            }
            else {
                //FOR SINGLE USE TRAINING/TESTING
                trainingTestingAI(trainingIterations, learningRate, regularizationConstant, regularizationIncrease, weights, derivWeights, constants, derivConstants, weightedAverages, activations, derivActivations);
            }
        }
        if (learning == false) { //if using the AI
            Scanner scan = new Scanner(new File("C:\\Users\\justi\\OneDrive\\Desktop\\Justin Lin\\Java word search project\\weightsAndConstants.txt"));
            for (int currentLayer = 0; currentLayer < weights.length; currentLayer++) {//reads through all weights
                for (int currentNode = 0; currentNode < weights[currentLayer].length; currentNode++) {
                    for (int currentConnection = 0; currentConnection < weights[currentLayer][currentNode].length; currentConnection++) {
                        weights[currentLayer][currentNode][currentConnection] = scan.nextDouble();//records previous weights to current matrix
                    }
                }
            }
            for (int currentLayer = 0; currentLayer < weights.length; currentLayer++) {//reads through all constants
                constants[currentLayer] = scan.nextDouble();//records constants to current array
            }


            //PREDICTS THE ANSWER FOR ALL PICTURES IN "INPUTS"
            String answer = using(numXLetters, numYLetters, activations, weights, constants, weightedAverages);
            System.out.println(answer);
            FileWriter writer = new FileWriter("C:\\Users\\justi\\OneDrive\\Desktop\\Justin Lin\\Java word search project\\wordSearch\\wordSearchSolver.txt");
            writer.write(numYLetters+" "+numXLetters+"\n"+answer+"\n");
            writer.close();
        }
    }

    /**--------------------*--------------------**/

    public static double trainingTestingAI (int trainingIterations, double learningRate, double regularizationConstant, double regularizationIncrease, double[][][] weights, double[][][] derivWeights, double[] constants, double[] derivConstants, double[][] weightedAverages, double[][] activations, double[][] derivActivations) throws IOException {
        //turns the training and testing pictures into matrices, then trains and tests the AI
        //returns percent correct of testing set with current conditions

        /**TAKES ALL OF THE TRAINING AND TESTING PICTURES AND TURNS THEM INTO 4 MATRICES**/
        double[][] trainingData = new double[0][0];
        double[][] trainingAnswers = new double[0][0];
        double[][] testingData = new double[0][0];
        double[][] testingAnswers = new double[0][0];


        String trainingFileDirectory = "C:\\Users\\justi\\OneDrive\\Desktop\\Justin Lin\\Java word search project\\Java AI Images\\Letters\\training";
        int[] numOfTrainingPicturesForEachLetter = new int[26];
        //number of training pictures for each letter

        for (int currentLetter = 0; currentLetter < 26; currentLetter++) {//for each letter in the training folder
            char currentCharOfLetter = (char) (65+currentLetter);
            numOfTrainingPicturesForEachLetter[currentLetter] = new File(trainingFileDirectory + "\\" + currentCharOfLetter).list().length;
        }


        String testingFileDirectory = "C:\\Users\\justi\\OneDrive\\Desktop\\Justin Lin\\Java word search project\\Java AI Images\\Letters\\testing";
        trainingData = pictureToMatrix.bundleInputsForAI(numOfTrainingPicturesForEachLetter, 0, false); //outer group is each case, inner group is each input node
        trainingAnswers = pictureToMatrix.bundleAnswersForAI(numOfTrainingPicturesForEachLetter); //outer group is each case, inner group is each output node

        int[] numOfTestingPicturesForEachLetter = new int[26];
        //number of testing pictures for each letter

        for (int currentLetter = 0; currentLetter < 26; currentLetter++) {//for each letter in the testing folder
            char currentCharOfLetter = (char) (65+currentLetter);
            numOfTestingPicturesForEachLetter[currentLetter] = new File(testingFileDirectory + "\\" + currentCharOfLetter).list().length;
        }

        testingData = pictureToMatrix.bundleInputsForAI(numOfTestingPicturesForEachLetter, 1, false);
        testingAnswers = pictureToMatrix.bundleAnswersForAI(numOfTestingPicturesForEachLetter);
        /**--------------------*--------------------**/


        training(trainingIterations, learningRate, regularizationConstant, regularizationConstant, regularizationIncrease, trainingData, trainingAnswers, weights, constants, weightedAverages, activations, derivWeights, derivConstants, derivActivations);
        return testing(testingData, testingAnswers, weights, constants, weightedAverages, activations); //return percent of testing cases correct
    }

    public static void training (double trainingIterations, double learningRate, double regularizationConstant, double initialRegularization, double regularizationIncrease, double[][] trainingData, double[][] trainingAnswers, double[][][] weights, double[] constants, double[][] weightedAverages, double[][] activations, double[][][] derivWeights, double[] derivConstants, double[][] derivActivations) throws IOException {
        //TRAINING
        for (int currentIteration = 0; currentIteration < trainingIterations; currentIteration++) {
            if ((currentIteration+100)%100-99==0||currentIteration==0) {
                System.out.println("***************ITERATION " + currentIteration + "***************\n");
            }

            double averageCostBeforeCurrentIteration = 0;
            double averageRegularization = 0;

            for (int currentCase = 0; currentCase < trainingData.length; currentCase++) {
                averageCostBeforeCurrentIteration += getCost(activations, trainingAnswers[currentCase], weights, regularizationConstant);
                averageRegularization += regularizationConstant;

                if ((currentIteration+100)%100-99==0||currentIteration==0) {
                    System.out.println("-----TRAINING CASE " + currentCase + "-----\n");
                }
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
                if ((currentIteration+100)%100-99==0||currentIteration==0) {
                    System.out.println("Cost Before: " + getCost(activations, trainingAnswers[currentCase], weights, regularizationConstant) + "\n");//checking initial cost
                }

                //backward propogation
                //System.out.println("Back Propogation: ");
                backPropogation(trainingData[currentCase], weights, weightedAverages, activations, derivWeights, derivConstants, derivActivations, trainingAnswers[currentCase], regularizationConstant);
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

                //change regularization after each training case
                regularizationConstant=/*(double)currentIteration/trainingIterations**/(regularizationIncrease*initialRegularization-(regularizationIncrease-1)*initialRegularization*getCost(activations, trainingAnswers[currentCase], weights, regularizationConstant));

                if ((currentIteration+100)%100-99==0||currentIteration==0) {
                    System.out.println("Cost After: " + getCost(activations, trainingAnswers[currentCase], weights, regularizationConstant) + "\n");//checking later cost
                }
            }

            averageCostBeforeCurrentIteration/=trainingData.length;
            averageRegularization/=trainingData.length;

            if ((currentIteration+100)%100-99==0||currentIteration==0) {
                System.out.println("***************ITERATION " + currentIteration + "***************\n" +
                "averageCostBefore: "+averageCostBeforeCurrentIteration+"\naverageRegularization: "+averageRegularization+"\n");
            }
        }
    }

    public static double testing (double[][] testingData, double[][] testingAnswers, double[][][] weights, double[] constants, double[][] weightedAverages, double[][] activations) throws IOException {
        //TESTING
        System.out.println("TESTING");

        //display weights and bias for current testing case
        /*for (int currentLayer = 0; currentLayer < activations.length; currentLayer++) {
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
        }*/

        double averageWrongness = 0;
        double percentCorrect = 0;
        for (int currentCase = 0; currentCase < testingData.length; currentCase++) {
            forwardPropogation(testingData[currentCase],weights, constants, weightedAverages, activations);

            int nodeIndexOfAnswerOfCurrentCase = 0;
            int nodeIndexOfGuessOfCurrentCase = 0;
            for (int currentNode = 0; currentNode < activations[activations.length-1].length; currentNode++) {//go through all nodes of the last layer
                System.out.println("Case "+currentCase+" Layer "+(activations.length-1)+" Node "+currentNode+" "+(char)(65+currentNode)+" Activation: "+activations[activations.length-1][currentNode]);//print out the activations of the last layer
                averageWrongness += Math.abs(activations[activations.length - 1][currentNode] - testingAnswers[currentCase][currentNode]);

                if (activations[activations.length-1][currentNode] > activations[activations.length-1][nodeIndexOfGuessOfCurrentCase]) {
                    nodeIndexOfGuessOfCurrentCase = currentNode;//find the biggest activation value for the current case
                }
                if (testingAnswers[currentCase][currentNode]==1) {
                    nodeIndexOfAnswerOfCurrentCase = currentNode;//find the index of the node for the answer for the current case
                }
            }

            if (nodeIndexOfGuessOfCurrentCase == nodeIndexOfAnswerOfCurrentCase) {
                percentCorrect++;
            }
            else {
                System.out.println("-----WRONG: " + (char)(nodeIndexOfGuessOfCurrentCase+65) + " RIGHT: " + (char)(nodeIndexOfAnswerOfCurrentCase+65) + "-----");
            }

           System.out.println();
        }
        averageWrongness/= testingData.length*activations[activations.length-1].length;
        percentCorrect/= testingData.length;
        System.out.println(averageWrongness+" average difference from actual\n"+percentCorrect+" percent correct");

        //PUTTING WEIGHTS INTO A TEXT FILE FOR EASY USE LATER
        FileWriter writer = new FileWriter("C:\\Users\\justi\\OneDrive\\Desktop\\Justin Lin\\Java word search project\\weightsAndConstants.txt");
        for (int currentLayer = 0; currentLayer < weights.length; currentLayer++) {
            for (int currentNode = 0; currentNode < weights[currentLayer].length; currentNode++) {
                for (int currentConnection = 0; currentConnection < weights[currentLayer][currentNode].length; currentConnection++) {
                    writer.write(weights[currentLayer][currentNode][currentConnection]+" ");
                }
            }
        }

        for (int currentLayer = 0; currentLayer < weights.length; currentLayer++) {
            writer.write(constants[currentLayer]+" ");
        }
        writer.close();

        //RETURN THE NUMBER OF CORRECT TESTING CASES
        return percentCorrect;
    }

    public static String using (int numXLetters, int numYLetters, double[][] activations, double[][][] weights, double[] constants, double[][] weightedAverages) throws IOException {

        int currentPicture = 1; /**assuming there's only one wordsearch right now, may change later*/
        String answers = "";
        double[][] input = pictureToMatrix.makeMatrixFromWordSearch(numXLetters, numYLetters, currentPicture);//split the wordsearch into individual letters

        for (int currentLetter = 0; currentLetter < input.length; currentLetter++) {
            forwardPropogation(input[currentLetter], weights, constants, weightedAverages, activations); //void method, changes activations matrix


            int nodeIndexOfGuessOfCurrentCase = 0;
            int nodeIndexOfSecondGuessOfCurrentCase = 0;

            System.out.println();
            for (int currentNode = 0; currentNode < activations[activations.length - 1].length; currentNode++) {//go through all nodes of the last layer
                System.out.println("Case " + currentPicture + " XLetter: " + currentLetter%numXLetters + " YLetter: " + currentLetter/numXLetters + " Node " + currentNode +" "+(char)(65+currentNode)+" Activation: " + activations[activations.length - 1][currentNode]);//print out the activations of the last layer

                if (activations[activations.length - 1][currentNode] > activations[activations.length - 1][nodeIndexOfGuessOfCurrentCase]) {
                    nodeIndexOfSecondGuessOfCurrentCase = nodeIndexOfGuessOfCurrentCase;//old biggest is now second biggest
                    nodeIndexOfGuessOfCurrentCase = currentNode;//find the biggest activation value for the current case
                }
                else if (activations[activations.length - 1][currentNode] > activations[activations.length - 1][nodeIndexOfSecondGuessOfCurrentCase]) {
                    nodeIndexOfSecondGuessOfCurrentCase = currentNode;//find the second biggets activation value
                }
            }
            char currentGuess = (char) (nodeIndexOfGuessOfCurrentCase + 65);
            System.out.println("\nBest Guess: " + currentGuess + "\n");

            if (activations[activations.length - 1][nodeIndexOfGuessOfCurrentCase]-activations[activations.length - 1][nodeIndexOfSecondGuessOfCurrentCase]>0.35) {//if "confidence" difference btwn best and second guess > 0.35
                answers += currentGuess;
            }
            else if (nodeIndexOfGuessOfCurrentCase!=nodeIndexOfSecondGuessOfCurrentCase){//if unsure btwn guess 1 and 2, and they're not both the same
                char secondGuess = (char) (nodeIndexOfSecondGuessOfCurrentCase + 65);
                System.out.println("Second Guess: " + secondGuess+"\n\nAnswer?"); //ask for answer
                Scanner scan = new Scanner(System.in);
                answers+=scan.next();
            }
            else {//exception with A, since first and second guess are the same, finding the "confidence" difference doesn't work
                answers+="A";
            }
        }

        return answers;
    }

    public static void forwardPropogateLayer (double[] input, double[][] layerWeights, double layerConstant, int currentLayer, double[][] weightedAverages, double[][] activations) throws IOException {//finding prediction for our given weights and inputs, one layer
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
    public static void forwardPropogation (double[] input, double[][][] weights, double[] constants, double[][] weightedAverages, double[][] activations) throws IOException {
        forwardPropogateLayer(input, weights[0], constants[0],0, weightedAverages, activations);//forward prop input layer (layer 0)
        for (int currentLayer = 1; currentLayer < activations.length; currentLayer++) {//go through each layer after input (layer 1-activations.length)
            forwardPropogateLayer(activations[currentLayer-1], weights[currentLayer], constants[currentLayer], currentLayer, weightedAverages, activations);
        }
        /*for (int currentNode = 0; currentNode < activations[activations.length-1].length; currentNode++) {//go through all nodes of the last layer
            System.out.println("Layer "+(activations.length-1)+" Node "+currentNode+" Activation: "+activations[activations.length-1][currentNode]);//print out the activations of the last layer
        }*/
    }
    public static void backPropogation (double[] input, double[][][] weights, double[][] weightedAverages, double[][] activations, double[][][] derivWeights, double[] derivConstants, double[][] derivActivations, double[] answers, double regularizationConstant) throws IOException {
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
                        else if (currentLayer == weightedAverages.length-1) {
                            derivWeights[currentLayer][currentNode][currentConnection] = regularizationConstant*weights[currentLayer-1][currentNode][currentConnection];//add regularization last layer
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
    public static double getCost (double[][] activations, double[] correctAnswer, double[][][] weights, double regularizationConstant) {//get cost of the current weights and constants for the current training example
        double cost = 0;
        for (int currentNode = 0; currentNode < correctAnswer.length; currentNode++) {//go thru each node of the last layer
            cost+=Math.pow(activations[activations.length-1][currentNode]-correctAnswer[currentNode],2);//sum up the squared differences btwn prediction and actual

            //adding regularization
            for (int currentConnection = 0; currentConnection < activations[activations.length-2].length; currentConnection++) {//go thru each node of second to last layer
                cost+=regularizationConstant*Math.abs(weights[activations.length-1][currentNode][currentConnection]);//add regularization part
            }
        }
        return cost;
    }
    public static double reLU (double inputs) {
        return Math.max(0,inputs);
    }
    public static double[] randomizeArray (double[] arr) {
        for (int a = 0; a < arr.length; a++) {
            arr[a] = Math.random()*0.2-0.1;
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