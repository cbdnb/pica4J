package de.dnb.basics.trees;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.swing.JTree;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import de.dnb.basics.collections.ListMultimap;
import de.dnb.basics.collections.Multimap;
import de.dnb.basics.filtering.RangeCheckUtils;
import de.dnb.basics.trees.huge.HugeFactory;
import de.dnb.basics.utils.OutputUtils;

public class TreeVisitor {

    /**
     * Traversierungsordnung.
     * <li>PREORDER: Zunächst der Knoten, dann die Kinder
     * <li>POSTORDER: Zunächst die Kinder, dann der Knoten.
     *
     * @author baumann
     *
     */
    public static enum TreeOrder {
        PREORDER, POSTORDER
    }

    public static void main(final String[] args) {
        final Multimap<Integer, Integer> multimap = new ListMultimap<>();
        multimap.addAll(1, 11, 12, 13);
        multimap.addAll(2, 23, 24);

        final TreeFactory<Integer> factory = new HugeFactory<>();
        factory.setTreeFunctionAndTopterms(multimap);
        factory.setRootname("zz");

        factory.setNodeSelectionListener(System.out::println);
        final JTree tree = factory.createTree(false);

        OutputUtils.show(tree, "Demo");

        final boolean expanded = false;
        expandAll(tree, expanded);

    }

    /**
     * Klappt Baum ein oder aus. Zyklen werden auf stdout ausgegeben.
     *
     * @param tree              nicht null
     * @param expanded          wenn true, ausgeklappt; wenn false,
     *                          eingeklappt.
     */
    public static void expandAll(final JTree tree, final boolean expanded) {
        RangeCheckUtils.assertReferenceParamNotNull("tree", tree);
        expandAll(tree, expanded, null);
    }

    /**
     * @param tree              nicht null
     * @param expanded          wenn true, ausgeklappt; wenn false,
     *                          eingeklappt.
     * @param reporterFunction  auch null, dann stdout
     */
    public static void expandAll(
        final JTree tree,
        final boolean expanded,
        final Consumer<Collection<Object>> reporterFunction) {
        RangeCheckUtils.assertReferenceParamNotNull("tree", tree);
        final BiConsumer<JTree, Collection<Object>> fragment =
            (x, y) ->
            {
                final TreePath path = new TreePath(y.toArray());
                if (expanded)
                    x.expandPath(path);
                // Wurzel nicht einklappen, da sonst mit Maus nicht mehr
                // zu öffnen:
                else if (y.size() > 1)
                    x.collapsePath(path);
            };

        final TreeVisitor treeVisitor = new TreeVisitor(tree, fragment);
        treeVisitor.setReporterFunction(reporterFunction);
        treeVisitor.visitAll(TreeOrder.POSTORDER);
    }

    private final LinkedHashSet<Object> path = new LinkedHashSet<>();
    private TreeModel model;
    private final JTree tree;
    private final BiConsumer<JTree, Collection<Object>> code;

    /**
     *
     * @param tree  nicht null
     * @param code  Das auszuführende Code-Fragment. Der Code nimmt 2
     *              Parameter entgegen: 1. den Baum, 2. Den Pfad zum
     *              aktuellen Knoten
     */
    public TreeVisitor(
        final JTree tree,
        final BiConsumer<JTree, Collection<Object>> code) {
        RangeCheckUtils.assertReferenceParamNotNull("tree", tree);
        RangeCheckUtils.assertReferenceParamNotNull("code", code);
        this.tree = tree;
        this.code = code;
    }

    /**
     * Besucht den Baum in der gewählten Reihenfolge.
     *
     * @param treeOrder      nicht null
     *
     */
    public void visitAll(final TreeOrder treeOrder) {
        RangeCheckUtils.assertReferenceParamNotNull("treeOrder", treeOrder);
        model = tree.getModel();
        final Object rootObject = model.getRoot();
        path.clear();
        path.add(rootObject);
        visitNodeAndChildren(rootObject, treeOrder);
    }

    /**
     * Hilfsfunktion, die rekursiv aufgerufen wird.
     */
    private void visitNodeAndChildren(
        final Object parent,
        final TreeOrder treeOrder) {
        if (treeOrder == TreeOrder.PREORDER) {
            code.accept(tree, path);
        }

        // Traverse children:
        for (int i = 0; i < model.getChildCount(parent); i++) {
            final Object child = model.getChild(parent, i);
            if (path.contains(child)) {
                reportCycle(child);
            } else {
                path.add(child);
                visitNodeAndChildren(child, treeOrder);
                path.remove(child);
            }
        }

        if (treeOrder == TreeOrder.POSTORDER) {
            code.accept(tree, path);
        }

    }

    private void reportCycle(final Object duplicate) {
        final Collection<Object> extendedPath = new LinkedList<>(path);
        extendedPath.add(duplicate);
        if (reporterFunction != null)
            reporterFunction.accept(extendedPath);
        else
            System.out.println("Zyklus: " + extendedPath);
    }

    private Consumer<Collection<Object>> reporterFunction;

    /**
     * Setzt eine Funktion, die doppelte Objekte behandelt. Diese Funktion
     * nimmt den Pfad von der Wurzel bis zum aktuellen (falschen) Knoten
     * entgegen.
     *
     * @param reporterFunction  auch null, dann wird
     * System.out.println()
     * genommen.
     */
    public final void setReporterFunction(
        final Consumer<Collection<Object>> reporterFunction) {
        this.reporterFunction = reporterFunction;
    }

}
