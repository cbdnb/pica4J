package de.dnb.basics.trees.small;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * 
 * Tree-Nodes mit nur einer Art User-Objekte.
 * 
 * @author baumann
 *
 * @param <T>       Typ der User-Objekte
 */
public class TypedTreeNode<T> extends DefaultMutableTreeNode {

    @Override
    public String toString() {
        return "MyTreeNode [userObject=" + userObject + "]";
    }

    public TypedTreeNode(final T name) {
        super(name);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T getUserObject() {
        // TODO Auto-generated method stub
        return (T) super.getUserObject();
    }

}
