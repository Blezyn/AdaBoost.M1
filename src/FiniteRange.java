import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Represents a finite range of continuous values, in order to reduce continuous
 * values into discrete categories.
 * @param <T> The type of the bounds of this FiniteRange.
 */
public class FiniteRange<T extends Comparable<T>> extends Range<T> {

    /**
     * The lower bound of this FiniteRange.
     */
    private T low;

    /**
     * The upper bound of this FiniteRange.
     */
    private T high;

    /**
     * Indicates if the lower bound of this FiniteRange is included. True if it
     * is included, otherwise false.
     */
    private boolean isLowInc;

    /**
     * Indicates if the upper bound of this FiniteRange is included. True if it
     * is included, otherwise false.
     */
    private boolean isHighInc;

    /**
     * Creates a FiniteRange, given its lower and upper bounds, and if they are
     * included or not.
     * @param low The lower bound of this FiniteRange.
     * @param high The upper bound of this FiniteRange.
     * @param isLowInc Indicates if the lower bound of this FiniteRange is
     * included.
     * True if it is included, otherwise false.
     * @param isHighInc Indicates if the upper bound of this FiniteRange is
     * included.
     * True if it is included, otherwise false.
     * @throws IllegalArgumentException If low > high.
     */
    public FiniteRange(@NotNull T low, @NotNull T high, boolean isLowInc,
            boolean isHighInc) {
        //Validates that low <= high
        if (low.compareTo(high) > 0) {
            throw new IllegalArgumentException("Arguments low and high must " +
                    "satisfy the inequality: low <= high.");
        }//end if

        this.low = low;
        this.high = high;
        this.isLowInc = isLowInc;
        this.isHighInc = isHighInc;
    }

    /**
     * Indicates if this FiniteRange contains the given value.
     * @param value A value to check if it is contained in this FiniteRange.
     * @return True if the given value is contained in this FiniteRange,
     * otherwise false.
     */
    @Override
    public boolean contains(@NotNull T value) {
        //Checks if the given value is strictly inside this FiniteRange
        if (this.low.compareTo(value) < 0 && this.high.compareTo(value) > 0) {
            return true;
        }//end if

        return this.isLowInc && this.low.compareTo(value) == 0 || this.isHighInc
                && this.high.compareTo(value) == 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FiniteRange<?> finiteRange = (FiniteRange<?>) o;
        return isLowInc == finiteRange.isLowInc && isHighInc ==
                finiteRange.isHighInc && low.equals(finiteRange.low) &&
                high.equals(finiteRange.high);
    }

    @Override
    public int hashCode() {
        return Objects.hash(low, high, isLowInc, isHighInc);
    }

}//end class FiniteRange