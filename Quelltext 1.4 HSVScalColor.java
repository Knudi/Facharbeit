package me.knuth.path.drivecontroller;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * Ein Enumerator, welcher es vereinfacht, zwischen Farben hin und her zu wechseln
 * Dieser Enumerator dient ebenfalls der Vereinfacherung der Speicherung der Farbe in der Edge-Klasse
 * Dazu wird hier das HSV Farbmodell genutzt, in dem die Farbe "H" auf einem Farbkreis der von 0-359 geht
 * Wiedergespiegel wird. "S" für Sättigung und "V" für Helligkeit der Farben, welche jeweils von 0-100 gehen.
 * 
 * @author Alexander Knuth
 *
 */
public enum HSVScalColor 
{
    /*Die hier umgesetzen Ennumerationen wurden auf die Werte 0-255 normalisiert, da die Bilbiothek Open-CV nur mit Werten in diesem Bereich arbeitet*/
	YELLOW(17, 10, 10, 47, 255, 255),
	BLUE(100, 170, 60, 130, 255, 240),
	GREEN(70, 153, 4, 100, 255, 255),
	/*Für Rot sind zwei Enumeration notwendig, da im HSV Farbraum der rote Bereich von 0 bis ca. 30 und von ca. 320 bis ca. 359*/
	RED_1(0, 90, 40, 16, 255, 195),
	RED_2(145, 90, 40, 179, 255, 195);
	
	private Scalar lowerBound; /*Obere HSV-Grenze*/
	private Scalar upperBound; /*Untere HSV-Grenze*/
	
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
	 * alles außer dieser ausschwärtzt.
	 * 
	 * Dabei wird die Methode inRange verwendet, welche ein Bild umwandelt, in dem es jeden Pixel prüft, ob
	 * dieser in einer bestimmten H-S-V-Reichweite liegt.
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
        /*Falls die Farbe eines der beiden Rot ist, erzeuge Binärbild des anderen Rotes und füge Bild zusammen*/
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
		return mask;
	}
	
}
