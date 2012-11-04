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

import java.util.HashSet;
import java.util.Iterator;


/**
 * Represents a directed edge between two nodes
 */
public class Edge implements Comparable<Edge> {

	/** The TF */
	private Gene TF_ = null;
	/** The target gene */
	private Gene target_ = null;
	/** The weight */
	private double weight_ = -1;
	

	// ============================================================================
	// PUBLIC METHODS
	
	public Edge(Gene TF, Gene target) {
		TF_ = TF;
		target_ = target;
	}

	public Edge(Gene TF, Gene target, double weight) {
		TF_ = TF;
		target_ = target;
		weight_ = weight;
	}
	
	
	// ----------------------------------------------------------------------------
	
	/** Return true if this edge agrees with the information stored in TF_ and target_ */
	public boolean isTruePositive() {
		return TF_.regulates(target_);
	}

	
	// ----------------------------------------------------------------------------
	
	/** 
	 * Return true if this is a transitive edge.
	 * Transitive edges are NOT part of the gold standard (false positives)
	 * and there is an indirect path TF->X->target in the gold standard.
	 */
	public boolean isTransitive() {
		
		if (isTruePositive())
			return false;
		
		// The set of regulators of the target (the set of potential genes X in the path TF_->X->target_)
		HashSet<Gene> X = target_.getRegulators();
		Iterator<Gene> iter = X.iterator();
		while (iter.hasNext())
			if (iter.next().isRegulatedBy(TF_))
				return true;
		
		return false;
	}


	// ----------------------------------------------------------------------------
	
	/** 
	 * Return true if this is a co-regulation edge.
	 * Co-regulation edges are NOT part of the gold standard (false positives)
	 * and the two genes are co-regulated in the gold standard (X->TF, X->target)
	 */
	public boolean isCoregulation() {
		
		if (isTruePositive())
			return false;
		
		// The set of regulators of the target (the set of potential genes X such that X->TF and X->target)
		HashSet<Gene> X = target_.getRegulators();
		Iterator<Gene> iter = X.iterator();
		while (iter.hasNext())
			if (iter.next().regulates(TF_))
				return true;
		
		return false;
	}

	
	// ----------------------------------------------------------------------------
	
	/** Compare the weights of the two edges (used for sorting with the Comparable interface) */
	public int compareTo(Edge edge2) {
		if (this.weight_ == edge2.weight_)
			return 0;
		else if (this.weight_ < edge2.weight_)
			return 1;
		else
			return -1;
	}

	
	// ============================================================================
	// SETTERS AND GETTERS

    public Gene getRegulator() { return TF_; }
    public Gene getTarget() { return target_; }    
    public double getWeight() { return weight_; }
        
}
