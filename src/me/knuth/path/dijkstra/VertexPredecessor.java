package me.knuth.path.dijkstra;

/**
 * Eine Klasse, um den Vorgänger eines Knotens, die dazugehörige 
 * Distanz und die dafür genutze Kante zu speichern
 * 
 * @author Alexander Knuth
 *
 */
public class VertexPredecessor
{
	private int distance;
	private Vertex predecessor;
	private Edge edge;
	
	public VertexPredecessor(int distance, Vertex vert, Edge edge)
	{
		this.predecessor = vert;
		this.distance = distance;
		this.edge = edge;
	}
	
	public void setDistance(int distance)
	{
		this.distance = distance;
	}
	
	public void setPredecessor(Vertex vertex)
	{
		this.predecessor = vertex;
	}
	
	public int getDistance()
	{
		return this.distance;
	}
	
	public Vertex getPredecessor()
	{
		return this.predecessor;
	}
	
	public Edge getEdge()
	{
		return this.edge;
	}
}
