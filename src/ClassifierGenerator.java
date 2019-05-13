import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Represents a generator of Classifier's'.
 * @param <Y> The type of the output variable (class/category) of the Record's'
 * of the Classifier's', this ClassifierGenerator generates.
 */
@FunctionalInterface
public interface ClassifierGenerator<Y> {

    /**
     * Generates a Classifier, given the training Record's'.
     * @param records A Collection with all the training data, to construct a
     * Classifier. The type of all the same Feature's' must be the same,
     * otherwise the generated Classifier will have undefined results.
     * @return A trained Classifier, from the given Record's'.
     */
    @NotNull Classifier<Y> generate(@NotNull Collection<Record<Y>> records);

}//end interface ClassifierGenerator