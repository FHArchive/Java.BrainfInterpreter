// 2>/dev/null; /usr/bin/java $0 $@; exit $?
/*
Developed by Kieran W on the 01/02/2019
*/
/*
* The aim of this Java project is to parse a file with extension .b or .bf
* and to produce an interpreter for the 'Brainf' programming language
*/

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Scanner;

class Brainf {

    // Errors
    final static String ERR_FILE_IS_INVALID = "ERROR: The specified file " +
    "does not exist (filename: %s)";
    final static String ERR_FILE_IS_NULL = "ERROR: The specified file does " +
    "not contain any text (filename: %s)";
    final static String ERR_POINTER_LT_ZERO = "ERROR: The pointer cannot be " +
    "less than zero (instruction: %d, %s)";
    final static String ERR_POINTER_GT_MAX = "ERROR: The pointer cannot be "
    + "greater than the size of the array (array_size: %d instruction: %d, %s)";
    final static String ERR_VALUE_LT_MIN = "ERROR: The value cannot be "
    + "less than the size of the minimum integer (instruction: %d, %s " +
    "pointer: %d)";
    final static String ERR_VALUE_GT_MAX = "ERROR: The value cannot be "
    + "greater than the size of the maximum integer (instruction: %d, %s " +
    "pointer: %d)";
    final static String ERR_EXCEEDED_INPUT = "ERROR: This program requests " +
    "too much input from the user (instruction: %d, %s pointer: %d limit: %d)";

    // Warnings
    final static String WARN_FILE_MAY_BE_INVALID = "WARN: The specified file "
    + "may not be a 'Brainf' file as it does not have the extension .b or " +
    ".bf (filename: %s)";
    final static String WARN_HIGH_INPUT = "WARN: This program is requesting " +
    "too much input from the user (instruction: %d, %s pointer: %d limit: %d)";

    // Information
    final static String INFO_EXECUTION_COMPLETE = "INFO: Code executed " +
    "successfully";

    final static String INPUT_MSG = "INFO: Input requested (type: %s) ";

    // Constants
    final static int MAX_SIZE = 30000;
    final static int MAX_INPUT = 20;
    public enum MODE{
        ASCII, INT
    };

    public static void main(String args[]) {

        // Print hello world from inline code, file and file with comments
        String dirtyInput = "++++++++[>++++[>++>+++>+++>+<<<<-]>+>+>->>+[<]<-" +
        "]>>.>---.+++++++..+++.>>.<-.<.+++.------.--------.>>+.>++.";
        String cleanOutput = syntaxCleaner(dirtyInput);
        brainfInterpreter(cleanOutput, MODE.INT);
        brainfInterpreter(syntaxCleaner(fileReader("helloWorld.bf")), MODE.ASCII);
        brainfInterpreter((fileReader("helloWorldComments.bf")), MODE.ASCII);

        // Test input command and run 'countdown' twice
        brainfInterpreter(syntaxCleaner(fileReader("countdown.bf")), MODE.INT);
        brainfInterpreter(syntaxCleaner(fileReader("countdown.bf")), MODE.INT);

        // Run subtract (5 - 3 = 2)
        brainfInterpreter(syntaxCleaner(fileReader("subtract.bf")), MODE.INT);

        // Run multiply (5 * 3 = 15)
        brainfInterpreter(syntaxCleaner(fileReader("multiply.bf")), MODE.INT);

        // Run divide (12 / 3 = 4)
        brainfInterpreter(syntaxCleaner(fileReader("divide.bf")), MODE.INT);

        // I can't seem to get these working  :(
        //brainfInterpreter(syntaxCleaner(fileReader("rot13.bf")), MODE.ASCII);
        //brainfInterpreter(syntaxCleaner(fileReader("truth.bf")), MODE.INT);
    }

    /*
     * Read in an input file (specified by a relative path, in the form of a
     * string) and return a string of the contents
     */
    public static String fileReader(String filePath) {
        String fileContents = null;
        try{
            fileContents = new String(Files.readAllBytes(Paths.get(filePath)));
        }
        catch(IOException e){
            // If there is an IOException, the file can be assumed to be invalid
            System.out.println(String.format(ERR_FILE_IS_INVALID, filePath));
        }
        // Print an error if the file has no content
        if(fileContents == null || fileContents.length() == 0){
            System.out.println(String.format(ERR_FILE_IS_NULL, filePath));
        }

        // Get the extension
        String extension = "";
        int periodLocation = filePath.lastIndexOf('.');
        if (periodLocation > 0) {
            extension = filePath.substring(periodLocation+1);
        }
        if(!(extension.contentEquals("b") || extension.contentEquals("bf"))){
            // Provide a warning if the file does not appear to be in 'Brainf'
            System.out.println(String.format(WARN_FILE_MAY_BE_INVALID, filePath));
        }

        return fileContents;
    }

    /*
     * The purpose of this function is to strip non brainf syntax, this is to
     * make the program more modular. The syntax is "< > + - . , [ ]"
     */
    public static String syntaxCleaner(String fileContents) {
        // Define variables
        int fileContentsLen = fileContents.length();
        char[] legalChars = { '<', '>', '+', '-', '.', ',', '[', ']' };
        int legalCharsLen = legalChars.length;
        StringBuffer cleanSyntax = new StringBuffer();
        // For every character in the input string...
        for (int syntaxPointer = 0; syntaxPointer < fileContentsLen;
        syntaxPointer++) {
            // ...Select the char at the position of the pointer
            char element = fileContents.charAt(syntaxPointer);
            // ...And compare it to each char in the legalChars array
            for (int legalPointer = 0; legalPointer < legalCharsLen;
            legalPointer++) {
                if (element == legalChars[legalPointer]) {
                    // Add the char to the output if it is part of the syntax
                    cleanSyntax.append(element);
                }
            }
        }
        return cleanSyntax.toString();
    }

    /*
     * The purpose of this function is to take the cleaned syntax and execute
     * the appropriate function based on this
     */
    public static void brainfInterpreter(String instruction, MODE mode) {
        // Define variables
        int[] array = new int[MAX_SIZE];
        int arrayPointer = 0;
        int instructionPointer = 0;
        int instructionLen = instruction.length();
        int inputCounter = 0;

        // While still reading instructions
        while (instructionPointer < instructionLen) {
            char currentInstruction = instruction.charAt(instructionPointer);
            int value = array[arrayPointer];

            // Define < operator
            if (currentInstruction == '<') {
                if (arrayPointer != 0) {
                    arrayPointer--;
                } else {
                    System.out.println(String.format(ERR_POINTER_LT_ZERO,
                    instructionPointer, currentInstruction));
                    return;
                }
            }

            // Define > operator
            if (currentInstruction == '>') {
                if (arrayPointer < MAX_SIZE) {
                    arrayPointer++;
                } else {
                    System.out.println(
                            String.format(ERR_POINTER_GT_MAX, MAX_SIZE,
                            instructionPointer, currentInstruction));
                    return;
                }
            }

            // Define - operator
            if (currentInstruction == '-') {
                if (value > Integer.MIN_VALUE) {
                    array[arrayPointer]--;
                } else {
                    System.out.println(String.format(ERR_VALUE_LT_MIN,
                    instructionPointer, currentInstruction, arrayPointer));
                    return;
                }
            }

            // Define + operator
            if (currentInstruction == '+') {
                if (value < Integer.MAX_VALUE) {
                    array[arrayPointer]++;
                } else {
                    System.out.println(String.format(ERR_VALUE_GT_MAX,
                    instructionPointer, currentInstruction, arrayPointer));
                    return;
                }
            }

            // Define . operator
            if (currentInstruction == '.') {
                switch(mode){
                    case ASCII:{
                        System.out.print((char) value);
                        break;
                    }
                    case INT:{
                        System.out.print(value + ", ");
                        break;
                    }
                }


            }

            // Define , operator
            if (currentInstruction == ',') {
                System.out.print(String.format(INPUT_MSG, mode.toString()));
                Scanner reader = new Scanner(System.in);
                inputCounter ++;
                switch(mode){
                    case ASCII:{
                        array[arrayPointer] = reader.next().charAt(0);
                        break;
                    }
                    case INT:{
                        array[arrayPointer] = reader.nextInt();
                        break;
                    }
                }

                // Terminate if input is called too many times
                if(inputCounter >= MAX_INPUT * 0.75){
                    System.out.println(String.format(WARN_HIGH_INPUT,
                    instructionPointer, currentInstruction, arrayPointer, MAX_INPUT));
                }
                if(inputCounter >= MAX_INPUT){
                    System.out.println(String.format(ERR_EXCEEDED_INPUT,
                    instructionPointer, currentInstruction, arrayPointer, MAX_INPUT));
                    reader.close();
                    return;
                }


                //reader.close();
            }

            // Define [ operator

            // Need to find the matching closing bracket
            if (currentInstruction == '[') {
                if (value == 0) {
                    int brackets = 0;
                    while (true) {
                        // Decrement the pointer and refresh the current instruction
                        instructionPointer++;
                        currentInstruction = instruction.charAt(instructionPointer);
                        // Another opening bracket is encountered
                        if (currentInstruction == '[') {
                            brackets++;
                        }
                        // A closing bracket is encountered
                        else if (currentInstruction == ']') {
                            // If this is the matching bracket
                            if (brackets == 0) {
                                break;
                            } else {
                                brackets--;
                            }
                        }
                    }
                }
            }

            // Define ] operator

            // Need to find the matching opening bracket
            if (currentInstruction == ']') {
                if (value > 0) {
                    int brackets = 0;
                    while (true) {
                        // Decrement the pointer and refresh the current instruction
                        instructionPointer--;
                        currentInstruction = instruction.charAt(instructionPointer);
                        // Another closing bracket is encountered
                        if (currentInstruction == ']') {
                            brackets++;
                        }
                        // An opening bracket is encountered
                        else if (currentInstruction == '[') {
                            // If this is the matching bracket
                            if (brackets == 0) {
                                break;
                            } else {
                                brackets--;
                            }
                        }
                    }

                }
            }


            // Increment the instruction
            instructionPointer++;
        }

        // Inform the user that code execution is complete
        System.out.println(INFO_EXECUTION_COMPLETE);

    }



}
