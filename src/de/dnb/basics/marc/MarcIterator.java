/**
 *
 */
package de.dnb.basics.marc;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Stream;

import org.marc4j.MarcReader;
import org.marc4j.MarcStreamReader;
import org.marc4j.MarcXmlParserThread;
import org.marc4j.MarcXmlReader;
import org.marc4j.marc.Record;

import de.dnb.basics.applicationComponents.FileUtils;
import de.dnb.basics.applicationComponents.Streams;

/**
 * Eine Wrapper-Klasse um {@link MarcReader}, da dieser leider
 * nicht {@link Iterator} implementiert. Wegen
 * Problemen mit einigen Implementierungen von {@link MarcReader},
 * die eigene Threads starten, welche nicht ohne weiteres anhalten,
 * empfiehlt es sich, die Methode {@link #close()} aufzurufen,
 * wenn Programme partout nicht enden wollen.
 *
 * @author baumann
 *
 */
public class MarcIterator
    implements Iterator<Record>, Iterable<Record>, Closeable {

    private final MarcReader marcReader;

    /**
     *  Liefert einen {@link Stream}. Aber Vorsicht: die
     *  Implementierung des zugrundeliegenden {@link MarcReader}s
     *  bewirkt, dass manche Stream-Operationen wie {@link Stream#limit(long)}
     *  auf XML-Files nicht richtig funktionieren! Zur Sicherheit sollte man
     *  daher die Methode {@link #close()} aufrufen, die den entsprechenden
     *  {@link MarcXmlParserThread} abwürgt.
     *
     * @return  einen {@link Stream} von {@link Record}s
     */
    public Stream<Record> stream() {
        return Streams.getStreamFromIterable(this);
    }

    /**
     *
     * @param marcReader    nicht null
     */
    public MarcIterator(final MarcReader marcReader) {
        Objects.requireNonNull(marcReader);
        this.marcReader = marcReader;
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#hasNext()
     */
    @Override
    public boolean hasNext() {
        return marcReader.hasNext();
    }

    /* (non-Javadoc)
     * @see java.util.Iterator#next()
     */
    @Override
    public Record next() {
        return marcReader.next();
    }

    /**
     * Vereinfachte Factory-Methode, wenn bekannt ist, dass eine Datei
     * im Marc-Format vorliegt. Diese kann auch
     * als GZIP verpackt sein.
     *
     *
     * @param filename  nicht null
     * @return  Iterator
     * @throws IOException  z.B., wenn Datei nicht existiert
     */
    public static MarcIterator getFromMarcFile(final String filename)
        throws IOException {
        Objects.requireNonNull(filename);
        final InputStream input = FileUtils.getMatchingInputStream(filename);
        final MarcReader marcReader = new MarcStreamReader(input);
        final MarcIterator iterator = new MarcIterator(marcReader);

        return iterator;
    }

    /**
     * Vereinfachte Factory-Methode, wenn bekannt ist, dass eine Datei
     * im XML-Format vorliegt. Diese kann auch Diese kann auch als
     * GZIP verpackt sein.
     *
     *
     * @param filename  nicht null
     * @return  Iterator
     * @throws IOException  z.B., wenn Datei nicht existiert
     */
    public static MarcIterator getFromXML(final String filename)
        throws IOException {
        Objects.requireNonNull(filename);
        final InputStream input = FileUtils.getMatchingInputStream(filename);
        final MarcReader marcReader = new MarcXmlReader(input);
        final MarcIterator iterator = new MarcIterator(marcReader);

        return iterator;
    }

    /**
     * Vereinfachte Factory-Methode. Wenn "xml" im Dateinamen vorkommt
     * wird vermutet, dass eine Datei
     * im XML-Format vorliegt. Ansonsten wird Marc-Format
     * vermutet.Die Datei kann auch als GZIP verpackt sein.
     *
     *
     * @param filename  nicht null
     * @return  Iterator
     * @throws IOException  z.B., wenn Datei nicht existiert
     */
    public static MarcIterator getFromFile(final String filename)
        throws IOException {
        Objects.requireNonNull(filename);
        if (filename.contains("xml")) {
            return getFromXML(filename);
        } else {
            return getFromMarcFile(filename);
        }
    }

    /**
     * @param args
     * @throws IOException
     */
    public static void main1(final String[] args) throws IOException {
        final MarcIterator it = getFromFile(
            "Z:/cbs/zen/vollabzug/aktuell/MARC21/DNB-Title-utf8-marc21-1.gz");
        final Stream<Record> stream = it.stream();
        stream.limit(5).forEach(System.out::println);
        stream.close();

    }

    /**
     * @param args
     * @throws IOException
     */
    public static void main(final String[] args) throws IOException {
        MarcIterator it = getFromFile("Z:/cbs_sync/ddc/ddc.xml");
        final Stream<Record> stream = it.stream();
        stream.limit(20L).map(DDCMarcUtils::getFullClassificationNumber)
            .forEach(System.out::println);
        it.close();

        it = getFromFile(
            "D:/eclipse-neon/workspace/zmarc/src/org/marc4j/samples/resources/brkrtest.mrc");
        it.stream().map(Record::getControlNumber).forEach(System.out::println);

        it = getFromFile(
            "D:/eclipse-neon/workspace/zmarc/src/org/marc4j/samples/resources/brkrtest.mrc");
        for (final Record record : it) {
            System.out.println(record);
        }

    }

    /* (non-Javadoc)
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<Record> iterator() {
        return this;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#finalize()
     */
    @Override
    public void finalize() throws Throwable {
        stopXMLThread();
        super.finalize();
    }

    /**
     *
     */
    private void stopXMLThread() {
        final ThreadGroup thgr = Thread.currentThread().getThreadGroup();
        final int count = thgr.activeCount();
        final Thread[] list = new Thread[count];
        thgr.enumerate(list);
        for (final Thread thread : list) {
            if (thread instanceof MarcXmlParserThread) {
                thread.stop();
            }
        }
    }

    /* (non-Javadoc)
     * @see java.io.Closeable#close()
     */
    @Override
    public void close() throws IOException {
        stopXMLThread();

    }

}
