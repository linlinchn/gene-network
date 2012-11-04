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

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;


/**
 * Parse a tab-separated value file (other separators than tab can also be set)
 */
public class FileParser {

	/** The token used to separate the columns (usually a single character) */
	String separator_ = "\t";
	/** The buffered file reader */
	BufferedReader reader_ = null;
	/** Line counter */
	private int lineCounter_ = 0;
	/** Next line */
	private String nextLine_ = null;
	
	
	// ============================================================================
	// PUBLIC METHODS
	    
	/** Constructor */
	public FileParser(String filename) {

		try {
			System.out.println("Reading file: " + filename);
			FileInputStream fstream = new FileInputStream(filename);
			DataInputStream in = new DataInputStream(fstream);
			reader_ = new BufferedReader(new InputStreamReader(in));
		} catch (Exception e) {
			Evaluation.error(e);
		}
	}
	
	
    // ----------------------------------------------------------------------------

	/** Read and return the next line, split using the separator_. Returns null if there is no more line to read. */
	public String[] readLine() {
		
		try {
			lineCounter_++;
			nextLine_ = reader_.readLine();
		} catch (IOException e) {
			Evaluation.error(e);
		}
		
		if (nextLine_ == null)
			return null;

		return nextLine_.split(separator_, -1);
	}
	

    // ----------------------------------------------------------------------------

	/** Read all the lines (see readLine()) */
	public ArrayList<String[]> readAll() {
		
		ArrayList<String[]> data = new ArrayList<String[]>();
		
		try {
			while (true) {
				nextLine_ = reader_.readLine();
				if (nextLine_ == null)
					break;
				
				lineCounter_++;
				data.add(nextLine_.split(separator_));
			}
		} catch (IOException e) {
			Evaluation.error(e);
		}
		
		return data;
	}

	
    // ----------------------------------------------------------------------------

	/** Be polite and close the file reader when you're done */
	public void close() {
		
		try {
			reader_.close();
		} catch (IOException e) {
			Evaluation.error(e);
		}
	}
	  
	
    // ----------------------------------------------------------------------------

	/** Skip N lines (useful to skip headers) */
	public void skipLines(int N) {
		
		try {
			for (int i=0; i<N; i++)
				reader_.readLine();
		} catch (IOException e) {
			Evaluation.error(e);
		}
	}

	  	
	// ============================================================================
	// SETTERS AND GETTERS

	public void setSeparator(String separator) { separator_ = separator; }
    
	public int getLineCounter() { return lineCounter_; }
		
}
