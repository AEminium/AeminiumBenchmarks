package aeminium.runtime.benchmarks.logcounter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class LogCounter {

	public static List<File> addFiles(List<File> files, File dir) {
		if (files == null)
			files = new LinkedList<File>();

		if (!dir.isDirectory()) {
			if (dir.getAbsolutePath().endsWith(".gz")) {
				files.add(dir);
			}
			return files;
		}

		for (File file : dir.listFiles()) {
			addFiles(files, file);
		}
		return files;
	}
	
    public static File[] finder(String dirName){
    	File dir = new File(dirName);
    	List<File> fs = LogCounter.addFiles(null, dir);
    	File[] fa = new File[2 * fs.size()];
    	int i = 0;
    	for (File f : fs) {
    		fa[i] = f;
    		fa[2 * i] = f;
    		i++;
    	}
    	return fa;
    }
    
    static int countAccesses(String d)
			throws FileNotFoundException, IOException { 
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(d));
			int count = 0;
			String line;
			while (true) {
				line = reader.readLine();
				if (line == null) break;
				if (line.startsWith("wiki.alcidesfonseca.com")) {
					count++;
				}
				return count;
			}
		} finally {
			if ( reader != null ) {
				reader.close();
			}
		}
		return 0;
	}
    
    public static File compressGZip(File source) throws IOException {
		if (source.getAbsolutePath().contains(".gz")) return source;
		
		String dest = source.getAbsolutePath() + ".gz";
		File d = new File(dest);
		InputStream in = new FileInputStream(source);
		OutputStream out = new GZIPOutputStream(new FileOutputStream(d));
		try {
			byte[] buffer = new byte[65536];
			int noRead;
			while ((noRead = in.read(buffer)) != -1) {
				out.write(buffer, 0, noRead);
			}
		} finally {
			try {
				out.close();
				in.close();
			} catch (Exception e) {
			}
		}
		return d;
	}

	static String uncompressGZip(File source) throws IOException {
		if (!source.getAbsolutePath().contains(".gz")) return source.getAbsolutePath();
		
		String dest = source.getAbsolutePath().replace(".gz", "");
		File d = new File(dest);
		InputStream in = new FileInputStream(source);
		OutputStream out = new FileOutputStream(d);
		try {
			in = new GZIPInputStream(in);
			byte[] buffer = new byte[65536];
			int noRead;
			while ((noRead = in.read(buffer)) != -1) {
				out.write(buffer, 0, noRead);
			}
		} finally {
			try {
				out.close();
				in.close();
			} catch (Exception e) {
			}
		}
		return dest;
	}

	protected static void deleteFile(File logfile) {
		String np = logfile.getAbsolutePath().replace(".gz", "");
		new File(np).delete();
	}
	    

}
