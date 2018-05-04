package me.knuth.path.dijkstra;

import java.util.ArrayList;
import java.util.List;

import me.knuth.path.drivecontroller.HSVScalColor;

/**
 * Die Knoten Klasse, welche eine Id, einen Namen und die Kanten 
 * speichert, welche zu den Nachbarknoten führen.
 * 
 * @author Alexander Knuth
 *
 */
public class Vertex 
{
	private int id;
	private String name;
	
	private List<Edge> adjecentVertecies;
	
	public Vertex(int id, String name)
	{
		this.id = id;
		this.name = name;
		this.adjecentVertecies = new ArrayList<Edge>();
	}
	
	public int getId()
	{
		return this.id;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public List<Edge> getEdges()
	{
		return this.adjecentVertecies;
	}
	
	public void addAjecentVertex(Vertex target, int weight, HSVScalColor color)
	{
		this.adjecentVertecies.add(new Edge(target, weight, color));
		target.adjecentVertecies.add(new Edge(this, weight, color));
	}
	
	@Override
	public int hashCode() 
	{
		int hash = 0;
		hash += this.name.hashCode();
		hash += this.id * 31;
		return hash;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if(obj == this) return true;
		if(obj == null || obj.getClass() != this.getClass()) return false;
		return ((Vertex)obj).getId() == this.id;
	}
	
	@Override
	public String toString() 
	{
		return this.id + "_" + this.name;
	}
}
