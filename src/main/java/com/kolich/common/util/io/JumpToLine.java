/**
 * Copyright (c) 2015 Mark S. Kolich
 * http://mark.koli.ch
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package com.kolich.common.util.io;

import static com.kolich.common.DefaultCharacterEncoding.UTF_8;
import static org.apache.commons.io.IOUtils.lineIterator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;

public final class JumpToLine {
	
	private final InputStream is_;
	private final String charsetName_;
	
	private final LineIterator it_;
	
	private long lastLineRead_ = -1L;
	
	/**
	 * Opens any underlying streams/readers and immeadietly seeks
	 * to the line in the file that's next to be read; skipping over
	 * lines in the file this reader has already read.
	 */
	public JumpToLine(final InputStream is, final String charsetName)
		throws IOException {
		is_ = is;
		charsetName_ = charsetName;
		lastLineRead_ = 1L;
		try {
			it_ = lineIterator(is_, charsetName_);
		} catch (IOException e) {
			close();
			throw e;
		}
	}
	
	public JumpToLine(final InputStream is) throws IOException {
		this(is, UTF_8);
	}
	
	public JumpToLine(final File file, final String charsetName)
		throws IOException {
		this(new FileInputStream(file), charsetName);
	}
	
	public JumpToLine(final File file) throws IOException {
		this(file, UTF_8);
	}
		
	/**
	 * Seeks to the last line read in the file.
	 */
	public long seek() {
		return seek(lastLineRead_);
	}
	
	/**
	 * Seeks to a given line number in the stream/file.
	 * @param line the line number to seek to
	 */
	public long seek(final long line) {
		long lineCount = 1L;
		while((it_ != null) && (it_.hasNext()) && (lineCount < line)) {
			it_.nextLine();
			lineCount += 1L;
		}
		// If we got to the end of the file, but haven't read as many
		// lines as we should have, then the requested line number is
		// out of range.
		if(lineCount < line) {
			throw new NoSuchElementException("Invalid line number; " +
				"out of range.");
		}
		lastLineRead_ = lineCount;
		return lineCount;
	}
	
	/**
	 * Closes this IOUtils LineIterator and the underlying
	 * input stream reader.
	 */
	public void close() {
		IOUtils.closeQuietly(is_);
		LineIterator.closeQuietly(it_);
	}
	
	/**
	 * Returns true of there are any more lines to read in the
	 * file.  Otherwise, returns false.
	 * @return
	 */
	public boolean hasNext() {
		return it_.hasNext();
	}
	
	/**
	 * Read a line of text from this reader.
	 * @return
	 */
	public String readLine() {
		String ret = null;
		try {
			// If there is nothing more to read with this LineIterator
			// then nextLine() throws a NoSuchElementException.
			ret = it_.nextLine();
			lastLineRead_ += 1L;
		} catch (NoSuchElementException e) {
			throw e;
		}
		return ret;
	}
	
	public long getLastLineRead() {
		return lastLineRead_;
	}

}
