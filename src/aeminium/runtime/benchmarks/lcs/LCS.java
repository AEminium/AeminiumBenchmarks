package aeminium.runtime.benchmarks.lcs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

public class LCS {
	public static final String s1 = "The quick fox jumps over the lazy dog.";
	public static final String s2 = "Jacob is a very lazy dog.";
	
	public static String readFile(String string) {
		FileInputStream stream;
		try {
			stream = new FileInputStream(new File(string));
		} catch (FileNotFoundException e1) {
			return null;
		}
		try {
			FileChannel fc = stream.getChannel();
			MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0,
					fc.size());
			/* Instead of using default, pass in a decoder. */
			return Charset.defaultCharset().decode(bb).toString();
		} catch (Exception e) {
			return null;
		} finally {
			try {
				stream.close();
			} catch (IOException e) {
				return null;
			}
		}
	}
}
