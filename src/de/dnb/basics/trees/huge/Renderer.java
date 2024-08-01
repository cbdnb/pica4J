package de.dnb.basics.trees.huge;

import java.awt.Component;
import java.util.function.Function;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * Renderer zu {@link Model}. Benutzung:
 * <code><br>jtree.setCellRenderer(new MyTreeModelCellRenderer<T>
 * (textfunction))</code><br>wobei textfunction eine Funktion T -> String
 * sein muss.
 *
 * @author baumann
 *
 * @param <T>   Typ der Baum-Objekte
 */
public class Renderer<T> extends DefaultTreeCellRenderer {

    /**
     *
     */
    private static final long serialVersionUID = -4946837867061888539L;
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
        // keine Wurzel:
        if (!(value instanceof DefaultMutableTreeNode)) {
            setText(textFunction.apply((T) value));
        }
        return this;
    }

}
