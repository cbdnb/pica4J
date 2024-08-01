/**
 *
 */
package de.dnb.basics.applicationComponents;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author baumann
 *
 */
public class Streams {

    /**
    *
    * @param <T>       Typ
    * @param iterator  nicht null
    * @return          einen sequentiellen Stream
    */
    public static <T>
        Stream<T>
        getStreamFromIterator(final Iterator<T> iterator) {
        Objects.requireNonNull(iterator);
        final Spliterator<T> spliterator = Spliterators
            .spliteratorUnknownSize(iterator, Spliterator.IMMUTABLE);
        final Stream<T> stream = StreamSupport.stream(spliterator, false);
        return stream;
    }

    /**
     *
     * @param <T>       Typ
     * @param iterable  nicht null
     * @return          einen sequentiellen Stream
     */
    public static <T>
        Stream<T>
        getStreamFromIterable(final Iterable<T> iterable) {
        Objects.requireNonNull(iterable);
        final Stream<T> stream =
            StreamSupport.stream(iterable.spliterator(), false);
        return stream;
    }

    /**
     *
     * Creates a lazily concatenated stream whose elements are all the
     * elements of the streams.  The resulting stream is ordered if all
     * of the input streams are ordered, and parallel if either of the input
     * streams is parallel.  When the resulting stream is closed, the close
     * handlers for all input streams are invoked.
     *
     * @param <T>       Typ
     * @param streams   auch null
     * @return          konkatenierten Stream
     */
    @SafeVarargs
    public static <T> Stream<T> concat(final Stream<? extends T>... streams) {
        if (streams == null)
            return Stream.empty();

        Stream<T> retS = Stream.empty();
        for (final Stream<? extends T> stream : streams) {
            retS = Stream.concat(retS, stream);
        }
        return retS;

    }

    /**
    *
    * Creates a lazily concatenated stream whose elements are all the
    * elements of the streams.  The resulting stream is ordered if all
    * of the input streams are ordered, and parallel if either of the input
    * streams is parallel.  When the resulting stream is closed, the close
    * handlers for all input streams are invoked.
    *
    * @param <T>       Typ
    * @param streams   auch null
    * @return          konkatenierten Stream
    */
    public static <T> Stream<T> concat(final Collection<Stream<T>> streams) {
        if (streams == null)
            return Stream.empty();
        Stream<T> retS = Stream.empty();
        for (final Stream<T> stream : streams) {
            retS = Stream.concat(retS, stream);
        }
        return retS;
    }

    /**
     * @param args
     */
    public static void main(final String[] args) {
        final Stream<? extends Number> is1 =
            IntStream.range(0, 3).boxed().sequential();
        final Stream<? extends Number> is2 =
            IntStream.range(100, 103).boxed().sequential();
        final Stream<? extends Number> is3 =
            IntStream.range(200, 203).boxed().sequential();

        final List<Stream<? extends Number>> list =
            Arrays.asList(is1, is2, is3);
        //        list = null;

        final Stream<? extends Number> is = concat(is1, is2, is3);
        is.forEach(System.out::println);
    }

}
