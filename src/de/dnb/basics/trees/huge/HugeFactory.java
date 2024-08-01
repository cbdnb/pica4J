package de.dnb.basics.trees.huge;

import java.util.function.Consumer;

import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import de.dnb.basics.collections.ListMultimap;
import de.dnb.basics.collections.Multimap;
import de.dnb.basics.trees.TreeFactory;
import de.dnb.basics.trees.TreeUtils;
import de.dnb.basics.trees.TreeVisitor;
import de.dnb.basics.utils.OutputUtils;

public class HugeFactory<T> extends TreeFactory<T> {

    private TreeSelectionListener treeSelectionListener;

    @Override
    public final void setNodeSelectionListener(final Consumer<T> consumer) {
        treeSelectionListener = new TreeSelectionListener() {
            @Override
            public void valueChanged(final TreeSelectionEvent event) {
                final TreePath treePath = event.getPath();
                final Object pathComponent = treePath.getLastPathComponent();
                if ((pathComponent instanceof DefaultMutableTreeNode)) {
                    return;
                }
                final T t = (T) pathComponent;
                consumer.accept(t);
            }
        };
    }

    @Override
    public JTree createTree(final boolean hasSingleRoot) {
        Model<T> model;
        if (hasSingleRoot) {
            if (root == null || treeFunction == null)
                throw new IllegalStateException("noch nicht initialisiert");
            model = new Model<T>(root, treeFunction);
        } else {
            if (topTerms == null || treeFunction == null)
                throw new IllegalStateException("noch nicht initialisiert");
            model = new Model<T>(rootname, topTerms, treeFunction);
        }
        final JTree tree = new JTree(model);
        tree.setCellRenderer(new Renderer<T>(textfunction));
        if (treeSelectionListener != null)
            tree.addTreeSelectionListener(treeSelectionListener);
        return tree;
    }

    public static void main(final String[] args) throws InterruptedException {

        final TreeFactory<Integer> factory = TreeUtils.getHugeTreeFactory();

        factory.setRoot(1);
        final Multimap<Integer, Integer> multimap =
            new ListMultimap<Integer, Integer>();
        multimap.addAll(1, 2, 3);
        multimap.addAll(2, 23, 24);
        multimap.addAll(23, 231, 232);
        multimap.addAll(231, 2311, 2312);
        factory.setTreeFunction(multimap);
        factory
            .setNodeSelectionListener(x -> System.out.println(x + " gewählt"));

        final JTree jTree = factory.createTree(true);

        OutputUtils.show(jTree, "Demo");
        TreeUtils.hideRoot(jTree);
        TreeVisitor.expandAll(jTree, true);

        Thread.sleep(1000);

//        TreeVisitor.expandAll(jTree, false);
    }

}
