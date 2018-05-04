package me.knuth.path.dijkstra;

import me.knuth.path.drivecontroller.HSVScalColor;

/**
 * Diese Klasse stellt die Kanten zwischen zwei Konten dar. Sie beinhaltet
 * den Zielknoten, eine Gewichtung, und eine Farbe.
 * 
 * @author Alexander Knuth
 *
 */
public class Edge 
{
	private Vertex destination;
	private int weight;
	private HSVScalColor color;
	
	public Edge(Vertex destination, int weight, HSVScalColor color)
	{
		this.destination = destination;
		this.weight = weight;
		this.color = color;
	}
	
	public Vertex getDestination()
	{
		return this.destination;
	}
	
	public int getWeight()
	{
		return this.weight;
	}
	
	public HSVScalColor getColor()
	{
		return this.color;
	}
}
