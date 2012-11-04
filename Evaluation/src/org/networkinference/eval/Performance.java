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
import java.util.HashSet;
import java.util.Iterator;


/**
 * Compute PR and ROC curves, as well as area under the curves 
 */
public class Performance {
	
	/** The network that is being assessed */
	private Network network_ = null;
	/** The prediction list */
	private ArrayList<Edge> predictionList_ = null;
	
	/** The number of gold standard edges */
	private int numGoldStandardEdges_ = -1;
	/** The number of predicted edges */
	private int numPredictedEdges_ = -1;
	/** The total number of possible edges */
	private int numPossibleEdges_ = -1;
	
	/** PR curve */
	private double[][] PR_ = null;
	/** ROC curve */
	private double[][] ROC_ = null;
	/** Area under PR curve */
	private double AUPR_ = -1;
	/** Area under ROC curve */
	private double AUROC_ = -1;
	
	/** The number of transitive edges */
	private int numTransitive_ = -1;
	/** The number of co-regulation edges */
	private int numCoregulation_ = -1;
	/** The total number of false positives */
	private int numFalsePositives_ = -1;

	/** The expected number of transitive edges in a randomized prediction with the same number of true/false positives */
	private int numTransitiveRand_ = -1;
	/** The number of co-regulation edges */
	private int numCoregulationRand_ = -1;

	
	// ============================================================================
	// PUBLIC METHODS
	
	/** Constructor */
	public Performance(Network network) {
		
		network_ = network;
		predictionList_ = network.getPredictionList();
		numPredictedEdges_ = predictionList_.size();
		numGoldStandardEdges_ = network_.getNumGoldStandardEdges();
		numPossibleEdges_ = network.getNumPossibleEdges();
	}
	
	
	// ----------------------------------------------------------------------------

	/** Compute PR and ROC curves, as well as AUPR and AUROC values */
	public void assess() {
		
		computeCurves();
		computeAreaUnderCurves();
	}

	
	// ----------------------------------------------------------------------------

	/** Display info for PR/ROC and write files */
	public void output(boolean PR, boolean ROC, String filename) {
		
		// Expected AUPR by chance
		double AUPR_rand = numGoldStandardEdges_/(double)numPossibleEdges_;
		
		Evaluation.println("");
		Evaluation.println("AREA UNDER CURVE");
		if (PR)
			Evaluation.println("AUPR:\t" + AUPR_);
		if (ROC)
			Evaluation.println("AUROC:\t" + AUROC_);
		Evaluation.println("");
		
		Evaluation.println("EXPECTED PERFORMANCE OF RANDOM PREDICTION");
		if (PR)
			Evaluation.println("AUPR:\t" + AUPR_rand);
		if (ROC)
			Evaluation.println("AUROC:\t0.5");
		Evaluation.println("");
		
		// PR curve
		if (PR)
			writeCurve(PR_, filename + "_PR.txt");
		if (ROC)
			writeCurve(ROC_, filename + "_ROC.txt");
		
		// AUCs
		FileExport writer = new FileExport(filename + "_AUC.txt");
		writer.println("AUPR\t" + AUPR_);
		writer.println("AUROC\t" + AUROC_);
		writer.println("AUPR_random\t" + AUPR_rand);
		writer.close();
	}

	
    // ----------------------------------------------------------------------------

	/** Evaluate transitive and co-regulation edges */
	public void analyzeErrors() {
		
		countErrors();
		countExpectedErrors();
		
		double goldStandardFractionTransitive = numTransitiveRand_/(double)(numPossibleEdges_-numGoldStandardEdges_);
		double goldStandardFractionCoregulation = numCoregulationRand_/(double)(numPossibleEdges_-numGoldStandardEdges_);
		double expectedTransitive = goldStandardFractionTransitive * numFalsePositives_;
		double expectedCoregulation = goldStandardFractionCoregulation * numFalsePositives_;

		Evaluation.println("");
		Evaluation.println("SYSTEMATIC PREDICTION ERRORS");
		Evaluation.println("             \tTotal\tFraction of false positives");
		Evaluation.println("Transitive   \t" + numTransitive_ + "\t" + numTransitive_/(double)numFalsePositives_);
		Evaluation.println("Co-regulation\t" + numCoregulation_ + "\t" + numCoregulation_/(double)numFalsePositives_);
		Evaluation.println("");
		Evaluation.println("EXPECTED ERRORS IN RANDOMIZED PREDICTION WITH SAME NUMBER OF TRUE AND FALSE POSITIVES");
		Evaluation.println("             \tTotal\tFraction of false positives");
		Evaluation.println("Transitive   \t" + expectedTransitive + "\t" + expectedTransitive/(double)numFalsePositives_);
		Evaluation.println("Co-regulation\t" + expectedCoregulation + "\t" + expectedCoregulation/(double)numFalsePositives_);
	}

	
	// ============================================================================
	// PRIVATE METHODS

	/** Compute PR and ROC curves */
	private void computeCurves() {
		
		// Define some constants
		int numGoldStandardNegatives = numPossibleEdges_ - numGoldStandardEdges_; // The total number of negatives
		if (numGoldStandardNegatives == 0)
			throw new RuntimeException("There are no negatives in the gold standard!");
		
		PR_ = new double[numPossibleEdges_][2];
		ROC_ = new double[numPossibleEdges_][2];

		double TP_k = 0; // Number of true positives at rank k
		double FP_k = 0; // Number of false positives at rank k
		
		int k = 0;
		for (; k<numPredictedEdges_; k++) {
			Edge edge_k = predictionList_.get(k);
			if (edge_k.isTruePositive())
				TP_k++;
			else
				FP_k++;
			
			PR_[k][0] = TP_k / numGoldStandardEdges_; // recall
			PR_[k][1] = TP_k / (k+1); // precision
			ROC_[k][0] = FP_k / numGoldStandardNegatives; // false positive rate
			ROC_[k][1] = PR_[k][0]; // true positive rate = recall
		}
		
		// Random discovery rate for the remaining edges, if not all edges were included
		double prob_TP = -1;
		double prob_FP = -1;
		if (k < numPossibleEdges_) {
			prob_TP = (numGoldStandardEdges_ - TP_k) / (double)(numPossibleEdges_ - k);
			prob_FP = 1 - prob_TP;
		}
		
		// Extend beyond the list of predicted edges if it does not include all possible edges
		for (; k<numPossibleEdges_; k++) {
			TP_k += prob_TP;
			FP_k += prob_FP;
			
			PR_[k][0] = TP_k / numGoldStandardEdges_; // recall
			PR_[k][1] = TP_k / (k+1); // precision
			ROC_[k][0] = FP_k / numGoldStandardNegatives; // false positive rate
			ROC_[k][1] = PR_[k][0]; // true positive rate = recall			
		}
		
		assert Evaluation.assertEquals(TP_k + FP_k, numPossibleEdges_);
		assert Evaluation.assertEquals(TP_k, numGoldStandardEdges_);
		assert Evaluation.assertEquals(FP_k, numGoldStandardNegatives);
	}


    // ----------------------------------------------------------------------------

	/** 
	 * Compute AUPR and AUROC. 
	 * Note, in the PR curve a nonlinear interpolation between points would be correct,
	 * whereas we use a linear interpolation here. See Stolovitzky et al. (2009) for a
	 * description of the nonlinear interpolation. However, the first-order approximation
	 * seems to give very similar results (delta ~= 0.0003). Furthermore, the AUPR from the 
	 * nonlinear interpolation is actually slightly larger than the linear one, which is 
	 * unexpected and points to a minor error in the implementation of the nonlinear form by 
	 * Stolovitzky et al. (2009) (indeed, a perfect prediction achieves an AUPR slightly
	 * above 1.0 in their nonlinear implementation -- without the normalization in the end
	 * the result is 1.0, but this is also seems unexpected to me...)
	 * 
	 * To summarize, the first-order approximation seems to be sufficient and reduces the
	 * chance of introducing errors compared to a nonlinear implementation.
	 */
	private void computeAreaUnderCurves() {
		
		AUPR_ = 0;
		AUROC_ = 0;
		
		for (int k=0; k<numPossibleEdges_-1; k++) {
			AUPR_ += (PR_[k+1][0] - PR_[k][0]) * (PR_[k+1][1] + PR_[k][1]) / 2;
			AUROC_ += (ROC_[k+1][0] - ROC_[k][0]) * (ROC_[k+1][1] + ROC_[k][1]) / 2;
		}
		// Normalize AUPR by max possible value
		AUPR_ = AUPR_ / (1 - 1.0/network_.getNumGoldStandardEdges());
	}


    // ----------------------------------------------------------------------------

	/** Count the number of false positives, transitive and co-regulation edges */
	private void countErrors() {
		
		numFalsePositives_ = 0;
		numTransitive_ = 0;
		numCoregulation_ = 0;
		
		for (int k=0; k<numPredictedEdges_; k++) {
			Edge edge_k = predictionList_.get(k);
			
			if (!edge_k.isTruePositive())
				numFalsePositives_++;
			if (edge_k.isTransitive())
				numTransitive_++;
			if (edge_k.isCoregulation())
				numCoregulation_++;
		}
	}

	
    // ----------------------------------------------------------------------------

	/** Count the number of expected transitive and co-regulation edges in a randomized prediction with the same number of TPs/FPs */
	private void countExpectedErrors() {

		HashSet<Gene> regulators = network_.getRegulators();
		Iterator<Gene> regIter = regulators.iterator();
		// Iterate over all regulators
		while (regIter.hasNext()) {
			Gene TF = regIter.next();

			// Iterate over all genes
			Iterator<Gene> targetIter = network_.getGenes().values().iterator();
			while (targetIter.hasNext()) {
				Gene target = targetIter.next();
				// self-loop
				if (TF.equals(target))
					continue;
				
				Edge edge = new Edge (TF, target);
				if (edge.isTransitive())
					numTransitiveRand_++;
				if (edge.isCoregulation())
					numCoregulationRand_++;
			}
		}
	}

	
    // ----------------------------------------------------------------------------

	/** Write PR/ROC curve */
	private void writeCurve(double[][] curve, String filename) {
		
		FileExport writer = new FileExport(filename);
		for (int i=0; i<numPossibleEdges_; i++)
			writer.println(curve[i][0] + "\t" + curve[i][1]);	
		
		writer.println("");
		writer.close();
	}

}
