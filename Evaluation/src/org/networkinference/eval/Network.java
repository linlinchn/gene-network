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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


/**
 *  The gold standard and the predicted network.
 *  Note, only edges in the prediction file that connect regulators and genes
 *  that are part of the gold standard network are considered, other edges
 *  are ignored when reading the prediction file (that's the standard approach
 *  used in the DREAM network inference challenge). 
 */
public class Network {

	/** 
	 * The genes of the gold standard (the key is the name of the gene).
	 * Note, the gold standard edges are stored in the genes themselves
	 * (see Gene.regulators_, Gene.targets_).
	 */
	private HashMap<String, Gene> genes_ = null;
	
	/** The set of regulators in the gold standard (a subset of genes_) */
	private HashSet<Gene> regulators_ = null;
	
	/** The number of edges in the gold standard */
	private int numGoldStandardEdges_ = -1;
	
	/** 
	 * The list of predicted edges, filtered to include only edges that
	 * connect regulators and genes that are part of the gold standard 
	 */
	private ArrayList<Edge> predictionList_ = null;
	
	
	// ============================================================================
	// PUBLIC METHODS
	
	/** Constructor, loads the network from the given file */
	public Network(String predictionFile, String goldFile) {
		
		genes_ = new HashMap<String, Gene>();
		regulators_ = new HashSet<Gene>();
		numGoldStandardEdges_ = 0;
		predictionList_ = new ArrayList<Edge>();
		
		// Load the gold standard
		loadGoldStandard(goldFile);
		// Load the prediction, only edges in the "universe" of the gold standard are included
		loadPrediction(predictionFile);
	}
	
	
    // ----------------------------------------------------------------------------

	/** If the gene exists already return it, otherwise create and add it to genes_ */
	public Gene addGene(String name) {
		
		Gene gene = genes_.get(name);
		if (gene == null) {
			gene = new Gene(name);
			genes_.put(name, gene);
		}
			
		return gene;
	}

	
	// ============================================================================
	// PRIVATE METHODS

	/** Load the gold standard */
	public void loadGoldStandard(String file) {
		
		FileParser parser = new FileParser(file);
		String[] nextLine = parser.readLine();
		if (nextLine == null)
			throw new RuntimeException("The file is empty!");
		
		boolean twoColumnFormat = (nextLine.length == 2);
		boolean threeColumnFormat = (nextLine.length == 3);
		
		while (nextLine != null) {
			// Check format: either two columns or three columns where the third column is always '1'
			if (twoColumnFormat && nextLine.length != 2)
				throw new RuntimeException("Parse error at line " + parser.getLineCounter() + ": expected two columns");
			if (threeColumnFormat && nextLine.length != 3)
				throw new RuntimeException("Parse error at line " + parser.getLineCounter() + ": expected three columns");
			if (nextLine.length == 3 && Integer.parseInt(nextLine[2]) != 1)
				throw new RuntimeException("Parse error at line " + parser.getLineCounter() + ": the third column must be '1'");

			Gene TF = addGene(nextLine[0]);
			regulators_.add(TF);
			Gene target = addGene(nextLine[1]);
			TF.addTarget(target);
			target.addRegulator(TF);
			numGoldStandardEdges_++;
			
			nextLine = parser.readLine();
		}
		parser.close();
	}

	
    // ----------------------------------------------------------------------------

	/** 
	 * Load the prediction file, filtered to include only edges that
	 * connect regulators and genes that are part of the gold standard
	 */
	public void loadPrediction(String file) {
		
		FileParser parser = new FileParser(file);
		String[] nextLine = parser.readLine();
		if (nextLine == null)
			throw new RuntimeException("The file is empty!");
		
		while (nextLine != null) {
			// Check that the line has three columns
			if (nextLine.length != 3)
				throw new RuntimeException("Parse error at line " + parser.getLineCounter() + ": expected three columns");

			Gene TF = genes_.get(nextLine[0]);
			Gene target = genes_.get(nextLine[1]);
			
			// Check that the TF is a regulator and the target is a gene of the gold standard
			if (TF != null && TF.isRegulator() && target != null) {
				double score = Double.parseDouble(nextLine[2]);
				Edge edge = new Edge(TF, target, score);
				predictionList_.add(edge);
			}
			nextLine = parser.readLine();
		}
	}

	
	// ============================================================================
	// GETTERS AND SETTERS

	public int getNumPossibleEdges() { return regulators_.size()*(genes_.size() - 1); }
	public int getNumGoldStandardEdges() { return numGoldStandardEdges_; }
				   
	public ArrayList<Edge> getPredictionList() { return predictionList_; }
	public HashSet<Gene> getRegulators() { return regulators_; }
	public HashMap<String, Gene> getGenes() { return genes_; }
}
