package me.knuth.path.dijkstra;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DijkstraAlgorithm 
{
	private Graph graph;
	/*Speichert f�r jeden Knoten den Vorg�nger mit der k�rzesten Gesamtdistanz*/
	private Map<Vertex, VertexPredecessor> predecessors;
	private Vertex start;
	
	public DijkstraAlgorithm(Graph graph)
	{
		this.graph = graph;
	}
	
	/**
	 * Berechnet f�r jeden Knoten im Graphen die k�rzeste Distanz
	 * und speichert die Vorg�nger f�r die k�rzeste Distanz f�r diese, 
	 * um sp�ter einen Weg finden zu k�nnen. 
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
		
		/*Setzt die Distanz des Startknoten auf null und f�gt ihn der Unbesuchtliste zu*/
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
			
			/*Update die Distanzen f�r alle Nachbarn des Knoten*/
			for(Edge e : vertex.getEdges())
			{
				int distance = this.predecessors.get(vertex).getDistance() + e.getWeight();
				/*Falls die jetzige Distanz kleiner als eine Bekannte/Unendlich ist, dann setze den Vorg�nger des Knoten auf diesen und update die Distanz*/
				if(distance < this.predecessors.get(e.getDestination()).getDistance())
				{
					this.predecessors.put(e.getDestination(), new VertexPredecessor(distance, vertex, e));
					/*F�ge diesen Knoten der Unbesuchtliste zu, um die benachbarten Knoten von diesem zu besuchen*/
					unvisited.add(e.getDestination());
				}
			}
		}
	}
	
	public Path createPath(Vertex destination)
	{
        /*Erzeugt Pfad mit der "r�ckf�hrungs" Methode*/
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
