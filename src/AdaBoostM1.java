import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Represents a model created by the AdaBoost.M1 algorithm.
 * @param <Y> The type of the output variable (class/category) of the Record's',
 * this AdaBoostM1 handles.
 */
public class AdaBoostM1<Y> implements Classifier<Y> {

    /**
     * A List with all the Classifier's' of this AdaBoostM1, along with their
     * weight for the final prediction stage.
     */
    private @NotNull List<Map.Entry<Classifier<Y>, Double>> classifiers;

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
     * @throws IllegalArgumentException If maxModels < 1.
     * @throws IllegalStateException If this AdaBoostM1 model, could not build
     * at least 1 weak classifier.
     */
    public AdaBoostM1(@NotNull Collection<Record<Y>> records,
            @NotNull ClassifierGenerator<Y> classifierGen, int maxModels) {
        //Validates that maxModels >= 1
        if (maxModels < 1) {
            throw new IllegalArgumentException("Argument maxModels must be " +
                    ">= 1.");
        }//end if

        //A Consumer that takes a Collection of Record's' and shares their
        //weights evenly to form a distribution. All the Record's' will have the
        //same weight and the sum of the weights of all the Record's' will be
        //1.0
        Consumer<Collection<Record<Y>>> shareWeights = c -> c.parallelStream()
                .forEach(r -> r.setWeight(1.0 / c.size()));
        //A Consumer that takes a Collection of Record's' and normalizes their
        //weights to form a distribution, while maintaining their relative
        //weight information. The sum of the weights of all the Record's' will
        //be 1.0
        Consumer<Collection<Record<Y>>> normalize = c -> {
            //Calculates the sum of the weights of all the Record's' in
            //Collection c
            final double SUM = c.parallelStream()
                                .mapToDouble(Record::getWeight)
                                .sum();
            //Normalizes all the weights
            c.parallelStream()
             .forEach(r -> r.setWeight(r.getWeight() / SUM));
        };

        //Shares evenly the weights of the records Collection
        shareWeights.accept(records);
        //A List with Classifier's' for this AdaBoostM1, along with their weight
        //for the final prediction stage.
        List<Map.Entry<Classifier<Y>, Double>> classifiers =
                new ArrayList<>(maxModels);
        //A Random instance to generate random values
        Random rand = new Random();
        //How many times has tried to create a new Classifier, when an old one
        //had > 0.5 error
        int tries = 50;

        //Populates classifiers List with Classifier's'
        for (int modelNum = 1; modelNum <= maxModels && tries > 0; ++modelNum) {
            //Gets a Classifier from Record's' records
            Classifier<Y> classifier = classifierGen.generate(records);
            //A Set with all the Record's' that classifier correctly predicted
            //their target value
            Set<Record<Y>> correctRecords = records.stream()
                    .filter(classifier::isCorrect)
                    .collect(Collectors.toSet());
            //System.out.println("RECORDS: " + records.size());
            //System.out.println("CORRECT RECORDS: " + correctRecords.size());
            //The weighted error of Classifier classifier, at its prediction of
            //Record's' records
            double error = records.stream()
                                  .filter(r -> !correctRecords.contains(r))
                                  .mapToDouble(Record::getWeight)
                                  .sum();
            //The weight^-1 of classifier in its final prediction stage
            final double INV_WEIGHT = error / (1.0 - error);
            //Checks if error is greater than the threshold 0.5
            if (error > 0.5) {
                //continue; //break;
                --tries;
            } else {
                //Adds classifier in classifiers List, along with prediction weight
                classifiers.add(new AbstractMap.SimpleEntry<>(classifier, 1.0 /
                        INV_WEIGHT));
            }//end if

            //System.out.println("Error: " + error);

            //Checks if there are no more models to build
            if (modelNum == maxModels) {
                break;
            }//end if

            //Updates the weights of the Record's' in records Collection
            records.parallelStream()
                   .filter(correctRecords::contains)
                   .forEach(r -> r.setWeight(r.getWeight() * INV_WEIGHT));

            /*records.forEach(r -> {
                        System.out.println(correctRecords.contains(r));
                        System.out.println(r.getWeight());
                    });
            System.out.println();
            System.out.println();*/

            //Normalizes the Record's'
            normalize.accept(records);
            //System.out.println("D: " + records.stream().distinct().count());
        }//end for

        //Validates that there is at least 1 weak classifier in classifiers List
        if (classifiers.isEmpty()) {
            throw new IllegalStateException("Could not build weak " +
                    "classifiers.");
        }//end if

        System.out.println("SIZE: " + classifiers.size());
        System.out.println("Tries: " + tries);
        //System.out.println("----------------------------------------------");
        //System.out.println(classifiers.size());
        this.classifiers = classifiers;
    }

    /**
     * Predicts/Classifies the output value of a given Record.
     * @param record A Record to predict its output value.
     * @return The predicted output value of the given Record.
     */
    @Override
    public @NotNull Y predict(@NotNull Record<Y> record) {
        //A Function: R^2 -> R, that represents a logarithm of any base
        BiFunction<Double, Double, Double> logb = (x, b) -> Math.log(x) /
                Math.log(b);
        return this.classifiers
                   .parallelStream()
                   .map(e -> new AbstractMap.SimpleEntry<>(e,
                           e.getKey().predict(record)))
                   .collect(Collectors.toList())
                   .stream()
                   .collect(Collectors.toMap(AbstractMap.SimpleEntry::getValue,
                           e -> {
                               List<Map.Entry<Classifier<Y>, Double>> l =
                                       new ArrayList<>();
                               l.add(e.getKey());
                               return l;
                           }, (v1, v2) -> {
                               v1.addAll(v2);
                               return v1;
                           }, LinkedHashMap::new))
                   .entrySet()
                   .parallelStream()
                   .map(e -> new AbstractMap.SimpleEntry<>(e.getKey(),
                           e.getValue()
                            .parallelStream()
                            .mapToDouble(Map.Entry::getValue)
                            .map(w -> logb.apply(w, 2.0))
                            .sum()))
                //.peek(System.out::println)
                   .max(Comparator.comparingDouble(Map.Entry::getValue))
                   .get()
                   .getKey();
    }

}//end class AdaBoostM1