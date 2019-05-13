import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Represents a model that predicts the output variable of a given Record.
 * @param <Y> The type of the output variable (class/category) of the Record's',
 * this Classifier handles.
 */
@FunctionalInterface
public interface Classifier<Y> {

    /**
     * Calculates the percentage of the given Record's' that were successfully
     * classified by this Classifier.
     * @param records A Collection of Record's' to be classified by this
     * Classifier. They must contain their target values.
     * @return A value in range [0.0, 1.0] describing the percentage of the
     * given Record's' that were successfully classified, by this Classifier. A
     * value of 0.0 means total failure to correctly classify even a single
     * Record, while a value of 1.0 means that all the Record's' were correctly
     * classified.
     * @throws IllegalArgumentException If there is at least 1 Record in records
     * Collection, that does not have a target value.
     */
    default double successRate(@NotNull Collection<Record<Y>> records) {
        throw new UnsupportedOperationException();
    }

    /**
     * Predicts/Classifies the output value of a given Record.
     * @param record A Record to predict its output value.
     * @return The predicted output value of the given Record.
     */
    @NotNull Y predict(@NotNull Record<Y> record);

}//end interface Classifier