package de.dnb.basics.trees;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import de.dnb.basics.collections.Multimap;
import de.dnb.basics.filtering.RangeCheckUtils;

public abstract class TreeFactory<T> {

    protected ITreeFunction<T> treeFunction;
    protected Collection<T> topTerms;
    protected Function<T, String> textfunction;
    protected Consumer<Collection<T>> reporterFunction;
    protected String rootname;
    protected T root;

    /**
     * Name des Baums.
     *
     * @param rootname  Wenn null, wird kein Name angezeigt
     */
    public void setRootname(final String rootname) {
        this.rootname = rootname;
    }

    /**
     * Wurzel des Baums.
     *
     * @param root  Wurzel für den Fall, dass kein zusammenfassender Name
     *              benötigt wird
     */
    public void setRoot(final T root) {
        this.root = root;
    }

    /**
     * Erzeugt den Baum.
     * Ohne textfunction und topTerms wird eine
     * {@link IllegalStateException} geworfen.
     *
     * @param hasSingleRoot hat nur einen Teilbaum? Dann ist die Wurzel
     *                      vom selben Typ wie der Rest und es werden keine
     *                      Topterms benötigt.
     *                      Sonst ist die Wurzel von Typ
     *                      {@link DefaultMutableTreeNode} und fasst die
     *                      Teilbäume zusammen
     *
     * @return  neuer Baum.
     */
    public abstract JTree createTree(boolean hasSingleRoot);

    /**
     * Einfacher TreeSelectionListener wird an den Baum angehängt. Die
     * Consumer-Funktion kann den Wert des angeklickten Knoten verarbeiten.
     * setNodeSelectionListener() muss VOR der Erzeugung des Baums aufgerufen
     * werden.
     *
     * @param consumer  Nimmt einen Knotenwert entgegen und macht etwas
     *                  daraus.
     */
    public abstract void setNodeSelectionListener(final Consumer<T> consumer);

    /**
     * Setzt die Funktion, die dem Knoten seine Kinder zuordnet.
     *
     * @param treeFunction  nicht null
     */
    public final void setTreeFunction(final ITreeFunction<T> treeFunction) {
        RangeCheckUtils.assertReferenceParamNotNull("treeFunction",
            treeFunction);
        this.treeFunction = treeFunction;
    }

    /**
     * Setzt die Funktion, die dem Knoten seine Kinder zuordnet (gewonnen
     * aus multimap).
     *
     * @param multimap  nicht null
     */
    public final void setTreeFunction(final Multimap<T, T> multimap) {
        this.treeFunction = TreeUtils.getTreeFunction(multimap);
    }

    /**
     * Setzt die Elemente, die an der Spitze sind (unterhalb vom Baumnamen).
     *
     * @param topTerms  nicht null, auch leer
     */
    public final void setTopTerms(final Collection<T> topTerms) {
        RangeCheckUtils.assertReferenceParamNotNull("topTerms", topTerms);
        this.topTerms = topTerms;
    }

    /**
     * Setzt die Elemente, die an der Spitze sind (unterhalb vom Baumnamen).
     *
     * @param topTerms  auch leer
     */
    public final void setTopTerms(final T... topTerms) {
        this.topTerms = Arrays.asList(topTerms);
    }

    /**
     * Vereinfachte Funktion, die aus multimap die Topterms extrahiert.
     *
     * @param multimap  nicht null
     */
    public final void setTreeFunctionAndTopterms(final Multimap<T, T> multimap) {
        this.treeFunction = TreeUtils.getTreeFunction(multimap);
        this.topTerms = TreeUtils.getTopTerms(multimap);
    }

    /**
     * Setzt die Beschriftung der Knoten.
     *
     * @param textfunction  wenn null, dann wird toString() verwendet
     */
    public final void setTextfunction(final Function<T, String> textfunction) {
        this.textfunction = textfunction;
    }

    /**
     * Setzt eine Funktion, die doppelte Objekte behandelt. Diese Funktion
     * nimmt den Pfad von der Wurzel bis zum aktuellen (falschen) Knoten
     * entgegen. Wird aber nicht von allen Implementierungen benötigt.
     *
     * @param reporterFunction  nicht null
     */
    public final void setReporterFunction(
        final Consumer<Collection<T>> reporterFunction) {
        RangeCheckUtils.assertReferenceParamNotNull("reporterFunction",
            reporterFunction);
        this.reporterFunction = reporterFunction;
    }

}
