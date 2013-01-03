package aeminium.runtime.benchmarks.histogrameq;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.imageio.ImageIO;

import aeminium.runtime.Body;
import aeminium.runtime.Runtime;
import aeminium.runtime.Task;
import aeminium.runtime.implementations.Factory;

/**
 * Image histogram equalization
 * 
 * Author: Bostjan Cigan (http://zerocool.is-a-geek.net)
 * 
 */

public class HistogramEQAeminium {

	private static BufferedImage original, equalized;
	public static Runtime rt;
	public static BufferedImage histogramEQ;
	public static ArrayList<int[]> histLUT;
	public static File original_f;
	public static String output_f;
	public static int[] rhistogram;
	public static int[] ghistogram;
	public static int[] bhistogram;

	public static ArrayList<int[]> imageHist;

	public static float scale_factor;

	public static int numberOfHistogramEqualizationTaskFor;

	private static Task histogramEqualizationTaskFor(Task current, Collection<Task> prev, final int paramI, final int paramP) {
		Task task = rt.createNonBlockingTask(new Body() {
			@Override
			public void execute(Runtime rt, Task current) {
				int red;
				int green;
				int blue;
				int alpha;
				int newPixel = 0;
				for (int i = paramI; i < paramP; i++) {
					for (int j = 0; j < original.getHeight(); j++) {

						// Get pixels by R, G, B
						alpha = new Color(original.getRGB(i, j)).getAlpha();
						red = new Color(original.getRGB(i, j)).getRed();
						green = new Color(original.getRGB(i, j)).getGreen();
						blue = new Color(original.getRGB(i, j)).getBlue();

						// Set new pixel values using the histogram lookup table
						red = histLUT.get(0)[red];
						green = histLUT.get(1)[green];
						blue = histLUT.get(2)[blue];

						// Return back to original format
						newPixel = colorToRGB(alpha, red, green, blue);

						// Write pixels into image
						histogramEQ.setRGB(i, j, newPixel);

					}
				}
			}
		}, Runtime.NO_HINTS);
		rt.schedule(task, current, prev);
		return task;
	}

	private static Task writeImageTask(Task current, Collection<Task> prev) {
		Task task = rt.createNonBlockingTask(new Body() {
			@Override
			public void execute(Runtime rt, Task current) throws IOException {
				equalized = histogramEQ;
				writeImage(output_f);
			}
		}, Runtime.NO_HINTS);
		rt.schedule(task, current, prev);
		return task;
	}

	private static Task histogramEqualizationTask(Task current, Collection<Task> prev) {
		Task task = rt.createNonBlockingTask(new Body() {
			@Override
			public void execute(Runtime rt, Task current) throws IOException {

				histogramEQ = new BufferedImage(original.getWidth(), original.getHeight(), original.getType());
				Collection<Task> prev = new ArrayList<Task>();
				int step = original.getWidth() / numberOfHistogramEqualizationTaskFor;
				int position=0;
				for (int i = 0; i < numberOfHistogramEqualizationTaskFor; i++) {
					if(i==numberOfHistogramEqualizationTaskFor-1){
						Task init1 = histogramEqualizationTaskFor(current, Runtime.NO_DEPS, position, original.getWidth());
						prev.add(init1);
					}else{
						Task init1 = histogramEqualizationTaskFor(current, Runtime.NO_DEPS, position, position+=step);
						prev.add(init1);
					}


				}

				writeImageTask(current, prev);
			}
		}, Runtime.NO_HINTS);
		rt.schedule(task, current, prev);
		return task;
	}

	private static Task histogramEqualizationLUTTask(Task current, Collection<Task> prev, final BufferedImage input) {
		Task task = rt.createNonBlockingTask(new Body() {
			@Override
			public void execute(Runtime rt, Task current) throws IOException {
				// Get the Lookup table for histogram equalization
				// Get an image histogram - calculated values by R, G, B
				// channels
				imageHist = imageHistogram(input);

				Collection<Task> prev1 = new ArrayList<Task>();
				Task init1 = fillLookupTableTask(current, Runtime.NO_DEPS, input);
				prev1.add(init1);

				Collection<Task> prev2 = new ArrayList<Task>();
				Task init2 = histogramEqualizationLUTTaskFirstFor(current, prev1, imageHist);
				Task init3 = histogramEqualizationLUTTaskSecondFor(current, prev1, imageHist);
				Task init4 = histogramEqualizationLUTTaskThirdFor(current, prev1, imageHist);
				prev2.add(init2);
				prev2.add(init3);
				prev2.add(init4);

				histogramEqualizationLUTTaskFinal(current, prev2);

			}
		}, Runtime.NO_HINTS);
		rt.schedule(task, current, prev);
		return task;
	}

	private static Task fillLookupTableTask(Task current, Collection<Task> prev, final BufferedImage input) {
		Task task = rt.createNonBlockingTask(new Body() {
			@Override
			public void execute(Runtime rt, Task current) {
				// Fill the lookup table
				rhistogram = new int[256];
				ghistogram = new int[256];
				bhistogram = new int[256];

				for (int i = 0; i < rhistogram.length; i++)
					rhistogram[i] = 0;
				for (int i = 0; i < ghistogram.length; i++)
					ghistogram[i] = 0;
				for (int i = 0; i < bhistogram.length; i++)
					bhistogram[i] = 0;

				// Calculate the scale factor
				scale_factor = (float) (255.0 / (input.getWidth() * input.getHeight()));
			}
		}, Runtime.NO_HINTS);
		rt.schedule(task, current, prev);
		return task;
	}

	private static Task histogramEqualizationLUTTaskFinal(Task current, Collection<Task> prev) {
		Task task = rt.createNonBlockingTask(new Body() {
			@Override
			public void execute(Runtime rt, Task current) {
				// Create the lookup table
				ArrayList<int[]> imageLUT = new ArrayList<int[]>();

				imageLUT.add(rhistogram);
				imageLUT.add(ghistogram);
				imageLUT.add(bhistogram);

				histLUT = imageLUT;
			}
		}, Runtime.NO_HINTS);
		rt.schedule(task, current, prev);
		return task;
	}

	private static Task histogramEqualizationLUTTaskFirstFor(Task current, Collection<Task> prev, final ArrayList<int[]> imageHist) {
		Task task = rt.createNonBlockingTask(new Body() {
			@Override
			public void execute(Runtime rt, Task current) {
				long sumr = 0;

				for (int i = 0; i < rhistogram.length; i++) {
					sumr += imageHist.get(0)[i];
					int valr = (int) (sumr * scale_factor);
					if (valr > 255) {
						rhistogram[i] = 255;
					} else
						rhistogram[i] = valr;
				}

			}
		}, Runtime.NO_HINTS);
		rt.schedule(task, current, prev);
		return task;
	}

	private static Task histogramEqualizationLUTTaskSecondFor(Task current, Collection<Task> prev, final ArrayList<int[]> imageHist) {
		Task task = rt.createNonBlockingTask(new Body() {
			@Override
			public void execute(Runtime rt, Task current) {
				long sumg = 0;

				for (int i = 0; i < rhistogram.length; i++) {

					sumg += imageHist.get(1)[i];
					int valg = (int) (sumg * scale_factor);
					if (valg > 255) {
						ghistogram[i] = 255;
					} else
						ghistogram[i] = valg;
				}

			}
		}, Runtime.NO_HINTS);
		rt.schedule(task, current, prev);
		return task;
	}

	private static Task histogramEqualizationLUTTaskThirdFor(Task current, Collection<Task> prev, final ArrayList<int[]> imageHist) {
		Task task = rt.createNonBlockingTask(new Body() {
			@Override
			public void execute(Runtime rt, Task current) {
				long sumb = 0;

				for (int i = 0; i < rhistogram.length; i++) {
					sumb += imageHist.get(2)[i];
					int valb = (int) (sumb * scale_factor);
					if (valb > 255) {
						bhistogram[i] = 255;
					} else
						bhistogram[i] = valb;
				}

			}
		}, Runtime.NO_HINTS);
		rt.schedule(task, current, prev);
		return task;
	}

	public static void main(String[] args) throws IOException {
		long initialTime = System.currentTimeMillis();

		original_f = new File(args[0]);
		output_f = args[1];
		numberOfHistogramEqualizationTaskFor=Integer.parseInt(args[2]);

		rt = Factory.getRuntime();
		rt.init();

		original = ImageIO.read(original_f);

		Collection<Task> prev = new ArrayList<Task>();
		Task init0 = histogramEqualizationLUTTask(Runtime.NO_PARENT, Runtime.NO_DEPS, original);
		prev.add(init0);

		histogramEqualizationTask(Runtime.NO_PARENT, prev);

		rt.shutdown();

		long finalTime = System.currentTimeMillis();
		System.out.println("Time cost = " + (finalTime - initialTime) * 1.0 / 1000);
	}

	private static void writeImage(String output) throws IOException {
		File file = new File(output + ".jpg");
		ImageIO.write(equalized, "jpg", file);
	}

	// Return an ArrayList containing histogram values for separate R, G, B
	// channels
	public static ArrayList<int[]> imageHistogram(BufferedImage input) {

		int[] rhistogram = new int[256];
		int[] ghistogram = new int[256];
		int[] bhistogram = new int[256];

		for (int i = 0; i < rhistogram.length; i++)
			rhistogram[i] = 0;
		for (int i = 0; i < ghistogram.length; i++)
			ghistogram[i] = 0;
		for (int i = 0; i < bhistogram.length; i++)
			bhistogram[i] = 0;

		for (int i = 0; i < input.getWidth(); i++) {
			for (int j = 0; j < input.getHeight(); j++) {

				int red = new Color(input.getRGB(i, j)).getRed();
				int green = new Color(input.getRGB(i, j)).getGreen();
				int blue = new Color(input.getRGB(i, j)).getBlue();

				// Increase the values of colors
				rhistogram[red]++;
				ghistogram[green]++;
				bhistogram[blue]++;

			}
		}

		ArrayList<int[]> hist = new ArrayList<int[]>();
		hist.add(rhistogram);
		hist.add(ghistogram);
		hist.add(bhistogram);

		return hist;

	}

	// Convert R, G, B, Alpha to standard 8 bit
	private static int colorToRGB(int alpha, int red, int green, int blue) {

		int newPixel = 0;
		newPixel += alpha;
		newPixel = newPixel << 8;
		newPixel += red;
		newPixel = newPixel << 8;
		newPixel += green;
		newPixel = newPixel << 8;
		newPixel += blue;

		return newPixel;

	}

}