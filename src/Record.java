import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a row in the table with all the data.
 * @param <T> The type of the output variable (class/category) of this Record.
 */
public class Record<T> {

    /**
     * A Map with the features/attributes of this Record, keyed by their title.
     */
    private Map<String, Feature<?>> features;

    /**
     * The class/category of this Record. It may or may not be present. Non null
     * values indicate that the target is present, otherwise null values
     * indicate that it is not.
     */
    private T target;

    /**
     * A non negative finite value, describing the weight of this Record.
     */
    private double weight;

    /**
     * Creates a Record, given its Feature's' and target value. Its weight will
     * be 1.0.
     * @param features The Feature's' of this Record.
     * @param target The target value of this Record. Can be null, to indicate
     * that this Record has no target value. It is useful for creating Record's'
     * that need to be classified.
     * @throws IllegalArgumentException If features.isEmpty() == true.
     */
    public Record(@NotNull Collection<Feature<?>> features,
            @Nullable T target) {
        this(features, target, 1.0);
    }

    /**
     * Creates a Record, given its Feature's', target value and weight.
     * @param features The Feature's' of this Record.
     * @param target The target value of this Record. Can be null, to indicate
     * that this Record has no target value. It is useful for creating Record's'
     * that need to be classified.
     * @param weight A non negative finite value, describing the weight of this
     * Record.
     * @throws IllegalArgumentException If features.isEmpty() == true.
     * @throws IllegalArgumentException If weight is not finite.
     * @throws IllegalArgumentException If weight < 0.0.
     */
    public Record(@NotNull Collection<Feature<?>> features,
            @Nullable T target, double weight) {
        //Validates that features Collections contains at least 1 element
        if (features.isEmpty()) {
            throw new IllegalArgumentException("Argument Collection features " +
                    "must contain at least 1 element.");
        }//end if

        //Validates that weight is finite
        if (!Double.isFinite(weight)) {
            throw new IllegalArgumentException("Argument weight must be " +
                    "finite.");
        }//end if

        //Validates that weight >= 0.0
        if (weight < 0.0) {
            throw new IllegalArgumentException("Argument weight can't be " +
                    "negative.");
        }//end if

        //Creates a Map with the features/attributes of this Record, keyed by
        //their title.
        Map<String, Feature<?>> featureMap = new HashMap<>(features.size());
        //Populates featureMap
        for (Feature<?> f : features) {
            //Puts Feature f in featureMap
            featureMap.put(f.getTitle(), f);
        }//end for

        this.features = featureMap;
        this.target = target;
        this.weight = weight;
    }

    /**
     * Gets an unmodifiable Map, with the Feature's' of this Record, keyed by
     * their title.
     * @return An unmodifiable Map, with the Feature's' of this Record, keyed by
     * their title.
     */
    public @NotNull
    Map<String, Feature<?>> getFeatures() {
        return Collections.unmodifiableMap(this.features);
    }

    /**
     * Gets the class/category of this Record. Non null values indicate that the
     * target is present, otherwise null values indicate that it is not.
     * @return The class/category of this Record or null if the target value is
     * not present.
     */
    public @Nullable T getTarget() {
        return this.target;
    }

    /**
     * Indicates if this Record has a target value.
     * @return True if this Record has a target value, otherwise false.
     */
    public boolean hasTarget() {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets the weight of this Record.
     * @return A non negative finite value, describing the weight of this
     * Record.
     */
    public double getWeight() {
        return this.weight;
    }

    /**
     * Sets the weight of this Record.
     * @param weight A non negative finite value, describing the new weight of
     * this Record.
     * @throws IllegalArgumentException If weight is not finite.
     * @throws IllegalArgumentException If weight < 0.0.
     */
    public void setWeight(double weight) {
        throw new UnsupportedOperationException();
    }

}//end class Record