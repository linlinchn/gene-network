/*
Copyright (c) 2012 Daniel Marbach

We release this software open source under an MIT license (see below). If this
software was useful for your scientific work, please cite our paper available at:
http://networkinference.org

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 */
package org.networkinference.eval;

import joptsimple.OptionParser;
import joptsimple.OptionSet;


/**
 * Main class
 */
public class Evaluation {

	/** The command-line parser */
	private OptionParser parser_ = null;

	/** The file with the edge prediction list */
	private String predictionFile_ = null;
	/** The file with the list of gold standard (true) edges */
	private String goldStandardFile_ = null;

	/** Set true to generate PR curves and AUPR */
	private boolean PR_ = true;
	/** Set true to generate ROC curves and AUROC */
	private boolean ROC_ = true;
	/** Set true to run the analysis of transitive and co-regulation edges */
	private boolean motifs_ = false;
	
	/** The performance assessment also contains the network instance */
	private Performance judge_ = null;
	
	
	// ============================================================================
	// MAIN

	/** Main function */
	public static void main(String[] args) {
		
		try {
			Evaluation example = new Evaluation();
			example.run(args);
		} catch (Exception e) {
			error(e);
		}
	}

	
    // ----------------------------------------------------------------------------

	/** Print the stack trace of the exception and exit */
	static public void error(Exception e) {
		e.printStackTrace();
		System.exit(-1); // return -1 in case of error
	}

    // ----------------------------------------------------------------------------

	/** Print line */
	static public void println(String msg) {
		System.out.println(msg);
	}

	
    // ----------------------------------------------------------------------------

	/** Test two doubles for equality with epsilon 1e-6 */
	static public boolean assertEquals(double x, double y) {
		return Math.abs(x - y) < 1e-6;
	}

	
	// ============================================================================
	// PUBLIC METHODS
	
	/** Parse the command-line arguments, read the files, perform network inference, write outputs */
	public void run(String[] args) {
		
		// Define the arguments accepted by your module
		defineArgs();
		// Parse the arguments
		parseArgs(args);

		// Load the gold standard and the prediction
		Network network = new Network(predictionFile_, goldStandardFile_);
		judge_ = new Performance(network);
		
		if (PR_ || ROC_) {
			judge_.assess();
			judge_.output(PR_, ROC_, getFilenameWithoutPathAndExtension(predictionFile_));
		}
		if (motifs_)
			judge_.analyzeErrors();
		
		// Write the predictions to a file
		//writeOutputFiles();
		
		System.out.println("Done!");
	}

	
	// ============================================================================
	// PRIVATE METHODS
		
	/**  
	 * Implementing this function is mandatory for your module. It will help us and
	 * the users to understand and verify the options of your tool.
 	 */
	private void displayHelp() {
		
		System.out.println("USAGE");
		System.out.println("   java -jar Evaluation.jar --pred <file> --gold <file> [OPTIONS]");
		System.out.println("OPTIONS");
		System.out.println("   --pred <file>   File with ranked list of predicted edges");
		System.out.println("   --gold <file>   File with list of gold standard (true) edges");
		System.out.println("   --PR            Compute precision-recall (PR) curve and area under the curve (AUPR)");
		System.out.println("   --ROC           Compute receiver operating characteristic (ROC) curve and area under the curve (AUROC)");
		System.out.println("   --motifs        Analyze systematic prediction errors (transitive/indirect and co-regulation edges)");
		System.out.println("   --help          Display this usage information");
	}

	
	// ----------------------------------------------------------------------------

	/** 
	 * Defines the command-line arguments.
	 * JOpt examples:
	 * http://pholser.github.com/jopt-simple/examples.html
	 */
	private void defineArgs() {
		
		parser_ = new OptionParser();
		parser_.accepts("pred").withRequiredArg();
		parser_.accepts("gold").withRequiredArg();
		parser_.accepts("PR");
		parser_.accepts("ROC");
		parser_.accepts("motifs");
		parser_.accepts("help");
	}

	
	// ----------------------------------------------------------------------------

	/** Parses the command-line arguments, which were defined by defineArgs() */
	private void parseArgs(String[] args) {
		
		OptionSet options = null;
		try {
			options = parser_.parse(args);
		} catch (Exception e) {
			displayHelp();
			error(e);
		}
		
		if (options.has("help")) {
			displayHelp();
			System.exit(0);
		}
		
		// Check for required options
		if (options.has("pred")) {
			predictionFile_ = (String) options.valueOf("pred");
		} else {
			displayHelp();
			throw new IllegalArgumentException("Missing argument '--pred <file>'");
		}
		
		if (options.has("gold")) {
			goldStandardFile_ = (String) options.valueOf("gold");
		} else {
			displayHelp();
			throw new IllegalArgumentException("Missing argument '--pred <gold>'");
		}

		if ((options.has("PR") || options.has("ROC")) && options.has("motifs")) {
			displayHelp();
			throw new IllegalArgumentException("You cannot compute PR/ROC curves and analyze prediction errors at the same time. " +
							"(PR/ROC should be computed over complete lists, whereas prediction errors should be analyzed after applying a cutoff)");
		}
		if (options.has("PR") && !options.has("ROC"))
			ROC_ = false;
		if (options.has("ROC") && !options.has("PR"))
			PR_ = false;
		if (options.has("motifs")) {
			PR_ = false;
			ROC_ = false;
			motifs_ = true;
		}
	}

	
	// ----------------------------------------------------------------------------

	/** Get prediction filename without path and extension */
	private String getFilenameWithoutPathAndExtension(String filename) {
				
		// The beginning of the filename (without the path) 
		int start = filename.lastIndexOf("/") + 1;
		if (start == -1)
			start = filename.lastIndexOf("\\") + 1; // windows
		if (start == -1)
			start = 0;
		
		// The end of the filename (without file extension)
		int end = filename.lastIndexOf(".");
		if (end == -1 || end <= start) // not found or part of the path
			end = filename.length();

		return filename.substring(start, end);
	}

}
