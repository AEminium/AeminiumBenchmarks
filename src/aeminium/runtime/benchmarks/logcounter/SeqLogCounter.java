/**
 * Copyright (c) 2010-11 The AEminium Project (see AUTHORS file)
 * 
 * This file is part of Plaid Programming Language.
 *
 * Plaid Programming Language is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 *  Plaid Programming Language is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Plaid Programming Language.  If not, see <http://www.gnu.org/licenses/>.
 */

package aeminium.runtime.benchmarks.logcounter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import aeminium.runtime.benchmarks.helpers.Benchmark;

public class SeqLogCounter {
	
	public static void main(String[] args) throws Exception {
		Benchmark be = new Benchmark(args);
		File[] fs = LogCounter.finder(args[0]);
		be.start();
		int r = sequentialCounter(fs);
		be.end();
		if (be.verbose) {
			System.out.println(r + " visits");
		}
	}

	public static int sequentialCounter(File[] files) {
		int n = 0;
		for (File logfile : files) {
			String d;
			try {
				d = LogCounter.uncompressGZip(logfile);
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			}
			
			try {
				n += LogCounter.countAccesses(d);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				LogCounter.deleteFile(logfile);
			}
		}
		return n;
	}
	
}