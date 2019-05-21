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
     * Indicates if this Classifier, classifies correctly a given Record.
     * @param record A Record to check if this Classifier classifies it
     * correctly. It must contain a target value.
     * @return True if this Classifier correctly classifies the given Record,
     * otherwise false.
     */
    default boolean isCorrect(@NotNull Record<Y> record) {

        return record.getTarget() == predict(record);
    }

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
        //Validates that all the given Records have a target value
        for (Record<Y> r : records) {
            //Checks if Record r does not have a target value
            if (!r.hasTarget()) {
                throw new IllegalArgumentException("Argument Collection " +
                        "records must contain only Record's' that have their " +
                        "target value.");
            }//end if
        }//end for

        int sum = records.size();
        int success=0;
        for (Record<Y> e:records ){
            if (this.isCorrect(e)){success ++;}
        }
        return  (double) success / sum;
    }

    /**
     * Predicts/Classifies the output value of a given Record.
     * @param record A Record to predict its output value.
     * @return The predicted output value of the given Record.
     */
    @NotNull Y predict(@NotNull Record<Y> record);

}//end interface Classifier