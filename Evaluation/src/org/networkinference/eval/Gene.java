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


/**
 * Represents a node (gene) of the network
 */
public class Gene {

	/** The name of the gene (unique ID) */
	private String name_ = null;
	
	/** The regulators / incoming edges of this gene */
	private HashSet<Gene> regulators_ = null;
	/** The target genes / outgoing edges of this gene */
	private HashSet<Gene> targets_ = null;
	
	
	// ============================================================================
	// PUBLIC METHODS
	    
	/** Constructor */
	public Gene(String name) {
		
		name_ = name;
		regulators_ = new HashSet<Gene>();
		targets_ = new HashSet<Gene>();
	}
	
	
    // ----------------------------------------------------------------------------

	/** Add the given gene to the set of regulators */
	public void addRegulator(Gene n) {
		regulators_.add(n);
	}

	
    // ----------------------------------------------------------------------------

	/** Add the given node to the set of targets */
	public void addTarget(Gene n) {
		targets_.add(n);
	}


    // ----------------------------------------------------------------------------

	/** Return true if g is in the set of targets_ */
	public boolean regulates(Gene g) {
		return targets_.contains(g);
	}

    // ----------------------------------------------------------------------------

	/** Return true if g is in the set of regulators_ */
	public boolean isRegulatedBy(Gene g) {
		return regulators_.contains(g);
	}

	
   // ----------------------------------------------------------------------------

	/** Return true if the gene has at least one target */
	public boolean isRegulator() {
		return targets_.size() > 0;
	}

	
	// ============================================================================
	// PRIVATE METHODS

    
	// ============================================================================
	// SETTERS AND GETTERS

	public String getName() { return name_; }
	public HashSet<Gene> getRegulators() { return regulators_; }
	public HashSet<Gene> getTargets() { return targets_; }

	
	
}
