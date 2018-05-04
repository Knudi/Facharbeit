package me.knuth.path;

import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.hardware.port.MotorPort;
import lejos.utility.Delay;
import me.knuth.path.dijkstra.DijkstraAlgorithm;
import me.knuth.path.dijkstra.Graph;
import me.knuth.path.dijkstra.Path;
import me.knuth.path.dijkstra.Vertex;
import me.knuth.path.drivecontroller.Cam;
import me.knuth.path.drivecontroller.HSVScalColor;
import me.knuth.path.drivecontroller.PIDController;


/**
 * Die Klasse Navigation, welche die Komponenten Routenplaner und Navigation vereint.
 * 
 * @author Alexander Knuth
 *
 */
public class Navigation 
{
	private static enum Mode
	{
		START, FOLLOWING_PATH, ON_VERTEX, END;
	}
	
	public static void main(String[] args)
	{
		/*Vorfertigung der eines Strßennetzes*/
		Graph g = new Graph();
		
		Vertex va = g.createVertex(0, "Punkt-A");
		Vertex vb = g.createVertex(1, "Punkt-B");
		Vertex vc = g.createVertex(2, "Punkt-C");
		Vertex vd = g.createVertex(3, "Punkt-D");
		Vertex ve = g.createVertex(4, "Punkt-E");
		
		va.addAjecentVertex(vc, 47, HSVScalColor.BLUE);
		va.addAjecentVertex(ve, 50, HSVScalColor.RED_1);
		vc.addAjecentVertex(vb, 17, HSVScalColor.YELLOW);
		vb.addAjecentVertex(ve, 20, HSVScalColor.BLUE);
		ve.addAjecentVertex(vd, 33, HSVScalColor.YELLOW);
		vd.addAjecentVertex(vc, 40, HSVScalColor.GREEN);
		
		List<Vertex> vertecies = g.getVertecies();
		DijkstraAlgorithm algo = new DijkstraAlgorithm(g);
		
		int start = 0;
		int dest = 0;
		
		GraphicsLCD display = BrickFinder.getDefault().getGraphicsLCD();
		
		/*Auswahl der Start-und Zielknoten*/
		boolean canStart = false;
		while(!canStart && start > -1 && dest > -1)
		{
			display.clear();
			display.drawString("Start: " + vertecies.get(start).getName(), 0, 0, 0);
			display.drawString("Dest: " + vertecies.get(dest).getName(), 0, 15, 0);
			if(Button.RIGHT.isDown())
			{
				if(start >= vertecies.size() - 1) start = 0;
				else start++;
			}
			if(Button.LEFT.isDown())
			{
				if(start <= 0) start = vertecies.size() - 1;
				else start--;
			}
			if(Button.UP.isDown())
			{
				if(dest >= vertecies.size() - 1) dest = 0;
				else dest++;
			}
			if(Button.DOWN.isDown())
			{
				if(dest <= 0) dest = vertecies.size() - 1;
				else dest--;
			}
			if(Button.ENTER.isDown())
			{
				canStart = true;
				Sound.beep();
			}
			Delay.msDelay(100);
		}
		display.clear();
        /*Lasse Dijkstra den Graphen "bearbeiten"*/
		algo.execute(vertecies.get(start));
		
		/*Erzeuge den Weg zum Zielknoten*/
		Path p = algo.createPath(vertecies.get(dest));
		
		PIDController pid = new PIDController(MotorPort.A, MotorPort.B, 100, 1f, 1.5f, 0, 0.5f, 0);
		Cam cam = new Cam(160, 120);
		
		Mode mode = Mode.START;
		
		if(p.isDone()) return;
		
		HSVScalColor currentColor = p.getCurrent().getColor();
		
		int roiHeight = 20;
		
		System.out.println("Goal: " + p.getCurrent().getDestination().getName());
		
		long leftVertex = System.currentTimeMillis();
		boolean end = false;
		boolean starting = true;
		
		/*Die eigentliche Navigation*/
		while(!end && Button.ESCAPE.isUp())
		{
			Mat roi = cam.getRoi(new Rect(0, 20, 160, roiHeight));
			Mat followRoi = new Mat();
			Mat colorRoi = new Mat();
            /*Start der Navigation, in der der Roboter sich auf dem Knoten befindet und um sich zu Orientieren dreht*/
			if(mode == Mode.START)
			{
				roi.copyTo(followRoi);
				float error = (float) Cam.getError(currentColor.getFiltered(followRoi), 400);
				if(error == 0)
				{
					if(starting)
					{
						pid.getLeftMotor().updateSpeed(60);
						pid.getRightMotor().updateSpeed(-60);
					}
					else
					{
						mode = Mode.FOLLOWING_PATH;
						leftVertex = System.currentTimeMillis();
					}
				}
				else
				{
					starting = false;
					pid.updatePID(error);
				}
			}
            /*Der Modus, in welchem, der EV3 der scharzen Linie folgt und die farbe des Knotens sucht*/
			if(mode == Mode.FOLLOWING_PATH)
			{
				roi.copyTo(followRoi);
				roi.copyTo(colorRoi);
				Cam.toBinaryImage(followRoi, 60);
		        Cam.reduceNoise(followRoi);
		        pid.updatePID((float) Cam.getError(followRoi, 100));
                /*Wenn Farbe des Knotens gefungen und letzter Knoten vor mehr als 1 Sekunde verlassen, dämpfe Geschwindigkeit und erhhöhe kP Konstante*/
		        if(Cam.getError(currentColor.getFiltered(colorRoi), 300) != 0 && System.currentTimeMillis() - leftVertex >= 1000)
		        {
					p.visitCurrent();
                    /*Prüft, ob der EV3 den letzen Knoten anfährt*/
					if(!p.isDone())
					{
				        mode = Mode.ON_VERTEX;
				        pid.setSpeed(50);
				        pid.setKP(2.5f);
						System.out.println("Goal: " + p.getCurrent().getDestination().getName());
					}
					else
					{
						mode = Mode.END;
					}
		        }
			}
            /*Der EV3 fährt einen Knoten an*/
			if(mode == Mode.ON_VERTEX)
			{
				roi.copyTo(followRoi);
				roi.copyTo(colorRoi);
                /*Prüft, ob die Farbe der nächsten Kante sichbar ist*/
				if(Cam.getError(p.getCurrent().getColor().getFiltered(colorRoi), 400) != 0)
				{
                    /*Setze die Farbe der neuen Kante als die zu folgende Farbe*/
					currentColor = p.getCurrent().getColor();
				}
				Mat bin = currentColor.getFiltered(followRoi);
				Cam.reduceNoise(bin);
				float error = (float) Cam.getError(bin, 300);
				if(error == 0 && currentColor == p.getCurrent().getColor())
				{
					mode = Mode.FOLLOWING_PATH;
					pid.setKP(1.5f);
					pid.setSpeed(100);
					leftVertex = System.currentTimeMillis();
				}
				pid.updatePID(error);
			}	
            /*Lezter Knoten erreicht*/
			if(mode == Mode.END)
			{
                /*Solange jetzige Farbe sichtbar, fahre weiter*/
				roi.copyTo(followRoi);
				float error = (float) Cam.getError(currentColor.getFiltered(followRoi), 400);
				if(error == 0) end = true;
				pid.updatePID(error);
			}
		}
		Sound.beep();
	}
}
