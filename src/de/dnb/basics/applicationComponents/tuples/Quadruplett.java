package de.dnb.basics.applicationComponents.tuples;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Quadruplett<S, U, V, W> implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 8357034255332594192L;

	public final S first;

	public final U second;

	public final V third;

	public final W forth;

	public static <U, V, W, X> Quadruplett<U, V, W, X> getNullQuad() {
		return new Quadruplett<>(null, null, null, null);
	}

	public Quadruplett(final S first, final U second, final V third, final W forth) {
		super();
		this.first = first;
		this.second = second;
		this.third = third;
		this.forth = forth;
	}

	@Override
	public final String toString() {
		return "Quadruplett [first=" + first + ", second=" + second + ", third=" + third + ", forth=" + forth + "]";
	}

	public List<String> asList() {
		return Arrays.asList(Objects.toString(first), Objects.toString(second), Objects.toString(third),
				Objects.toString(forth));
	}

}
