package aeminium.runtime.benchmarks.histogrameq;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Histogram {
    // Convert R, G, B, Alpha to standard 8 bit
    static int colorToRGB(int alpha, int red, int green, int blue) {
 
        int newPixel = 0;
        newPixel += alpha; newPixel = newPixel << 8;
        newPixel += red; newPixel = newPixel << 8;
        newPixel += green; newPixel = newPixel << 8;
        newPixel += blue;
 
        return newPixel;
 
    }
    
    static void writeImage(String output, BufferedImage eq) throws IOException {
        File file = new File(output+".jpg");
        ImageIO.write(eq, "jpg", file);
    }
}
