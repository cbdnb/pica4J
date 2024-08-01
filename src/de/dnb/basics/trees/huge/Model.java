package de.dnb.basics.trees.huge;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import javax.swing.JTree;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import de.dnb.basics.collections.ListUtils;
import de.dnb.basics.filtering.RangeCheckUtils;
import de.dnb.basics.trees.ITreeFunction;
import de.dnb.basics.utils.OutputUtils;

/**
 * Klasse, die Bäume von T-Objekten modelliert. Diese Bäume können im
 * Prinzip auch unendlich sein, da nur der Teil gezeichnet wird, der
 * auch angezeigt werden soll.
 *
 * Die Bäume werden durch
 * eine Abbildung
 * <br>parent -> (child1, child2 ...)
 * <br>repräsentiert.
 * Einzig die Wurzel ist nicht vom Typ T, sondern ein
 * {@link DefaultMutableTreeNode}. Das nutzt die Klasse
 * {@link Renderer}, um die Knoten zu unterscheiden.
 *
 * @author baumann
 *
 * @param <T>   Typ der Baum-Objekte
 */
public class Model<T> implements TreeModel {

    private final Object root;

    private final ITreeFunction<T> treeFunction;

    private final Collection<T> topTerms;

    /**
     * Macht einen Baum, bei dem die Wurzel von anderem Typ ist als alle
     * anderen Elemente, da diese mehrere Teilbäume zusammenfasst.
     *
     * @param rootname      Name des Baums
     * @param topTerms      Liste der Top-Terms, nicht leer
     * @param treeFunction  Funktion, die einem Knoten die Kinder zuordnet
     */
    public Model(
        final String rootname,
        final Collection<T> topTerms,
        final ITreeFunction<T> treeFunction) {
        RangeCheckUtils.assertReferenceParamNotNull("topTerms", topTerms);
        RangeCheckUtils.assertReferenceParamNotNull("treeFunction",
            treeFunction);
        this.root = new DefaultMutableTreeNode(rootname);
        this.topTerms = topTerms;
        this.treeFunction = treeFunction;
    }

    /**
     *
     * @param root          Wurzel des Baums
     * @param treeFunction  Funktion, die einem Knoten die Kinder zuordnet
     */
    public Model(final T root, final ITreeFunction<T> treeFunction) {
        RangeCheckUtils.assertReferenceParamNotNull("treeFunction",
            treeFunction);
        this.root = root;
        this.treeFunction = treeFunction;
        this.topTerms = null;
    }

    @Override
    public Object getRoot() {
        return root;
    }

    /**
     *
     * @param parent    beliebig
     * @return          Kinder, nicht null
     */
    private Collection<T> getChildren(final Object parent) {
        if (parent instanceof DefaultMutableTreeNode)
            return topTerms;
        @SuppressWarnings("unchecked")
        final
        T content = (T) parent;
        Collection<T> collection = treeFunction.apply(content);
        if (collection == null)
            collection = Collections.emptyList();
        return collection;
    }

    @Override
    public final Object getChild(final Object parent, final int index) {
        final AbstractList<T> list = ListUtils.convertToList(getChildren(parent));
        return list.get(index);
    }

    @Override
    public final int getChildCount(final Object parent) {
        return getChildren(parent).size();
    }

    @Override
    public final boolean isLeaf(final Object node) {
        return getChildren(node).isEmpty();
    }

    @Override
    public void valueForPathChanged(final TreePath path, final Object newValue) {
        // TODO Auto-generated method stub
    }

    @Override
    public final int getIndexOfChild(final Object parent, final Object child) {
        final AbstractList<T> list = ListUtils.convertToList(getChildren(parent));
        return list.indexOf(child);
    }

    @Override
    public void addTreeModelListener(final TreeModelListener l) {
        // TODO Auto-generated method stub
    }

    @Override
    public void removeTreeModelListener(final TreeModelListener l) {
        // TODO Auto-generated method stub
    }

    public static void main(final String[] args) {
        final ITreeFunction<Integer> function = new ITreeFunction<Integer>() {
            @Override
            public Collection<Integer> apply(final Integer x) {
                return Arrays.asList(1,2,3);
            }
        };

        final Model<Integer> model = new Model<Integer>(null, function);
        final JTree jTree = new JTree(model);

        OutputUtils.show(jTree, "Demo");
    }

}
