package de.dnb.basics.filtering;

import de.dnb.basics.cloneable.CopyObjectUtils;

/**
 * Hilfsklasse für Wertebereiche mit Minimum und Maximum.
 * <br>
 * Typen müssen Subtyp von Number sein und Comparable erfüllen
 *
 * @author Michael Inden
 *
 * Copyright 2011 by Michael Inden
 */
public final class ValueRange<T extends Number & Comparable<T>> {
  private final T minValue;

  private final T maxValue;

  public ValueRange(final T minValue, final T maxValue) {
    if (minValue.compareTo(maxValue) >= 0)
      throw new IllegalArgumentException(
        "minValue " + minValue + " must be <= maxValue " + maxValue);

    this.minValue = CopyObjectUtils.copyObject(minValue);
    this.maxValue = CopyObjectUtils.copyObject(maxValue);
  }

  public T getMinValue() {
    return CopyObjectUtils.copyObject(minValue);
  }

  public T getMaxValue() {
    return CopyObjectUtils.copyObject(maxValue);
  }

  public boolean contains(final T value) {
    return (value.compareTo(minValue) >= 0 && value.compareTo(maxValue) <= 0);
  }

  @Override
  public String toString() {
    return "ValueRange [" + minValue + " -- " + maxValue + "]";
  }

  public String createErrorMessage(final T value) {
    if (contains(value))
      return "";

    return "value " + value + " not in range [" + minValue + " -- " + maxValue + "]";
  }
}
