package de.dnb.basics.trees.small;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.function.Consumer;

import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

import de.dnb.basics.collections.ListMultimap;
import de.dnb.basics.collections.Multimap;
import de.dnb.basics.trees.ITreeFunction;
import de.dnb.basics.trees.TreeFactory;
import de.dnb.basics.trees.TreeUtils;
import de.dnb.basics.utils.OutputUtils;

/**
 *
 * Factory zur Erzeugung von Bäumen. Diese nimmt zwar den Typ T entgegen,
 * implementiert aber den Baum de facto über {@link MutableTreeNode}.
 *
 * Benötigt werden:
 * <li>Ein oder mehrere Wurzelknoten
 * <li>eine Funktion, die die Kinder aufzählt.
 * <br><br>Optional werden noch benötigt:
 * <li>eine Reportfunktion, die aufgerufen wird, wenn Zyklen entdeckt werden
 *      (Default-Ausgabe auf stdout)
 * <li>eine Funktion (textFunction), die den T-Objekten eine Beschriftung
 *      zuordnet (Default ist toString())
 *
 * @author Christian_2
 *
 * @param <T>   Typ der Knotenobjekte
 */
public class SmallFactory<T> extends TreeFactory<T> {

    private final LinkedHashSet<T> path = new LinkedHashSet<>();

    private TreeSelectionListener treeSelectionListener;

    /**
     *
     * @param treeFunction  Funktion, die zu einem im Knoten enthaltenen
     *                      Objekt die Unterbegriffe ermittelt
     */
    public SmallFactory() {
        super();
        reporterFunction = x -> System.out.println("Zyklus: " + x);
    }

    @Override
    public JTree createTree(final boolean hasSingleRoot) {
        Collection<T> toptermsActual;
        DefaultMutableTreeNode rootNode;
        if (hasSingleRoot) {
            if (root == null || treeFunction == null)
                throw new IllegalStateException("noch nicht initialisiert");
            rootNode = new TypedTreeNode<T>(root);
            toptermsActual = treeFunction.apply(root);
        } else {
            if (topTerms == null || treeFunction == null)
                throw new IllegalStateException("noch nicht initialisiert");
            rootNode = new DefaultMutableTreeNode(rootname);
            toptermsActual = topTerms;
        }

        int index = 0;
        for (final T t : toptermsActual) {
            path.clear();
            path.add(t);
            rootNode.insert(makeSubTree(t), index);
            index++;
        }
        final JTree tree = new JTree();
        tree.setModel((new DefaultTreeModel(rootNode)));

        tree.setCellRenderer(new Renderer<T>(textfunction));
        if (treeSelectionListener != null)
            tree.addTreeSelectionListener(treeSelectionListener);
        return tree;
    }

    private TypedTreeNode<T> makeSubTree(final T userObject) {
        final TypedTreeNode<T> node = new TypedTreeNode<T>(userObject);
        final Collection<T> subs = treeFunction.apply(userObject);
        int index = 0;
        if (subs != null) // defensiv programmieren!
            for (final T sub : subs) {
                if (path.contains(sub)) {
                    reportCycle(sub);
                } else {
                    path.add(sub);
                    node.insert(makeSubTree(sub), index);
                    index++;
                    path.remove(sub);
                }
            }
        return node;
    }

    private void reportCycle(final T duplicate) {
        final Collection<T> extendedPath = new LinkedList<>(path);
        extendedPath.add(duplicate);
        reporterFunction.accept(extendedPath);
    }

    @Override
    public final void setNodeSelectionListener(final Consumer<T> consumer) {
        treeSelectionListener = new TreeSelectionListener() {
            @Override
            public void valueChanged(final TreeSelectionEvent event) {
                final TreePath treePath = event.getPath();
                final Object pathComponent = treePath.getLastPathComponent();
                if (pathComponent instanceof TypedTreeNode<?>) {
                    final T t = ((TypedTreeNode<T>) pathComponent).getUserObject();
                    consumer.accept(t);
                }
            }
        };
    }

    public static void main(final String[] args) {

        final Multimap<Integer, Integer> multimap =
            new ListMultimap<Integer, Integer>();
        multimap.addAll(1, 11,12);
        multimap.add(11, 1);
        final ITreeFunction<Integer> function = TreeUtils.getTreeFunction(multimap);
        final SmallFactory<Integer> factory = new SmallFactory<Integer>();
        factory.setTextfunction(null);
        factory.setTreeFunction(function);
        factory.setRoot(1);
        factory
            .setNodeSelectionListener(x -> System.out.println(x + " gewählt"));

        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                final JTree tree = factory.createTree(true);
                OutputUtils.show(tree, "Tree-Demo");
            }
        });

    }
}
