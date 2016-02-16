package org.kidneyomics.graph;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

public class TopologicalSorterTest {

	@Test
	public void test() {
		
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
		
		assertEquals(8,result.size());
		
		assertEquals(5,(int)result.get(0));
		assertEquals(7,(int)result.get(1));
		assertEquals(3,(int)result.get(2));
		assertEquals(11,(int)result.get(3));
		assertEquals(8,(int)result.get(4));
		assertEquals(2,(int)result.get(5));
		assertEquals(10,(int)result.get(6));
		assertEquals(9,(int)result.get(7));
		
		System.err.println(result);
	}

}
