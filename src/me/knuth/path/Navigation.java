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
 * @author Alex
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
		/*Vorfertigung der eines Strﬂennetzes*/
		Graph g = new Graph();
		
		/*Vertex va = g.createVertex(0, "Punkt-A");
		Vertex vb = g.createVertex(1, "Punkt-B");
		Vertex vc = g.createVertex(2, "Punkt-C");
		Vertex vd = g.createVertex(3, "Punkt-D");
		Vertex ve = g.createVertex(4, "Punkt-E");
		
		va.addAjecentVertex(vc, 47, HSVScalColor.BLUE);
		va.addAjecentVertex(ve, 50, HSVScalColor.RED_1);
		vc.addAjecentVertex(vb, 17, HSVScalColor.YELLOW);
		vb.addAjecentVertex(ve, 20, HSVScalColor.BLUE);
		ve.addAjecentVertex(vd, 33, HSVScalColor.YELLOW);
		vd.addAjecentVertex(vc, 40, HSVScalColor.GREEN);*/
		Vertex va = g.createVertex(0, "Punkt-A");
		Vertex vb = g.createVertex(1, "Punkt-B");
		Vertex vc = g.createVertex(2, "Punkt-C");
		Vertex vd = g.createVertex(3, "Punkt-D");
		Vertex ve = g.createVertex(4, "Punkt-E");
		Vertex vf = g.createVertex(5, "Punkt-F");
		Vertex vg = g.createVertex(6, "Punkt-G");
		Vertex vh = g.createVertex(7, "Punkt-H");
		Vertex vi = g.createVertex(8, "Punkt-I");
		Vertex vj = g.createVertex(9, "Punkt-J");
		
		va.addAjecentVertex(vb, 50, HSVScalColor.GREEN);
		va.addAjecentVertex(vd, 32, HSVScalColor.RED_1);
		vb.addAjecentVertex(vc, 39, HSVScalColor.RED_1);
		vc.addAjecentVertex(vf, 35, HSVScalColor.BLUE);
		vf.addAjecentVertex(vh, 28, HSVScalColor.GREEN);
		vh.addAjecentVertex(ve, 52, HSVScalColor.RED_1);
		ve.addAjecentVertex(vb, 25, HSVScalColor.BLUE);
		ve.addAjecentVertex(vd, 34, HSVScalColor.YELLOW);
		ve.addAjecentVertex(vg, 27, HSVScalColor.GREEN);
		vd.addAjecentVertex(vg, 72, HSVScalColor.BLUE);
		vg.addAjecentVertex(vi, 33, HSVScalColor.RED_1);
		vg.addAjecentVertex(vj, 42, HSVScalColor.YELLOW);
		
		
		List<Vertex> vertecies = g.getVertecies();
		DijkstraAlgorithm algo = new DijkstraAlgorithm(g);
		
		int start = 0;
		int dest = 0;
		
		GraphicsLCD display = BrickFinder.getDefault().getGraphicsLCD();
		
		/*Auswahl  der Start und ZielKnoten*/
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
		algo.execute(vertecies.get(start));
		
		/*Erzeuge den Weg zum Zielknoten*/
		Path p = algo.createPath(vertecies.get(dest));
		
		PIDController pid = new PIDController(MotorPort.A, MotorPort.B, 80, 1f, 1f, 0.01f, 0.3f, 0);
		Cam cam = new Cam(160, 120);
		
		Mode mode = Mode.START;
		
		if(p.isDone()) return;
		
		HSVScalColor currentColor = p.getCurrent().getColor();
		
		int roiHeight = 30;
		
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
			if(mode == Mode.FOLLOWING_PATH)
			{
				roi.copyTo(followRoi);
				roi.copyTo(colorRoi);
				Cam.toBinaryImage(followRoi, 60);
		        Cam.reduceNoise(followRoi);
		        pid.updatePID((float) Cam.getError(followRoi, 100));
		        if(Cam.getError(currentColor.getFiltered(colorRoi), 300) != 0 && System.currentTimeMillis() - leftVertex >= 2000)
		        {
					p.visitCurrent();
					if(!p.isDone())
					{
				        mode = Mode.ON_VERTEX;
				        pid.setSpeed(30);
				        pid.setKP(3f);
						System.out.println("Goal: " + p.getCurrent().getDestination().getName() + " via " + p.getCurrent().getColor());
					}
					else
					{
						mode = Mode.END;
					}
		        }
			}
			if(mode == Mode.ON_VERTEX)
			{
				roi.copyTo(followRoi);
				roi.copyTo(colorRoi);
				System.out.println("looking for " + p.getCurrent().getColor());
				if(Cam.getError(p.getCurrent().getColor().getFiltered(colorRoi), 200) != 0)
				{
					currentColor = p.getCurrent().getColor();
				}
				Mat bin = currentColor.getFiltered(followRoi);
				Cam.reduceNoise(bin);
				float error = (float) Cam.getError(bin, 200);
				System.out.println(currentColor + " " + error);
				if(error == 0 && currentColor == p.getCurrent().getColor())
				{
					mode = Mode.FOLLOWING_PATH;
					pid.setKP(1.5f);
					pid.setSpeed(100);
					leftVertex = System.currentTimeMillis();
				}
				pid.updatePID(error);
			}	
			if(mode == Mode.END)
			{
				roi.copyTo(followRoi);
				float error = (float) Cam.getError(currentColor.getFiltered(followRoi), 200);
				if(error == 0) end = true;
				pid.updatePID(error);
			}
		}
		Sound.beep();
	}
}
