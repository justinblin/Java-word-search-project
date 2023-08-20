import java.util.*;
import java.io.*;

public class wordSearchSolver {
    public static void main(String[] args) throws IOException {
        /**input ex:

        2 2 (dimensions of matrix, rows columns)
        abcd (single string of matrix)
        2 (number of words)
        ab cd (each word)

        **/

        boolean useAI = false;
        Scanner consoleInput = new Scanner(System.in);

        if (useAI) {
            Scanner scan = new Scanner(new File("C:\\Users\\justi\\OneDrive\\Desktop\\Justin Lin\\Java word search project\\wordSearch\\wordSearchSolver.txt"));

            pictureAI.main(null);//getting all the letters of the word search
            int numRows = scan.nextInt();
            int numCol = scan.nextInt();
            String letters = scan.next();

            System.out.println("\nNum Words: ");//finding num words
            int numWords = consoleInput.nextInt();

            String wordBank = "";//finding word bank
            System.out.println("Enter btwn each word: ");
            for (int currentWord = 0; currentWord < numWords; currentWord++) {
                wordBank+=consoleInput.next()+" ";
            }

            System.out.println(numRows+" "+numCol+"\n"+letters+"\n"+numWords+"\n"+wordBank);
            /**FileWriter clears the file selected*/
            FileWriter writer = new FileWriter("C:\\Users\\justi\\OneDrive\\Desktop\\Justin Lin\\Java word search project\\wordSearch\\wordSearchSolver.txt");
            writer.write(numRows+" "+numCol+"\n"+letters+"\n"+numWords+"\n"+wordBank);
            writer.close();
        }
        /**initializing Scanner resets scanner counter*/
        Scanner scan = new Scanner(new File("C:\\Users\\justi\\OneDrive\\Desktop\\Justin Lin\\Java word search project\\wordSearch\\wordSearchSolver.txt"));

        int rows = scan.nextInt();
        int columns = scan.nextInt();

        String unconvertedMatrix = scan.next();
        int matrixCounter = 0;
        String[][] matrix = new String[rows][columns];
        System.out.println("Input:");
        for (int currRow = 0; currRow < rows; currRow++) {
            for (int currCol = 0; currCol < columns; currCol++) {
                matrix[currRow][currCol] = unconvertedMatrix.substring(matrixCounter, matrixCounter+1);
                matrixCounter++;
                System.out.print(matrix[currRow][currCol]+" ");
            }
            System.out.println();
        }
        System.out.println();

        int numWords = scan.nextInt();
        String[] wordBank = new String[numWords];
        for (int currWord = 0; currWord < numWords; currWord++) {
            wordBank[currWord] = scan.next();
        }

        //FINDING ANSWERS
        ArrayList<int[]> ans = new ArrayList<int[]>();
        ArrayList<String> answerNames = new ArrayList<String>();

        for (int currRow = 0; currRow < rows; currRow++) {//go through each row
            for (int currCol = 0; currCol < columns; currCol++) {//go through each column
                for (int rowDirection = -1; rowDirection < 2; rowDirection++) {//go through each row direction
                    for (int colDirection = -1; colDirection < 2; colDirection++) {//go through each column direction
                        if (!(rowDirection==0&&colDirection==0)) {//if not both directions = 0
                            for (int currWord = 0; currWord < numWords; currWord++) {//go through word bank
                                int[] tempAns = findWord(wordBank[currWord], matrix, 0, currRow, currCol, currRow, currCol, rowDirection, colDirection);//check current position, word, and direction

                                if (tempAns[0] != -1) {//if it's been found, add it to the answers matrix
                                    ans.add(tempAns);
                                    answerNames.add(wordBank[currWord]);
                                }
                            }
                        }
                    }
                }
            }
        }

        //display ans matrix
        System.out.println("Answers: ");
        for (int currRow = 0; currRow < ans.size(); currRow++) {
            System.out.print("#"+currRow+" "+answerNames.get(currRow)+": ");
            for (int currCol = 0; currCol < ans.get(currRow).length; currCol++) {
                System.out.print(ans.get(currRow)[currCol]+" ");
            }
            System.out.println();
        }
        System.out.println();

        //word bank order != answers order

        //replace answers with #'s
        int filledCounter = 0;
        //example int[] ans --> ans[0] is {startRow, startCol, finalRow, finalCol, rowDirection, colDirection}
        for (int curWord = 0; curWord < ans.size(); curWord++) {//go through each of the answers
            //System.out.println("troubleshooting "+ans.get(curWord)[0]+" "+ans.get(curWord)[1]+" "+ans.get(curWord)[2]+" "+ans.get(curWord)[3]+" "+ans.get(curWord)[4]+" "+ans.get(curWord)[5]);
            int curRow = ans.get(curWord)[0];
            int curCol = ans.get(curWord)[1];
            while (curRow != ans.get(curWord)[2] + ans.get(curWord)[4] || curCol != ans.get(curWord)[3] + ans.get(curWord)[5]) {//use OR because it could be going vert or hori, which would mess up AND
                if (matrix[curRow][curCol].charAt(0)>=65&&matrix[curRow][curCol].charAt(0)<=90)//if the answers space is a letter (it hasn't been visited yet)
                    filledCounter++;//add to fill counter
                //System.out.print("("+curRow+","+curCol+") ");
                matrix[curRow][curCol] = "" + curWord % 10;//mark that we visited the space
                curRow += ans.get(curWord)[4];
                curCol += ans.get(curWord)[5];
            }
            //System.out.println("\n");
        }
        System.out.println(filledCounter+" of "+((rows)*(columns))+" spaces filled");

        /*//test
        int curRow = ans.get(0)[0];
        int curCol = ans.get(0)[1];
        while (curRow != ans.get(0)[2] || curCol != ans.get(0)[3]) {
            matrix[curRow][curCol] = "*";
            curRow += ans.get(0)[4];
            curRow += ans.get(0)[5];
        }*/

        //display * matrix
        System.out.println("Output: ");
        for (int currRow = 0; currRow < rows; currRow++) {
            for (int currCol = 0; currCol < columns; currCol++) {
                System.out.print(matrix[currRow][currCol]+" ");
            }
            System.out.println();
        }
        System.out.println();
    }

    public static int[] findWord (String target, String[][] matrix, int count, int currRow, int currCol, int startRow, int startCol, int rowDirection, int colDirection) {
        if (matrix[currRow][currCol].equals(target.substring(count, count+1))) {//if the current letter matches the expected letter
            /*System.out.println("currRow: "+currRow+" currCol: "+currCol);*/
            if (count == target.length()-1) { //if we reach the end of our target
                int[] ans = {startRow, startCol, currRow,currCol, rowDirection, colDirection};
                return ans;//return the start and end positions
            }
            else {
                if (currRow > 0&&rowDirection==-1) {
                    if (colDirection==-1&&currCol>0) {//upper left
                        return findWord(target, matrix, count + 1, currRow +rowDirection, currCol +colDirection, startRow, startCol, rowDirection,colDirection);
                    }
                    else if (colDirection==0) {//up
                        return findWord(target, matrix, count + 1, currRow +rowDirection, currCol +colDirection, startRow, startCol, rowDirection,colDirection);
                    }
                    else if (colDirection==1&&currCol<matrix[currRow].length-1) {//upper right
                        return findWord(target, matrix, count + 1, currRow +rowDirection, currCol +colDirection, startRow, startCol, rowDirection,colDirection);
                    }
                }
                if (rowDirection==0) {
                    if (colDirection == -1 && currCol > 0) {//left
                        return findWord(target, matrix, count + 1, currRow + rowDirection, currCol + colDirection, startRow, startCol, rowDirection, colDirection);
                    } else if (colDirection == 1 && currCol < matrix[currRow].length - 1) {//right
                        return findWord(target, matrix, count + 1, currRow +rowDirection, currCol +colDirection, startRow, startCol, rowDirection,colDirection);
                    }
                }
                if (currRow < matrix.length-1&&rowDirection==1) {
                    if (colDirection==-1&&currCol>0) {//bottom left
                        return findWord(target, matrix, count + 1, currRow +rowDirection, currCol +colDirection, startRow, startCol, rowDirection,colDirection);
                    }
                    else if (colDirection==0) {//bottom
                        return findWord(target, matrix, count + 1, currRow +rowDirection, currCol +colDirection, startRow, startCol, rowDirection,colDirection);
                    }
                    else if (colDirection==1&&currCol<matrix[currRow].length-1) {//bottom right
                        return findWord(target, matrix, count + 1, currRow +rowDirection, currCol +colDirection, startRow, startCol, rowDirection,colDirection);
                    }
                }
            }
        }
        int[] dunno = {-1,-1,-1,-1, 0, 0};
        return dunno;
    }
}