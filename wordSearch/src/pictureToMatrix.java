import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;

public class pictureToMatrix {
    public static void main(String[] args) throws IOException {

        /**FOR DEMONSTRATION PURPOSES ONLY, DELETE THE COMMENT THINGY IN FRONT OF THE REST OF MAIN TO USE IT, COMMENT OUT THE LINE BELOW
         *
         * FOLLOW INSTRUCTIONS AT L135
         */
        //makeArrayFromPicture("A",1,3,1,1,0,0,true);

        String trainingFileDirectory = "C:\\Users\\justi\\OneDrive\\Desktop\\Justin Lin\\Java word search project\\Java AI Images\\Letters\\training";
        int[] numOfTrainingPicturesForEachLetter = new int[26];
        //number of training pictures for each letter

        for (int currentLetter = 0; currentLetter < 26; currentLetter++) {//for each letter in the training folder
            char currentCharOfLetter = (char) (65+currentLetter);
            numOfTrainingPicturesForEachLetter[currentLetter] = new File(trainingFileDirectory + "\\" + currentCharOfLetter).list().length;
        }

        //bundleInputsForAI(numOfTrainingPicturesForEachLetter, 0, true);



        String testingFileDirectory = "C:\\Users\\justi\\OneDrive\\Desktop\\Justin Lin\\Java word search project\\Java AI Images\\Letters\\testing";

        int[] numOfTestingPicturesForEachLetter = new int[26];
        //number of testing pictures for each letter

        for (int currentLetter = 0; currentLetter < 26; currentLetter++) {//for each letter in the testing folder
            char currentCharOfLetter = (char) (65+currentLetter);
            numOfTestingPicturesForEachLetter[currentLetter] = new File(testingFileDirectory + "\\" + currentCharOfLetter).list().length;
        }

        bundleInputsForAI(numOfTestingPicturesForEachLetter, 1, true);

        //makeMatrixFromWordSearch(5,1, 1);

        //checking inputs and answers for each picture
        /*double[][] inputs = bundleInputsForAI(numOfTrainingPicturesForEachLetter, true);
        double[][] answers = bundleAnswersForAI(numOfTrainingPicturesForEachLetter);
        for (int currentPicture = 0; currentPicture < inputs.length; currentPicture++) {
            for (int a = 0; a < inputs[currentPicture].length; a++) {
                if (a%25==0) {
                    System.out.println();
                }
                System.out.print((int)inputs[currentPicture][a] + " ");
            }
            System.out.println();
            for (int a = 0; a < answers[currentPicture].length; a++) {
                System.out.print(answers[currentPicture][a] + " ");
            }
            System.out.println();
        }*/
    }
    public static double[][] bundleInputsForAI (int[] numOfPicturesForEachLetter, int training, boolean display) throws IOException {
        int totalPictures = 0;
        for (int a = 0; a < numOfPicturesForEachLetter.length; a++) {
            totalPictures+=numOfPicturesForEachLetter[a];
        }

        double[][] aiInput = new double[totalPictures][625];//outside -> number of test cases, inside -> number of input nodes per case
        int totalPictureCounter = 0;

        for (int currentLetterCounter = 65; currentLetterCounter < 91; currentLetterCounter++) {//for each letter
            char currentCharacter = (char)currentLetterCounter;//ascii 65-90 inclusive
            String currentLetter = String.valueOf(currentCharacter);//convert int/char to string

            //System.out.println(currentLetter);/**troubleshooting*/

            for (int currentPicture = 1; currentPicture < numOfPicturesForEachLetter[currentCharacter - 65] + 1; currentPicture++) {//go through each picture of the current letter

                aiInput[totalPictureCounter] = pictureAI.copyOf(makeArrayFromPicture(currentLetter, currentPicture, training, 1, 1, 0, 0, display));
                totalPictureCounter++;
                //System.out.println();
            }
        }

        return aiInput;
    }
    public static double[][] bundleAnswersForAI (int[] numOfPicturesForEachLetter) throws IOException {
        int totalPictures = 0;
        for (int a = 0; a < numOfPicturesForEachLetter.length; a++) {
            totalPictures+=numOfPicturesForEachLetter[a];
        }

        double[][] aiAnswers = new double[totalPictures][26];//outside -> number of test cases, inside -> number of input nodes per case
        int totalPictureCounter = 0;

        for (int currentLetterCounter = 65; currentLetterCounter < 91; currentLetterCounter++) {//for each letter
            char currentCharacter = (char)currentLetterCounter;//ascii 65-90 inclusive

            for (int currentPicture = 1; currentPicture < numOfPicturesForEachLetter[currentCharacter - 65] + 1; currentPicture++) {//go through each picture of the current letter

                aiAnswers[totalPictureCounter][currentLetterCounter-65] = 1;
                totalPictureCounter++;
            }
        }

        return aiAnswers;
    }

    //splits the word search into a picture of each letter and runs makeArrayFromPicture on it,
    //then stores it in a matrix (outside: each letter, inside: the values of each section in an individual letter)
    public static double[][] makeMatrixFromWordSearch (int numXLetters, int numYLetters, int currentPicture) throws IOException { //when using the ai
        double[][] pictureMatrix = new double[numXLetters*numYLetters][625];//number of letters * num of inputs in each letter
        System.out.println(numXLetters*numYLetters + " " + numXLetters + " " + numYLetters);/**trouble shooting*/
        for (int currentYPicture = 0; currentYPicture < numYLetters; currentYPicture++) {
            for (int currentXPicture = 0; currentXPicture < numXLetters; currentXPicture++) {
                System.out.println("\n"+currentXPicture + " " + currentYPicture);/**trouble shooting*/
                pictureMatrix[(currentYPicture*numXLetters)+currentXPicture] = makeArrayFromPicture("",currentPicture, 2, numXLetters, numYLetters, currentXPicture, currentYPicture, true);
                System.out.println();
            }
        }

        return pictureMatrix;
    }

    //splits an individual letter into sections and finds the greyscale value of each section,
    //then stores it in an array
    public static double[] makeArrayFromPicture (String currentLetter, int currentPicture, int training, int numXPictures, int numYPictures, int currentXPicture, int currentYPicture, boolean display) throws IOException {
        BufferedImage inputImage;
        if (training == 0) {//if training the AI, look at training pictures
            inputImage = ImageIO.read(new File("C:\\Users\\justi\\OneDrive\\Desktop\\Justin Lin\\Java word search project\\Java AI Images\\Letters\\training\\" + currentLetter + "\\" + currentLetter + currentPicture + ".jpg"));
        }
        else if (training == 1) {//if we are testing, look at the testing pictures
            inputImage = ImageIO.read(new File("C:\\Users\\justi\\OneDrive\\Desktop\\Justin Lin\\Java word search project\\Java AI Images\\Letters\\testing\\" + currentLetter + "\\test " + currentLetter + currentPicture + ".jpg"));
        }
        else /*if (training == 2)*/ {//if using the AI, look at use pictures
            inputImage = ImageIO.read(new File("C:\\Users\\justi\\OneDrive\\Desktop\\Justin Lin\\Java word search project\\Java AI Images\\Inputs\\" + currentPicture + ".jpg"));
        }
        /**FOR DEMONSTRATION PURPOSES ONLY, WHEN ACTUALLY USING, CHANGE THE SECTIONS NUMBER BACK TO 25*
         * TURN THE ELSE IF ABOVE BACK INTO AN ELSE, COMMENT OUT THE LINE BELOW
         *
         * FOLLOW INSTRUCTIONS AT TOP
         */
        //inputImage = ImageIO.read(new File("C:\\Users\\justi\\OneDrive\\Desktop\\Justin Lin\\Java word search project\\Java AI Images\\misc\\apple.jpg"));

        //25*25 sections for each letter
        int numSections = 25;

        //calculate the size of each letter
        int pictureXSize = inputImage.getWidth()/numXPictures;
        int pictureYSize = inputImage.getHeight()/numYPictures;

        /**--------------------*--------------------**/

        //calculate size of each section of the letter (same size for each letter so only need to calculate once)
        int sectionXSize = pictureXSize/numSections; //section size ~4px (3.9...)
        int sectionYSize = pictureYSize/numSections;

        //System.out.println(inputCircleImage.getWidth()+" "+inputCircleImage.getHeight()+"\n"+sectionXSize+"px * "+sectionYSize+" px");

        double[][] greyScaleMatrix = new double[numSections][numSections];
        int sectionGreyScaleValue = 0;

        //help reduce cutoff at the end by making first few sections take more pixels
        int numCutOffPixX = (pictureXSize - numSections*sectionXSize);
        int numReAddPixX = 0;
        int numCutOffPixY = (pictureYSize - numSections*sectionYSize);
        int numReAddPixY = 0;

        if (training==2||display) {//hopefully runs faster without having to print several hundred letters
            System.out.println(currentPicture);
        }

        //int currentYSection = 0;
        //int currentXSection = 0;
        for (int currentYSection = 0; currentYSection < numSections; currentYSection++) { //go through each y section
            /**i changed the code to add offsets up top but idk if they actually do what they're supposed to, meh works anyway**/
            numReAddPixX = 0;
            for (int currentXSection = 0; currentXSection < numSections; currentXSection++) { // go through each x section
                for (int x = (currentXSection * sectionXSize) + numReAddPixX + (currentXPicture * pictureXSize); x < sectionXSize + (currentXSection * sectionXSize) + numReAddPixX + (currentXPicture * pictureXSize); x++) {//add the greyscale value for each pixel in the section
                    for (int y = (currentYSection * sectionYSize) + numReAddPixY + (currentYPicture * pictureYSize); y < sectionYSize + (currentYSection * sectionYSize) + numReAddPixY + (currentYPicture * pictureYSize); y++) {
                        //System.out.print(" x:"+x+" y:"+y+" ");
                        Color currentPixelColor = new Color(inputImage.getRGB(x, y));
                        //System.out.println((currentPixelColor.getRed() + currentPixelColor.getGreen() + currentPixelColor.getBlue()) / 3);
                        sectionGreyScaleValue += (currentPixelColor.getRed() + currentPixelColor.getGreen() + currentPixelColor.getBlue()) / 3;
                    }
                    //read the offsets for the y's
                    /**idk if this actually works*/
                    /**so it doesn't read the offset, but it still offsets, so it works**/
                    /*if (currentXSection < numCutOffPixX/2||currentXSection > (numSections-1)-numCutOffPixX/2) {
                        Color currentPixelColor = new Color(inputImage.getRGB(x, sectionYSize + (currentYSection * sectionYSize) + numReAddPixY + (currentYPicture * pictureYSize)));
                        sectionGreyScaleValue += (currentPixelColor.getRed() + currentPixelColor.getGreen() + currentPixelColor.getBlue()) / 3;
                    }*/
                }
                //read the offsets for the x's
                /**idk if this actually works*/
                /**so it doesn't read the offset, but it still offsets, so it works**/
                /*if (currentYSection < numCutOffPixY/2||currentYSection > (numSections-1)-numCutOffPixY/2) {
                    Color currentPixelColor = new Color(inputImage.getRGB(sectionXSize + (currentXSection * sectionXSize) + numReAddPixX + (currentXPicture * pictureXSize), sectionYSize + (currentYSection * sectionYSize) + numReAddPixY + (currentYPicture * pictureYSize)));
                    sectionGreyScaleValue += (currentPixelColor.getRed() + currentPixelColor.getGreen() + currentPixelColor.getBlue()) / 3;
                }*/


                /**this is some sketchy code, still technically works but for the wrong reason, maybe fix later**/
                if (currentXSection < numCutOffPixX/2||currentXSection > (numSections-1)-numCutOffPixX/2) { //making the first few and last few sections larger to center the image and reduce cutoff at the end
                    numReAddPixX++;
                }

                /**if i eventually actually read the offsets, make sure to include it when averaging the section greyscale value**/
                sectionGreyScaleValue /= (sectionXSize * sectionYSize);//average the section greyscale value

                if (sectionGreyScaleValue > 255) {//nudging the values a bit, sometimes above 255, maybe section isn't dividing right
                    sectionGreyScaleValue = 255;
                } else if (sectionGreyScaleValue < 0) {
                    sectionGreyScaleValue = 0;
                }

                //for aesthetics, take out when actually using
                //actually, it kinda works for letters since I don't need to know the exact greyscale value, just black and white
                if (sectionGreyScaleValue > 256/1.5) {
                    sectionGreyScaleValue = 1;
                }
                else {
                    sectionGreyScaleValue = 0;
                }

                if (training == 2||display) { //hopefully runs faster without having to print several hundred letters
                    //printing each section grey scale value
                    System.out.print(sectionGreyScaleValue + " ");
                }

                greyScaleMatrix[currentYSection][currentXSection] = sectionGreyScaleValue; //add value to the greyscale matrix
            }
            if (training == 2||display) { //hopefully runs faster without having to print several hundred letters
                System.out.println();
            }

            /**i changed the code to add offsets up top but idk if they actually do what they're supposed to, meh works anyway**/
            if (currentYSection < numCutOffPixY/2||currentYSection > (numSections-1)-numCutOffPixY/2) {
                numReAddPixY++;
            }
        }
        return matrixToArray(greyScaleMatrix);
    }
    public static double[] matrixToArray (double[][] matrix) {//only works for matrices with uniform shapes
        double[] arr = new double[matrix.length*matrix[0].length];
        int arrayCounter = 0;
        for (int a = 0; a < matrix.length; a++) {
            for (int b = 0; b < matrix[a].length; b++) {
                arr[arrayCounter] = matrix[a][b];
                arrayCounter++;
            }
        }
        return arr;
    }
}