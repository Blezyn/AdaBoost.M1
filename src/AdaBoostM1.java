import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Represents a model created by the AdaBoost.M1 algorithm.
 * @param <Y> The type of the output variable (class/category) of the Record's',
 * this AdaBoostM1 handles.
 */
public class AdaBoostM1<Y> implements Classifier<Y> {

    /**
     * Creates an AdaBoostM1 trained model, given its training Record's', a
     * ClassifierGenerator to generate its weak Classifier's' and the maximum
     * number of weak Classifier's' it will use for prediction.
     * @param records A Collection with all the training data, to construct this
     * AdaBoostM1 model. The type of all the same Feature's' must be the same,
     * otherwise the generated Classifier will have undefined results.
     * @param classifierGen A generator of Classifier's'.
     * @param maxModels The maximum number of weak Classifier's' this AdaBoostM1
     * model, will use for prediction.
     */
    public AdaBoostM1(@NotNull Collection<Record<Y>> records,
            @NotNull ClassifierGenerator<Y> classifierGen, int maxModels) {
        throw new UnsupportedOperationException();
    }

    /**
     * Predicts/Classifies the output value of a given Record.
     * @param record A Record to predict its output value.
     * @return The predicted output value of the given Record.
     */
    @Override
    public @NotNull Y predict(@NotNull Record<Y> record) {
        throw new UnsupportedOperationException();
    }

}//end class AdaBoostM1