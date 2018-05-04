package me.knuth.path.dijkstra;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DijkstraAlgorithm 
{
	private Graph graph;
	/*Speichert für jeden Knoten den Vorgänger mit der kürzesten Gesamtdistanz*/
	private Map<Vertex, VertexPredecessor> predecessors;
	private Vertex start;
	
	public DijkstraAlgorithm(Graph graph)
	{
		this.graph = graph;
	}
	
	/**
	 * Berechnet für jeden Knoten im Graphen die kürzeste Distanz
	 * und speichert die Vorgänger für die kürzeste Distanz für diese, 
	 * um später einen Weg finden zu können. 
	 * 
	 * @param source - Der Startknoten
	 */
	public void execute(Vertex source)
	{
		this.start = source;
		this.predecessors = new HashMap<Vertex, VertexPredecessor>();
		Set<Vertex> unvisited = new HashSet<Vertex>();
		
		/*Setzt alle Distanten auf unendlich*/
		for(Vertex vertex: this.graph.getVertecies())
		{
			this.predecessors.put(vertex, new VertexPredecessor(Integer.MAX_VALUE, null, null));
		}
		
		/*Setzt die Distanz des Startknoten auf null und fügt ihn der Unbesuchtliste zu*/
		this.predecessors.get(source).setDistance(0);
		unvisited.add(source);
		
		/*Solange Unbesuchtliste nicht leer ist*/
		while(!unvisited.isEmpty())
		{
			/*Finde den Knoten mit der vom jetzigen Zeitpunkt betrachtet kleinsten distanz*/
			Vertex vertex = null;
			for(Vertex unvisitedVertex : unvisited)
			{
				if(vertex == null) vertex = unvisitedVertex;
				else if(this.predecessors.get(unvisitedVertex).getDistance() < this.predecessors.get((vertex)).getDistance()) vertex = unvisitedVertex;
			}
			/*Entferne diesen aus der Liste, da dieser jetzt besucht wird*/
			unvisited.remove(vertex);
			
			/*Update die Distanzen für alle Nachbarn des Knoten*/
			for(Edge e : vertex.getEdges())
			{
				int distance = this.predecessors.get(vertex).getDistance() + e.getWeight();
				/*Falls die jetzige Distanz kleiner als eine Bekannte/Unendlich ist, dann setze den Vorgänger des Knoten auf diesen und update die Distanz*/
				if(distance < this.predecessors.get(e.getDestination()).getDistance())
				{
					this.predecessors.put(e.getDestination(), new VertexPredecessor(distance, vertex, e));
					/*Füge diesen Knoten der Unbesuchtliste zu, um die benachbarten Knoten von diesem zu besuchen*/
					unvisited.add(e.getDestination());
				}
			}
		}
	}
	
	public Path createPath(Vertex destination)
	{
        /*Erzeugt Pfad mit der "rückführungs" Methode*/
		Path p = new Path(this.start);
		Vertex step = destination;
		VertexPredecessor predecessor = null;
		while((predecessor = this.predecessors.get(step)).getPredecessor() != null)
		{
			p.add(predecessor.getEdge());
			step = predecessor.getPredecessor();
		}
		return p;
	}
	
	public Graph getGraph()
	{
		return this.graph;
	}
}
