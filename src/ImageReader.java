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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Iterator;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class ImageReader {
	ArrayList<String> peopleList = new ArrayList<>();
	ArrayList<TrainingImage> trainingImages = new ArrayList<>(); 
	int idLocal=0;
	int id=0;
	byte observedMaxVal = 0;
	int encodedMaxVal = 0;
	DecimalFormat df = new DecimalFormat("#.##");
	public boolean verbose = false;

	public ImageReader(){

	}

	public int getMaxImageId() {
		return id;
	}
	
	public void normalizeImages() {
		for (TrainingImage t : trainingImages) {
			double[] normalizedImage = new double[t.getByteImage().length];
			int i = 0;
			
			for (byte val : t.getByteImage()) {
				//System.out.println(b + " " +(double)b);

				//Min is zero, max is observed max val
				//casting to double converts flawlessly from bytes
				//now just need to squish values into 0-1 range

				//https://stackoverflow.com/a/5295202/8711488
				//       (b-a)(x - min)
				//f(x) = --------------  + a
				//          max - min

				double x = (double)val;
				double a = 0;
				double b = 1;
				double min = 0;
				double max = (double)observedMaxVal;

				double normalizedVal = (b-a)*(x-min)/(max-min) +a;
				
				if (val == 127) {
					break;
				}
				
				normalizedImage[i] = normalizedVal;
				i++;
			}
			
			t.setNormalizedImage(normalizedImage);
			//System.out.println("Byte array: " + Arrays.toString(t.getByteImage()));
			//System.out.println("Scaled double array: " + Arrays.toString(normalizedImage));
		}
	}

	public void createTrainingInput() {
		//byte[] img = readImage("faces/kawamura/kawamura_left_happy_sunglasses_4.pgm");
		//For some reason, file path needs to include bin, but file read must not have bin

		//File reading advice: https://stackoverflow.com/a/4917359/8711488
		File path = new File("bin/faces/");

		File [] files = path.listFiles();
		for (int i = 0; i < files.length; i++){
			if (files[i].isDirectory()) {
				//System.out.println(files[i]);
				File[] imageFiles = files[i].listFiles();
				for (int j = 0; j < imageFiles.length; j++){
					if (imageFiles[j].isFile()){ //ignore directories
						String imageName = imageFiles[j].getName();
						//System.out.println("    " + imageName);

						String[] split1 = imageName.split("\\.");
						String noDot = split1[0];
						String[] split2 = noDot.split("_");
						String person = split2[0];
						//System.out.println(person);
						ArrayList<String> imageInfo = new ArrayList<>();

						if (!peopleList.contains(person)) {
							peopleList.add(person);
							idLocal = 0;
						}

						for(int k = 0; k < split2.length; k++){
							/*if(k == -1) {
								imageInfo.add(imageFiles[j].toString());
							}*/
							imageInfo.add(split2[k]);
						}

						String loc = imageFiles[j].toString();
						loc = loc.substring(4);
						//System.out.println(imageInfo);
						//System.out.println(loc);

						//Add to array of training images if it is a #4, aka 30x32 image
						if (imageInfo.contains("4")) {;
						boolean sunglasses = false;
						Class mood = null;
						byte[] image = readImage(loc);

						if(imageInfo.contains("sunglasses")) {
							sunglasses = true;
						}
						if(imageInfo.contains("neutral")) {
							mood = Class.NEUTRAL;
						}
						else if(imageInfo.contains("happy")) {
							mood = Class.HAPPY;
						}
						else if(imageInfo.contains("sad")) {
							mood = Class.SAD;
						}
						else if(imageInfo.contains("angry")) {
							mood = Class.ANGRY;
						}
						else{
							System.out.println("No emotion listed");
							System.exit(1);
						}

						trainingImages.add(new TrainingImage(person,sunglasses,mood,image, idLocal, id));
						idLocal++;
						id++;
						}

					}

				}
			}
		}
		if(verbose) printInfo();

		normalizeImages();
	}
	
	public void printInfo() {
		System.out.println(peopleList);
		for (TrainingImage t : trainingImages) {
			System.out.println(t.toString());
		}
		System.out.println("Maximum val observed in image byte arrays: " + observedMaxVal);
		System.out.println("Maximum val claimed in file header: " + encodedMaxVal);
	}

	public ArrayList<String> getPeopleList() {
		return peopleList;
	}

	public void setPeopleList(ArrayList<String> peopleList) {
		this.peopleList = peopleList;
	}

	public ArrayList<TrainingImage> getTrainingImages() {
		return trainingImages;
	}

	public void setTrainingImages(ArrayList<TrainingImage> trainingImages) {
		this.trainingImages = trainingImages;
	}

	public byte getObservedMaxVal() {
		return observedMaxVal;
	}

	public void setObservedMaxVal(byte observedMaxVal) {
		this.observedMaxVal = observedMaxVal;
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

			if (maxVal > encodedMaxVal) encodedMaxVal = maxVal;

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
							if (im[count] > observedMaxVal) observedMaxVal = im[count];
							count++;
							im[count] = (byte)(b & 0xFF);
							if (im[count] > observedMaxVal) observedMaxVal = im[count];
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
			if(verbose) System.out.println("Height=" + height);
			if(verbose) System.out.println("Width=" + width);
			if(verbose) System.out.println("Required elements=" + (height * width));
			if(verbose) System.out.println("Obtained elements=" + count);

			d.close();
			f.close();

			return im;
		}
		catch(Throwable t) {
			t.printStackTrace(System.err) ;
			System.exit(1);
		}
		return null;


	}

}
