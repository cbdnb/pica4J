
/*
 * (C) Copyright 2010-2023, by Tom Conerly and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * See the CONTRIBUTORS.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the
 * GNU Lesser General Public License v2.1 or later
 * which is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1-standalone.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR LGPL-2.1-or-later
 */
package de.dnb.basics.collections;

import java.util.*;
import java.util.stream.*;

/**
 * An implementation of
 * <a href="http://en.wikipedia.org/wiki/Disjoint-set_data_structure">Union
 * Find</a> data structure. Union Find is a disjoint-set data structure. It
 * supports two operations: finding the set a specific element is in, and
 * merging two sets. The implementation uses union by rank and path compression
 * to achieve an amortized cost of $O(\alpha(n))$ per operation where $\alpha$
 * is the inverse Ackermann function. UnionFind uses the hashCode and equals
 * method of the elements it operates on.
 *
 * @param <T> element type
 *
 * @author Tom Conerly
 */
public class UnionFind<T> {

	public static void main(final String[] args) {
		UnionFind<Integer> uf = new UnionFind<Integer>(null);

		uf.union(1, 2);
		uf.debug();
		uf.union(3, 4);
		uf.debug();
		uf.find(4);
		uf.debug();
		uf.union(3, 5);
		uf.debug();
		uf.union(3, 6);
		uf.debug();
		uf.union(2, 5);
		uf.debug();

	}

	private final Map<T, T> parentMap;
	private final Map<T, Integer> sizeMap;
	private final Map<T, Set<T>> childrenMap;
	private final Set<T> roots;

	public int getNumberOfClusters() {
		return numberOfClusters;
	}

	public Set<T> getRoots() {
		return roots;
	}

	private int numberOfClusters; // number of nonequivalent components

	/**
	 * Creates a UnionFind instance with all the elements in separate sets.
	 * 
	 * @param elements the initial elements to include (each element in a singleton
	 *                 set) or null;
	 */
	public UnionFind(Set<T> elements) {
		parentMap = new LinkedHashMap<>();
		childrenMap = new LinkedHashMap<>();
		sizeMap = new HashMap<>();
		numberOfClusters = 0;
		roots = new LinkedHashSet<T>();
		if (elements != null)
			for (T element : elements) {
				add(element);
			}
	}

	/**
	 * Adds a new element to the data structure in its own set.
	 *
	 * @param element The element to add. Auch null, dann geschieht nichts.
	 */
	public void addElement(T element) {
		if (element == null || parentMap.containsKey(element))
			return;
		add(element);
	}

	/**
	 * 
	 * @param element
	 */
	private void add(T element) {
		parentMap.put(element, element);
		childrenMap.put(element, new HashSet<>());
		sizeMap.put(element, 1);
		roots.add(element);
		numberOfClusters++;
	}

	/**
	 * @return map from element to parent element
	 */
	protected Map<T, T> getParentMap() {
		return parentMap;
	}

	/**
	 * @return map from element to rank
	 */
	protected Map<T, Integer> getRankMap() {
		return sizeMap;
	}

	/**
	 * Returns the representative element of the set that element is in. The path to
	 * the root is compressed.
	 *
	 * @param element The element to find.
	 *
	 * @return The element representing the set the element is in or null.
	 */
	public T find(final T element) {
		if (!parentMap.containsKey(element)) {
			return null;
		}

		T current = element;
		while (true) {
			T parent = parentMap.get(current);
			if (parent.equals(current)) {
				break;
			}
			current = parent;
		}

		// Pfadkompression:
		final T root = current;
		Set<T> rootChildren = childrenMap.get(root);
		current = element;
		T parent = parentMap.get(current);
		while (!parent.equals(root)) {
			int currentsize = sizeMap.get(current);
			parentMap.put(current, root);

			// size und children korrigieren:
			int parentsize = sizeMap.get(parent);
			parentsize -= currentsize;
			if (parentsize <= 0)
				throw new IllegalStateException();
			sizeMap.put(parent, parentsize);
			Set<T> parChildren = childrenMap.get(parent);
			parChildren.remove(current);
			rootChildren.add(current);

			// wechsel zum oberen Knoten:
			current = parent;
			parent = parentMap.get(current);
		}

		return root;
	}

	/**
	 * Merges the sets which contain element1 and element2. No guarantees are given
	 * as to which element becomes the representative of the resulting (merged) set:
	 * this can be either find(element1) or find(element2). If one of the elements
	 * == null, nothing happens.
	 *
	 * @param element1 The first element to union.
	 * @param element2 The second element to union.
	 */
	public void union(T element1, T element2) {
		if (element1 == null || element2 == null)
			return;
		T parent1;
		T parent2;
		if (!parentMap.containsKey(element1)) {
			add(element1);
			parent1 = element1;
		} else {
			parent1 = find(element1);
		}

		if (!parentMap.containsKey(element2)) {
			add(element2);
			parent2 = element2;
		} else {
			parent2 = find(element2);
		}

		// check if the elements are already in the same set
		if (parent1.equals(parent2)) {
			return;
		}

		int size1 = sizeMap.get(parent1);
		int size2 = sizeMap.get(parent2);

		int total = size1 + size2;
		T newParent;
		T newChild;
		if (size1 >= size2) {
			newParent = parent1;
			newChild = parent2;
		} else {
			newParent = parent2;
			newChild = parent1;
		}

		parentMap.put(newChild, newParent);
		sizeMap.put(newParent, total);
		roots.remove(newChild);
		Set<T> children = childrenMap.get(newParent);
		children.add(newChild);

		numberOfClusters--;
	}

	/**
	 * Tests whether two elements are contained in the same set.
	 * 
	 * @param element1 first element
	 * @param element2 second element
	 * @return true if element1 and element2 are contained in the same set, false
	 *         otherwise.
	 */
	public boolean inSameSet(T element1, T element2) {
		return find(element1).equals(find(element2));
	}

	/**
	 * Returns the number of sets. Initially, all items are in their own set. The
	 * smallest number of sets equals one.
	 * 
	 * @return the number of sets
	 */
	public int numberOfClusters() {
		assert numberOfClusters >= 1 && numberOfClusters <= parentMap.keySet().size();
		return numberOfClusters;
	}

	/**
	 * Returns the total number of elements in this data structure.
	 * 
	 * @return the total number of elements in this data structure.
	 */
	public int size() {
		return parentMap.size();
	}

	/**
	 * Resets the UnionFind data structure: each element is placed in its own
	 * singleton set.
	 */
	public void reset() {
		for (T element : parentMap.keySet()) {
			parentMap.put(element, element);
			sizeMap.put(element, 1);
		}
		numberOfClusters = parentMap.size();
	}

	public Set<T> getCluster(T elem) {
		if (!parentMap.containsKey(elem))
			return Collections.emptySet();
		Set<T> result = new HashSet<>();
		T root = find(elem);
		depthFirst(root, result);
		return result;
	}

	private void depthFirst(T root, Set<T> result) {
		result.add(root);
		for (T child : childrenMap.get(root)) {
			depthFirst(child, result);
		}
	}

	/**
	 * Returns a string representation of this data structure. Each component is
	 * represented as $\left{v_i:v_1,v_2,v_3,...v_n\right}$, where $v_i$ is the
	 * representative of the set.
	 * 
	 * @return string representation of this data structure
	 */
	public String toString() {
		Map<T, Set<T>> setRep = new LinkedHashMap<>();
		roots.forEach(root -> {
			setRep.put(root, getCluster(root));
		});

		return setRep.keySet().stream()
				.map(key -> "{" + key + ":"
						+ setRep.get(key).stream().map(Objects::toString).collect(Collectors.joining(",")) + "}")
				.collect(Collectors.joining(", ", "{", "}"));
	}

	public void debug() {
		System.out.println();
		System.out.println("Number of Clusters: " + numberOfClusters);
		System.out.println("Roots: " + roots);
		System.out.println("Clusters:");
		roots.forEach(root -> {
			System.out.println(root + ": " + getCluster(root));
		});
		System.out.println("Mappings:");
		parentMap.forEach((el, parent) -> {
			String out = el + ": p-" + parent + " ch-" + childrenMap.get(el);
			out += " s-" + sizeMap.get(el);
			System.out.println(out);
		});
	}
}
