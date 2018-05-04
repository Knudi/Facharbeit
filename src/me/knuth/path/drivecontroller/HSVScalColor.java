package me.knuth.path.drivecontroller;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * Ein Enumerator, welcher es vereinfacht, zwischen Farben zu unterschiden
 * 
 * @author Alexander Knuth
 *
 */
public enum HSVScalColor 
{
	YELLOW(17, 10, 10, 47, 255, 255),
	BLUE(100, 170, 60, 130, 255, 240),
	GREEN(70, 153, 4, 104, 255, 150),
	/*Für Rot sind zwei Enumeratoren notwendig, da im HSV Farbraum der rote Bereich von 0 bis ca. 30 und von ca. 320 bis ca. 359*/
	RED_1(0, 90, 40, 16, 255, 195),
	RED_2(145, 90, 40, 179, 255, 195);
	
	private Scalar lowerBound;
	private Scalar upperBound;
	
	private HSVScalColor(int lowerH, int lowerS, int lowerV, int upperH, int upperS, int upperV)
	{
		this.lowerBound = new Scalar(lowerH, lowerS, lowerV);
		this.upperBound = new Scalar(upperH, upperS, upperV);
	}
	
	public Scalar getUpperBound()
	{
		return this.upperBound;
	}
	
	public Scalar getLowerBound()
	{
		return this.lowerBound;
	}
	
	/**
	 * Wandelt ein BGR Bild in ein Binärbild um, welches abhängig von der ausgewählten Farbe
	 * alles außer dieser ausschwärzt.
	 * 
	 * Dabei wird die Methode inRange verwendet, welche ein Bild umwandelt, in dem es jeden Pixel prüft, ob
	 * dieser im Farbspektrum liegt
	 * 
	 * @param img
	 * @return
	 */
	public Mat getFiltered(Mat img)
	{
        Mat hsv = new Mat();
        Imgproc.cvtColor(img, hsv, Imgproc.COLOR_BGR2HSV);
        Imgproc.GaussianBlur(hsv, hsv, new Size(9, 9), 2, 2);
		Mat mask = new Mat();
		/*Mit hilfe dieser Methode wird ein HSV Bild zu einem Binärbild, in dem nur die Pixel innerhalb des durch
		 * die Parametern 2 und 3 beschränkten Raumes weiß dargestsellt sind*/
		Core.inRange(hsv, this.lowerBound, this.upperBound, mask);
		if(this == RED_1)
		{
			Mat temp = new Mat();
			Core.inRange(hsv, RED_2.lowerBound, RED_2.upperBound, temp);
			Core.add(mask, temp, mask);
		}
		if(this == RED_2)
		{
			Mat temp = new Mat();
			Core.inRange(hsv, RED_1.lowerBound, RED_1.upperBound, temp);
			Core.add(mask, temp, mask);
		}
		Cam.reduceNoise(mask);
		return mask;
	}
	
}
