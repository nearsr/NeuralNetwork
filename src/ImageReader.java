import java.awt.Point;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Base64;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class ImageReader {
	public ImageReader(){

	}
	
	

//Source: https://stackoverflow.com/questions/16494916/equivalent-method-for-imshow-in-opencv-java-build
public static void showResult(BufferedImage bufImage) {
    try {
        JFrame frame = new JFrame();
        System.out.println("Bufimg: " +bufImage);
        frame.getContentPane().add(new JLabel(new ImageIcon(bufImage)));
        frame.pack();
        frame.setVisible(true);
    } catch (Exception e) {
        e.printStackTrace();
    }
}


	//Source: https://www.mkyong.com/java/how-to-convert-byte-to-bufferedimage-in-java/
	public BufferedImage createBufferedImage(byte[] imageData, int width, int height) {
		try {
			
		    DataBuffer buffer = new DataBufferByte(imageData, imageData.length);

		    WritableRaster raster = Raster.createInterleavedRaster(buffer, width, height, 3 * width, 3, new int[] {0, 1, 2}, (Point)null);
		    ColorModel cm = new ComponentColorModel(ColorModel.getRGBdefault().getColorSpace(), false, true, Transparency.OPAQUE, DataBuffer.TYPE_BYTE); 
		    BufferedImage image = new BufferedImage(cm, raster, true, null);
		    return image;
			 
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return null;
	}

	//Source: https://stackoverflow.com/questions/18079754/convert-and-display-image-from-byte-array
/*
	private static BufferedImage createRGBImage(byte[] bytes, int width, int height) {
	    DataBufferByte buffer = new DataBufferByte(bytes, bytes.length);
	    ColorModel cm = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[]{8, 8, 8}, false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
	    return new BufferedImage(cm, Raster.createInterleavedRaster(buffer, width, height, width * 3, 3, new int[]{0, 1, 2}, null), false, null);
	}
	*/
	
	//Source: https://stackoverflow.com/questions/11922252/reading-a-pgm-file-in-java
	public byte[] readImage(String filename){
		try {
			InputStream f = ClassLoader.getSystemClassLoader().getResourceAsStream(filename);
			BufferedReader d = new BufferedReader(new InputStreamReader(f));
			String magic = d.readLine();    // first line contains P2 or P5
			String line = d.readLine();     // second line contains height and width
			while (line.startsWith("#")) {
				line = d.readLine();
			}
			Scanner s = new Scanner(line);
			int width = s.nextInt();
			int height = s.nextInt();
			line = d.readLine();// third line contains maxVal
			s = new Scanner(line);
			int maxVal = s.nextInt();
			s.close();
			
			byte[] im = new byte[height*width];

			int count = 0;
			int b = 0;
			try {
				while (count < height*width) {
					b = d.read() ;
					if ( b < 0 ) 
						break ;

					if (b == '\n') { // do nothing if new line encountered
					} 
					//                  else if (b == '#') {
					//                      d.readLine();
					//                  } 
					//                  else if (Character.isWhitespace(b)) { // do nothing if whitespace encountered
					//                  } 
					else {
						if ( "P5".equals(magic) ) { // Binary format
							im[count] = (byte)((b >> 8) & 0xFF);
							count++;
							im[count] = (byte)(b & 0xFF);
							count++;
						}
						else {  // ASCII format
							im[count] = (byte)b ;
							count++;
						}
					}
				}
			} catch (EOFException eof) {
				eof.printStackTrace(System.out) ;
			}
			System.out.println("Height=" + height);
			System.out.println("Width=" + height);
			System.out.println("Required elements=" + (height * width));
			System.out.println("Obtained elements=" + count);
			
			d.close();
			f.close();
			showResult(createBufferedImage(im, width, height));
			
			return im;
		}
		catch(Throwable t) {
			t.printStackTrace(System.err) ;
			System.exit(1);
		}
		return null;


	}
}
