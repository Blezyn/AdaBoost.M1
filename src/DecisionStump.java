import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Represents a DecisionTree of depth 1.
 * @param <Y> The type of the output variable (class/category) of the Record's',
 * this DecisionStump handles.
 */
public class DecisionStump<Y> extends DecisionTree<Y> {

    /**
     * Creates a DecisionStump on a given Collection of data. After this
     * constructor ends, the DecisionStump is trained, and ready to receive
     * classification queries.
     * @param records A Collection of Record's' to be classified by this
     * DecisionStump. They must contain their target values.
     * @throws IllegalArgumentException If records.isEmpty() == true.
     */
    public DecisionStump(@NotNull Collection<Record<Y>> records) {
        super(records, 1, 1);
    }

}//end class DecisionStump