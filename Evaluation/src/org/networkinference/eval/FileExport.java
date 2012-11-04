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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;


/**
 * Write a text file
 */
public class FileExport {

	/** The buffered file writer */
	BufferedWriter writer_ = null;
	
	
	// ============================================================================
	// PUBLIC METHODS
	    
	/** Constructor */
	public FileExport(String filename) {

		try {
			System.out.println("Writing file: " + filename);
			FileWriter fstream = new FileWriter(filename);
			writer_ = new BufferedWriter(fstream);
		} catch (Exception e) {
			Evaluation.error(e);
		}
	}
	
	
    // ----------------------------------------------------------------------------

	/** Write a line to the file */
	public void println(String str) {
		
		print(str + "\n");
	}


	// ----------------------------------------------------------------------------

	/** Write the given string to the file */
	public void print(String str) {
		
		try {
			writer_.write(str);
		} catch (IOException e) {
			Evaluation.error(e);
		}
	}
	
	
    // ----------------------------------------------------------------------------

	/** Be polite and close the file writer */
	public void close() {
		
		try {
			writer_.close();
		} catch (IOException e) {
			Evaluation.error(e);
		}
	}
	  
}
