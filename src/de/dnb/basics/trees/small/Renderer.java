package de.dnb.basics.trees.small;

import java.awt.Component;
import java.util.function.Function;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

public class Renderer<T> extends DefaultTreeCellRenderer {

    private final Function<T, String> textFunction;

    /**
     *
     * @param textFunction  Funktion, die dem eingebetteten Objekt einen
     *                      Text zuordnet. Wenn null, wird toString()
     *                      aufgerufen.
     */
    public Renderer(final Function<T, String> textFunction) {
        super();
        if (textFunction == null)
            this.textFunction = new Function<T, String>() {
                @Override
                public String apply(final T x) {
                    return "" + x;
                }
            };
        else
            this.textFunction = textFunction;
    }

    @Override
    public final Component getTreeCellRendererComponent(
        final JTree tree,
        final Object value,
        final boolean sel,
        final boolean expanded,
        final boolean leaf,
        final int row,
        final boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
            row, hasFocus);
        if (value instanceof TypedTreeNode<?>) {
            @SuppressWarnings("unchecked")
            final
            TypedTreeNode<T> node = (TypedTreeNode<T>) value;
            final T userObject = node.getUserObject();
            setText(textFunction.apply(userObject));
        }
        return this;
    }

}
