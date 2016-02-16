package org.kidneyomics.graph;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

public class TopologicalSorterTest {

	@Test
	public void testSort() {
		
		/* https://en.wikipedia.org/wiki/Topological_sorting
		 */
		DirectedNode<Integer> a = DirectedNode.create(5);
		
		DirectedNode<Integer> b = DirectedNode.create(7);
		
		DirectedNode<Integer> c = DirectedNode.create(3);
		
		DirectedNode<Integer> d = DirectedNode.create(11);
		d.addParent(a);
		d.addParent(b);
		
		DirectedNode<Integer> e = DirectedNode.create(8);
		e.addParent(b);
		e.addParent(c);
		
		DirectedNode<Integer> f = DirectedNode.create(2);
		f.addParent(d);
		
		DirectedNode<Integer> g = DirectedNode.create(9);
		g.addParent(e);
		g.addParent(d);
		
		DirectedNode<Integer> h = DirectedNode.create(10);
		h.addParent(d);
		h.addParent(c);
		
		
		List<DirectedNode<Integer>> nodes = new LinkedList<DirectedNode<Integer>>();
		
		nodes.add(a);
		nodes.add(b);
		nodes.add(c);
		nodes.add(d);
		nodes.add(e);
		nodes.add(f);
		nodes.add(g);
		nodes.add(h);
		
		List<Integer> result = TopologicalSorter.sort(nodes);
		
		System.err.println(result);
		
		assertEquals(8,result.size());
		
		assertTrue(TopologicalSorter.validOrder(result, nodes));
				
	}
	
	@Test
	public void testValidOrder() {
		DirectedNode<Integer> a = DirectedNode.create(5);
		
		DirectedNode<Integer> b = DirectedNode.create(7);
		
		DirectedNode<Integer> c = DirectedNode.create(3);
		
		DirectedNode<Integer> d = DirectedNode.create(11);
		d.addParent(a);
		d.addParent(b);
		
		DirectedNode<Integer> e = DirectedNode.create(8);
		e.addParent(b);
		e.addParent(c);
		
		DirectedNode<Integer> f = DirectedNode.create(2);
		f.addParent(d);
		
		DirectedNode<Integer> g = DirectedNode.create(9);
		g.addParent(e);
		g.addParent(d);
		
		DirectedNode<Integer> h = DirectedNode.create(10);
		h.addParent(d);
		h.addParent(c);
		
		List<DirectedNode<Integer>> nodes = new LinkedList<DirectedNode<Integer>>();
		
		nodes.add(a);
		nodes.add(b);
		nodes.add(c);
		nodes.add(d);
		nodes.add(e);
		nodes.add(f);
		nodes.add(g);
		nodes.add(h);
		
		List<Integer> order = new LinkedList<Integer>();
		order.add(9);
		order.add(10);
		order.add(2);
		order.add(8);
		order.add(11);
		order.add(3);
		order.add(7);
		order.add(5);
		
		assertFalse(TopologicalSorter.validOrder(order, nodes));
	}

}
