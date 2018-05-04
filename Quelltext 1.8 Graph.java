package me.knuth.path.dijkstra;

import java.util.ArrayList;
import java.util.List;

/**
 * Diese Klasse dient der Abspeicherung mehrere Kanten und bietet diverse Zugriffsmethoden. 
 * @author Alexander Knuth
 *
 */
public class Graph 
{
	private List<Vertex> vertecies;
	
	public Graph(List<Vertex> list)
	{
		this.vertecies = new ArrayList<Vertex>(list);
	}
	
	public Graph()
	{
		this.vertecies = new ArrayList<Vertex>();
	}
	
	public Vertex createVertex(int id, String name)
	{
		Vertex vertex = new Vertex(id, name);
		this.vertecies.add(vertex);
		return vertex;
	}
	
	public Vertex getVertex(int id)
	{
		for(Vertex vert : this.vertecies)
		{
			if(vert.getId() == id) return vert;
		}
		return null;
	}
	
	public List<Vertex> getVertecies()
	{
		return this.vertecies;
	}
}
