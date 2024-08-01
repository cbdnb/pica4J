/**
 * 
 */
package de.dnb.basics.applicationComponents;

/**
 * Provides mutable access to a value.
 * 
 * @author baumann
 * 
 * 
 * 
 */
public class Mutable<T> {

  T value = null;

  /**
   * Gets the value of this mutable.
   * 
   * @return the stored value, may be null
   */
  public T getValue() {
    return value;
  }

  /**
   * Constructs a new Mutable.
   * 
   * @param value  value to set, may be null
   */
  public Mutable(T value) {    
    this.value = value;
  }

  /**
   * Sets the value of this mutable.
   * 
   * @param newValue  the value to store, may be null
   */
  public void setValue(T newValue) {
    value = newValue;
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    Mutable<String> mutable = new Mutable<>("");
    mutable.setValue("qwe");
    CharSequence s = mutable.getValue();

  }

}
