/**
 *
 */
package de.dnb.basics.applicationComponents;

/**
 * @author baumann
 *
 */
public enum TernaryLogic {

        TRUE, MAYBE, FALSE;

    public static TernaryLogic create(final boolean booleanValue) {
        if (booleanValue)
            return TRUE;
        else
            return FALSE;
    }

    /**
     *
     * @return  true, wenn TRUE; false für alle anderen Werte
     */
    public boolean toBool() {
        if (this == TRUE)
            return true;
        else
            return false;
    }

    public TernaryLogic and(final TernaryLogic other) {
        if (this == TRUE) {
            return other;
        } else if (this == MAYBE) {
            return (other == FALSE) ? FALSE : MAYBE;
        } else {
            return FALSE;
        }
    }

    public TernaryLogic or(final TernaryLogic other) {
        if (this == TRUE) {
            return TRUE;
        } else if (this == MAYBE) {
            return (other == TRUE) ? TRUE : MAYBE;
        } else {
            return other;
        }
    }

    public TernaryLogic implies(final TernaryLogic other) {
        if (this == TRUE) {
            return other;
        } else if (this == MAYBE) {
            return (other == TRUE) ? TRUE : MAYBE;
        } else {
            return TRUE;
        }
    }

    public TernaryLogic not() {
        if (this == TRUE) {
            return FALSE;
        } else if (this == MAYBE) {
            return MAYBE;
        } else {
            return TRUE;
        }
    }

    public TernaryLogic equals(final TernaryLogic other) {
        if (this == TRUE) {
            return other;
        } else if (this == MAYBE) {
            return MAYBE;
        } else {
            return other.not();
        }
    }

}
