package me.knuth.path.dijkstra;

import java.util.Stack;

/**
 * Eine Klasse welche die Kanten für einen  Pfad abspeichert.
 * Aufgrund der rückführenden Wegfindung des Dijkstra-Algorithmus, werden die Kanten in einem Stack gespeichert.
 * @author Alexander Knuth
 *
 */
public class Path 
{
	private Vertex start;
	private Stack<Edge> pathEdges;
	
	public Path(Vertex start)
	{
		this.start = start;
		this.pathEdges = new Stack<Edge>();
	}
	
	public void add(Edge e)
	{
		this.pathEdges.push(e);
	}
	
	public void visitCurrent()
	{
		this.pathEdges.pop();
	}
	
	public Edge getCurrent()
	{
		return this.pathEdges.peek();
	}
	
	public boolean hasNext()
	{
		return this.pathEdges.size() > 1;
	}
	
	public Edge getNext()
	{
		return this.pathEdges.get(1);
	}
	
	public Vertex getStart()
	{
		return this.start;
	}
	
	public boolean isDone()
	{
		return this.pathEdges.isEmpty();
	}
}
