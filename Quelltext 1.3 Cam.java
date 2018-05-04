package me.knuth.path.drivecontroller;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

/**
 * Diese Klasse dient der Fehler erkennung welche f�r den PID controller ben�tigt wird.
 * 
 * @author Alexander Knuth
 *
 */
public class Cam 
{
	static
	{
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}
	
	/*Die Fixpunkte/Keren der Erosion und dilatation, welche f�r die Rauschentfernung benutzt werden.*/
	private static Mat erodeKernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
	private static Mat dilateKernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5));

	private int width;
	private int height;
	
	private VideoCapture capture;
	private Mat image;
	
	public Cam(int width, int height)
	{
		this.height = height;
		this.width = width;
		this.capture = new VideoCapture(0);
		/*Setze Aufl�sung*/
		this.capture.set(Highgui.CV_CAP_PROP_FRAME_WIDTH, this.width);
		this.capture.set(Highgui.CV_CAP_PROP_FRAME_HEIGHT, this.height);
		this.capture.open(0);
		this.image = new Mat(height, width, CvType.CV_8UC3);
	}
	
	/**
	 * Schneidet aus dem Bild einen Bereich aus, welche f�r uns Interessant ist.
	 * 
	 * @param height die H�he des bereichs
	 * @return Mat, das ausgeschnittene Bild
	 */
	public Mat getRoi(Rect roi)
	{
		this.capture.read(image);
		return new Mat(image, roi);
	}
	
	/**
	 * Wandelt ein BGR Bild in ein Bin�rbild um 
	 * 
	 * @param img - das Bild, welches umgewandelt werden soll
	 * @param thresh - minimales Beleutungslevel
	 */
	public static void toBinaryImage(Mat img, int thresh)
	{
		Imgproc.cvtColor(img, img, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(img, img, new Size(9, 9), 2, 2);
        Imgproc.threshold(img, img, 60, 255, Imgproc.THRESH_BINARY_INV);
        Cam.reduceNoise(img);
	}
	
	/**
	 * Diese Methode berechnet aus einem Bin�rbild, in dem eine Linienkontour enthalten ist
	 * eine Abweichung. Dazu wird in die Mitte ein Koordinatensystem gezeichnet von dem aus die Position 
	 * bestimmt wird, welche sp�ter als abweichung in den PID-Controller gespei�t wird. 
	 * 
	 * 
	 * @param binMat - Das Bin�re bild, in der sich eine Linien Kontour befindet.
	 * @param min - Minimalste Masse eines Objektes, um als Linie wahrgenommenwerden zu k�nnen. 
	 * @return
	 */
	public static double getError(Mat binMat, int min)
	{
        List<MatOfPoint> cont = new ArrayList<MatOfPoint>();
        /*https://docs.opencv.org/2.4/modules/imgproc/doc/structural_analysis_and_shape_descriptors.html?highlight=findcontours#findcontours*/
        /*Finde alle Konturen innerhalb des Bildes*/
        Imgproc.findContours(binMat, cont, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        double error = 0;
        for(MatOfPoint mop : cont)
        {
        	Moments mu = Imgproc.moments(mop, false);
        	/*Falls die masse g��er ist, dann berechne die x koordinate des Objektes*/ 
        	if(mu.get_m00() > min)
        	{
        		/*Berechne die Position der Kontur anhand und verschiebe das Koordinatensysytem so, dass der Bereich zwischen -1 und 1 liegt.*/
                int cx = (int) (mu.get_m10() / mu.get_m00());
                double c = 2.0f * cx / binMat.width() - 1.0f;
                /*Nimm den Fehler, welcher 0 am n�chsten ist */
                if(error == 0) error = c;
                else if(Math.abs(c) < Math.abs(error)) error = c;
        	}
    	}
        return error;
	}
	
	/**
	 * Reduziert St�rger�usche.
	 */
	public static void reduceNoise(Mat binMat)
	{
        Imgproc.erode(binMat, binMat, Cam.erodeKernel);
        Imgproc.dilate(binMat, binMat, Cam.dilateKernel);
	}
}
