package de.dnb.basics.trees;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.function.Function;

import javax.swing.JTree;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import de.dnb.basics.applicationComponents.LFUCachedFunction;
import de.dnb.basics.collections.ListMultimap;
import de.dnb.basics.collections.ListUtils;
import de.dnb.basics.collections.Multimap;
import de.dnb.basics.filtering.RangeCheckUtils;
import de.dnb.basics.trees.TreeVisitor.TreeOrder;
import de.dnb.basics.trees.small.TypedTreeNode;
import de.dnb.basics.utils.OutputUtils;

public class TreeUtils {

    private TreeUtils() {
    }

    public static void main(final String[] args) {

        final TreeFactory<Integer> factory = getHugeTreeFactory();
        factory.setTextfunction(null);
        final ITreeFunction<Integer> function = new ITreeFunction<Integer>() {
            @Override
            public Collection<Integer> apply(final Integer x) {
                return Arrays.asList(1, 2, 3);
            }
        };
        final ITreeFunction<Integer> cached = cache(function);
        factory.setTreeFunction(cached);
        factory.setTopTerms(1);

        factory.setRootname("zz");
        final JTree tree = factory.createTree(false);
        OutputUtils.show(tree, "Demo");
    }

    /**
     * Verbirgt die Wurzel des Baums.
     *
     * @param jTree nicht null
     */
    public static void hideRoot(final JTree jTree) {
        jTree.setRootVisible(false);
        jTree.setShowsRootHandles(true);
    }

    /**
     *
     * @param jTree nicht null
     * @return      eine Multimap, die zu jedem Knoten den Pfad zu diesem
     *              Knoten enthält. Bei Zyklen  wird der Zyklus auf stdout
     *              ausgegeben.
     */
    public static Multimap<Object, TreePath> getAllPaths(final JTree jTree) {
        final Multimap<Object, TreePath> multimap =
            new ListMultimap<Object, TreePath>();
        final TreeVisitor visitor = new TreeVisitor(jTree, (x, y) ->
        {
            final TreePath path = new TreePath(y.toArray());
            final Object last = ListUtils.getLast(y);
            multimap.add(last, path);
        });
        visitor.visitAll(TreeOrder.POSTORDER);
        return multimap;
    }

    /**
     * Sucht alle Knoten vom Typ {@link TypedTreeNode} auf und trägt sie in
     * eine Multimap
     * <br>UserObject -> (Nodes)
     * <br>ein. Grund: Ein User-Objekt kann mehrmals in einem Baum auftauchen.
     *
     * @param tree  nicht null
     * @param <T>   Typ der User-Objekte
     * @return      Multimap, die User-Objekte auf Collection der zugehörigen
     *              Treenodes abbildet
     */
    public static <T> Multimap<T, TypedTreeNode<T>> createMultimap(
        final JTree tree) {
        final Multimap<T, TypedTreeNode<T>> multimap =
            new ListMultimap<T, TypedTreeNode<T>>();
        final TreeNode root = (TreeNode) tree.getModel().getRoot();
        addChildrenToMultimap(multimap, root);
        return multimap;
    }

    /**
     * Sucht rekursiv alle Kinder von node auf (preorder) und trägt sie
     * in multimap ein, wenn sie vom Typ {@link TypedTreeNode} sind.
     *
     * @param multimap  nicht null
     * @param node      nicht null
     * @param <T>   Typ der User-Objekte
     *
     */
    public static <T> void addChildrenToMultimap(
        final Multimap<T, TypedTreeNode<T>> multimap,
        final TreeNode node) {

        if (node instanceof TypedTreeNode<?>) {
            final T userObject = (T) ((TypedTreeNode) node).getUserObject();
            multimap.add(userObject, (TypedTreeNode<T>) node);
        }

        if (node.getChildCount() >= 0) {
            for (int i = 0; i < node.getChildCount(); i++) {
                final TreeNode childNode = node.getChildAt(i);
                addChildrenToMultimap(multimap, childNode);
            }
        }
    }

    /**
     * Expandiert oder kollabiert einen Baum.
     *
     * @param tree      nicht null
     * @param expand    expandiert, wenn true; kollabiert, wenn false
     */
    public static void expandAll(final JTree tree, final boolean expand) {
        RangeCheckUtils.assertReferenceParamNotNull("tree", tree);
        final TreeModel model = tree.getModel();
        final Object root = model.getRoot();
        final TreePath pathToRoot = new TreePath(root);
        // Traverse tree from root
        if (expand) {
            TreeUtils.expandNodeAndChildren(tree, pathToRoot, expand);
        } else {
            /*
             * Man darf die Wurzel nicht einklappen, sonst kommt
             * man nie mehr mit der Maus an die Kinder heran!
             *
             * also werden nur die Kinder eingeklappt:
             */
            for (int i = 0; i < model.getChildCount(root); i++) {
                final Object child = model.getChild(root, i);
                final TreePath path = pathToRoot.pathByAddingChild(child);
                expandNodeAndChildren(tree, path, false);
            }
        }
    }

    /**
     * Hilfsfunktion, die rekursiv aufgerufen wird. Das Ein- oder
     * Ausklappen erfolgt Postorder.
     *
     * @param tree
     * @param pathToNode
     * @param expand
     */
    public static void expandNodeAndChildren(
        final JTree tree,
        final TreePath pathToNode,
        final boolean expand) {
        // Traverse children
        final Object parent = pathToNode.getLastPathComponent();
        final TreeModel model = tree.getModel();
        for (int i = 0; i < model.getChildCount(parent); i++) {
            final Object child = model.getChild(parent, i);
            final TreePath path = pathToNode.pathByAddingChild(child);
            expandNodeAndChildren(tree, path, expand);
        }

        // Expansion or collapse must be done bottom-up
        if (expand) {
            tree.expandPath(pathToNode);
        } else {
            tree.collapsePath(pathToNode);
        }
    }

    /**
     * Hilfsmethode, die die Wurzeln eines Baumes ermittelt. Diese können
     * dann mittels {@link #createTree(String, Collection)} weiterverarbeitet
     * werden.
     *
     * @param <T>       Typ
     * @param multimap  nicht null
     * @return          Top Terms. Zyklen können keine Top terms entahlten
     *                  und werden daher nicht meher erkannt.
     */
    public static <T> Collection<T> getTopTerms(final Multimap<T, T> multimap) {
        RangeCheckUtils.assertReferenceParamNotNull("multimap", multimap);
        final LinkedHashSet<T> topTerms = new LinkedHashSet<T>(multimap.getKeySet());
        for (final T t : multimap) {
            final Collection<T> values = multimap.get(t);
            topTerms.removeAll(values);
        }
        return topTerms;
    }

    /**
     * Hilfsmethode. Macht aus einer Multimap eine Funktion, die im
     * Konstruktor TypedTreeNodeFactory() benötigt wird.
     * @param <T>       Typ der Knotenelemente
     * @param multimap  nicht null
     *
     * @return          treeFunction
     */
    public static <T> ITreeFunction<T> getTreeFunction(
        final Multimap<T, T> multimap) {
        RangeCheckUtils.assertReferenceParamNotNull("multimap", multimap);
        return new ITreeFunction<T>() {
            @Override
            public Collection<T> apply(final T x) {
                return multimap.getNullSafe(x);
            }
        };
    }

    /**
     *
     * @param treeFunction  nicht null
     * @param <T>           Typ
     * @return              Memory-Funktion
     */
    public static
        <T>
        ITreeFunction<T>
        cache(final ITreeFunction<T> treeFunction) {
        RangeCheckUtils.assertReferenceParamNotNull("treeFunction",
            treeFunction);
        final Function<T, Collection<T>> cachedFunction =
            LFUCachedFunction.create(treeFunction);
        final ITreeFunction<T> returnFunction = new ITreeFunction<T>() {
            @Override
            public Collection<T> apply(final T t) {
                return cachedFunction.apply(t);
            }
        };
        return returnFunction;
    }

    public static <T> TreeFactory<T> getHugeTreeFactory() {
        return new de.dnb.basics.trees.huge.HugeFactory<>();
    }

    public static <T> TreeFactory<T> getSmallTreFactory() {
        return new de.dnb.basics.trees.small.SmallFactory<>();
    }

}
