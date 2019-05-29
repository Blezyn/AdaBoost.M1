import org.jetbrains.annotations.NotNull;

import javax.print.DocFlavor;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents a list of Record's' that all have a non-null target value.
 * @param <T> The type of the output variable (class/category) of the Record's'.
 */
public class Table<T> {

    /**
     * Represents a tuple of 2 objects.
     */
    private static class Tuple<A, R> {

        /**
         * The 1st object of this Tuple.
         */
        private A a;

        /**
         * The 2nd object of this Tuple.
         */
        private R b;

        /**
         * Creates a Tuple given its 2 objects.
         * @param a The 1st object of this Tuple.
         * @param b The 2nd object of this Tuple.
         */
        private Tuple(A a, R b) {
            this.a = a;
            this.b = b;
        }

    }//end local class Tuple

    /**
     * A List with all the records of this Table.
     */
    private List<Record<T>> records;

    /**
     * A Map with keys the title of a Feature and values a Set with the
     * SemiRange's' of that Feature column. It is used on the Features with
     * continuous data.
     */
    private Map<String, Set<SemiRange<?>>> ranges;

    /**
     * Calculates the weighted entropy of the target variable of a Stream of
     * Records.
     * @return The weighted entropy of the target variable of a Stream of
     * Records.
     */
    private static <T> double weightedEntropy(
            @NotNull Stream<Record<T>> records) {
        //The entropy of this Table, for the target values
        double entropy = 0.0;
        //A Map with all the unique values of the target variable of the
        //Record's' of records Stream as its keys and the sum of their weights
        //as its values
        Map<T, Double> weights = records.collect(Collectors.toMap(
                Record::getTarget, Record::getWeight, Double::sum,
                LinkedHashMap::new));
        //The sum of the weights of all the Record's' in weights Map
        final double SUM = weights.entrySet()
                                  .stream()
                                  .mapToDouble(Map.Entry::getValue)
                                  .sum();

        //Calculates the entropy
        for (Map.Entry<?, Double> e : weights.entrySet()) {
            //Calculates the relative frequency of the current value
            double relFreq = e.getValue() / SUM;
            //Subtracts the term of the current value from the entropy
            entropy -= relFreq * (Math.log(relFreq) / Math.log(2.0));
        }//end for

        return entropy;
    }

    /**
     * Calculates the entropy of the target variable of a Stream of Records.
     * @return The entropy of the target variable of a Stream of Records.
     */
    private static <T> double entropy(@NotNull Stream<Record<T>> records) {
        //The entropy of this Table, for the target values
        double entropy = 0.0;
        //A Map with all the unique values of the target variable of the
        //Record's' of records Stream as its keys and their frequency as its
        //values
        Map<?, Long> valFreq = records.map(Record::getTarget)
                                      .collect(Collectors.groupingBy(
                                              Function.identity(),
                                              Collectors.counting()));

        //Calculates the entropy
        for (Map.Entry<?, Long> e : valFreq.entrySet()) {
            //Calculates the relative frequency of the current value
            double relFreq = (double) e.getValue() / valFreq.size();
            //Subtracts the term of the current value from the entropy
            entropy -= relFreq * (Math.log(relFreq) / Math.log(2.0));
        }//end for

        return entropy;
    }

    /**
     * Calculates the information gain of splitting this Table by the given
     * Feature, based on the weights of the Record's'.
     * @param ftrTitle The title of the Feature to calculate its information
     * gain on split.
     * @return The information gain of splitting this Table by the given
     * Feature, based on the weights of the Record's'.
     */
    private static <T> double wInfoGain(@NotNull Collection<Record<T>> records,
            @NotNull Set<SemiRange<?>> ranges, @NotNull String ftrTitle) {
        //The entropy of this Table, for the target value of its Records.
        double entropy = Table.weightedEntropy(records.parallelStream());

        //Checks if the given Feature contains continuous values
        if (ranges != null) {
            //A Map with the the entropy of this Table, on its target variable, if
            //split by each value in ftrValues
            Map<Object, Double> entropies = new LinkedHashMap<>(ranges.size());
            //The information gain of splitting
            double infoGain = entropy;
            //Calculates the information gain
            for (SemiRange sr : ranges) {
                entropies.put(sr, Table.weightedEntropy(records
                        .parallelStream()
                        .filter(r -> sr.contains(r.getFeatures()
                                .get(ftrTitle)
                                .getData()))));
            }//end for

            //Calculates the information gain
            for (Map.Entry<Object, Double> e : entropies.entrySet()) {
                //Subtracts the term of e.getKey() from infoGain
                infoGain -= ftrValWFreq(records, ranges, ftrTitle,
                        e.getKey()) * e.getValue();
            }//end for

            return infoGain;
        }//end if

        throw new RuntimeException();
    }

    /**
     * Calculates the weighted frequency of a given value of a Feature, in the
     * Record's' of this Table.
     * @param ftrTitle The title of the Feature the given value belongs to.
     * @param value A value of the given Feature.
     * @return The weighted frequency of a given value of a Feature, in the
     * Record's' of this Table. If there are no Record's' with the given value,
     * or the value does not belong in the value set of the given Feature,
     * returns 0.
     */
    private static <T> double ftrValWFreq(
            @NotNull Collection<Record<T>> records,
            @NotNull Set<SemiRange<?>> ranges,
            @NotNull String ftrTitle,
            @NotNull Object value) {
        //The sum of the weights of all the Record's' of this Table
        final double WEIGHT_SUM = records
                .parallelStream()
                .mapToDouble(Record::getWeight)
                .sum();

        //Checks if the given Feature contains continuous values
        if (ranges != null) {
            //A variable to store a reference to the SemiRange containing value
            SemiRange range = (SemiRange) value;
            //Iterates over ranges to find the one that contains value
            /*for (SemiRange r : ranges) {
                //Checks if r contains value
                if (r.contains((Comparable) value)) {
                    //Store a reference to r
                    range = r;
                    //No need to continue iteration
                    break;
                }//end if
            }//end for

            //To make the compiler happy, for lambda use
            final SemiRange RANGE = range;*/
            return records
                    .parallelStream()
                    .filter(r -> range.contains(r.getFeatures()
                            .get(ftrTitle)
                            .getData()))
                    .mapToDouble(Record::getWeight)
                    .sum() / WEIGHT_SUM;
        }//end if

        return records
                .parallelStream()
                .filter(r -> r.getFeatures()
                        .get(ftrTitle)
                        .getData()
                        .equals(value))
                .mapToDouble(Record::getWeight)
                .sum() / WEIGHT_SUM;
    }

    /**
     * Finds the optimal pivot from a List of continuous data.
     * @param records A Collection with all the Record's' of a table.
     * @param ftrTitle The title of the continuous Feature to pick its optimal
     * pivot.
     * @param <T> The type of the target variable in a Record.
     * @return The optimal pivot element.
     */
    private static <T> Comparable<?> optPivot(
            @NotNull Collection<Record<T>> records, @NotNull String ftrTitle) {
        List<Comparable> values = records.parallelStream()
                                            .map(r -> r.getFeatures()
                                                       .get(ftrTitle)
                                                       .getData())
                                            .collect(Collectors.toList());
        List<Tuple<Comparable<?>, Double>> data = new ArrayList<>(
                values.size());
        for (Comparable<?> v : values) {
            SemiRange<?> lowRange = new SemiRange(v, false,
                    true);
            SemiRange<?> highRange = new SemiRange(v, true,
                    false);
            data.add(new Tuple<>(v, Table.wInfoGain(records, new HashSet<>(
                    List.of(lowRange, highRange)), ftrTitle)));
        }//end for

        Comparable<?> c = data.parallelStream()
                .max(Comparator.comparing(t -> t.b))
                .get()
                .a;
        values.sort(Comparator.naturalOrder());
        //System.out.printf("i: %d / %d%n", values.indexOf(c), values.size());

        return data.parallelStream()
                   .max(Comparator.comparing(t -> t.b))
                   .get()
                   .a;
    }

    /**
     * Creates a Table, given its Record's'.
     * @param records The Record's' of this Table. All the Record's' must be
     * consistent with each other, in the sense, that all of them must contain
     * the same semantic Features. This constructor will not verify that, for
     * performance reasons. If the given Record's' have that inconsistency, this
     * instance will have undefined behavior and state.
     * @throws IllegalArgumentException If the records.isEmpty() == true.
     * @throws IllegalArgumentException If there is at least 1 Record in records
     * Collection with a null target value.
     */
    public Table(@NotNull Collection<Record<T>> records) {
        //Validates that there is at least 1 element in records Collection
        if (records.isEmpty()) {
            throw new IllegalArgumentException("Argument Collection records " +
                    "must contain at least 1 element.");
        }//end if

        //Validates that all the Record's' in records Collection have non-null
        //target value
        for (Record<T> r : records) {
            //Checks if Record r has no target value
            if (r.getTarget() == null) {
                throw new IllegalArgumentException("All the Record's' in " +
                        "Collection records must contain a non-null target " +
                        "value.");
            }//end if
        }//end for

        //System.out.println("Records size: " + records.size());

        //A Map with keys the title of a Feature and values a Set with the
        //SemiRange's' of that Feature column.
        Map<String, Set<SemiRange<?>>> ranges = new HashMap<>();
        //Gets a Record from Records Collection
        Record<T> templateRecord = records.stream()
                                          .findAny()
                                          .get();
        //A Map with the Feature's' of the Record's' as values, and their titles
        //as keys
        Map<String, Feature<?>> features = templateRecord.getFeatures();
        //Loops through all the Feature's' of the Records, to find the ones with
        //continuous data type
        for (Map.Entry<String, Feature<?>> e : features.entrySet()) {
            //Checks if the current Feature doesn't have continuous data type
            if (!e.getValue().getType().equals(Feature.Type.CONTINUOUS)) {
                continue;
            }//end if

            //Gets the pivot of data List
            Comparable<?> pivot = Table.optPivot(records, e.getKey());
            //Partitions the continuous values of the current Feature into 2
            //SemiRange's'. The generics at instantiation are dropped, as the
            //compiler cannot infer them, because of the capture<?>
            SemiRange<?> lowRange = new SemiRange(pivot, false,
                    true);
            SemiRange<?> highRange = new SemiRange(pivot, true,
                    false);
            //Puts them in ranges Map, keyed by the title of the current Feature
            ranges.put(e.getKey(), new HashSet(Arrays.asList(lowRange,
                    highRange)));
        }//end for

        //Map<Record<T>, Double> weights

        this.records = records.parallelStream()
                              .map(Record::shallowCopy)
                              .collect(Collectors.toList());
        this.ranges = ranges;
    }

    /**
     * Gets the Record's' of this Table.
     * @return An unmodifiable List with the Record's' of this Table.
     */
    public @NotNull
    List<Record<T>> getRecords() {
        return Collections.unmodifiableList(this.records);
    }

    /**
     * Gets the Range's' of the continuous Feature's' of this Table, if any.
     * @return An unmodifiable Map with the Range's' of the continuous
     * Feature's' of this Table, if any.
     */
    public @NotNull
    Map<String, Set<SemiRange<?>>> getRanges() {
        return Collections.unmodifiableMap(this.ranges);
    }

    /**
     * Finds the optimal Feature to split this Table.
     * @return The title of the optimal Feature to split this Table.
     */
    public @NotNull
    String optimalFeature() {
        //A template Record to obtain the Feature's' of this Table
        Record<T> templateRecord = this.records.get(0);
        return templateRecord.getFeatures()
                             .entrySet()
                             .parallelStream()
                             .map(e -> new Tuple<>(e.getKey(),
                                     this.weightedInfoGain(e.getKey())))
                             .max(Comparator.comparing(x -> x.b))
                             .get()
                             .a;
    }

    /**
     * Gets a Set with all the values of a Feature, in the Record's' of this
     * Table.
     * @param ftrTitle The title of the Feature to retrieve its values.
     * @return A Set with all the values of a given Feature, in the Record's' of
     * this Table.
     */
    public @NotNull
    Set<?> ftrValues(@NotNull String ftrTitle) {
        //Gets the Range's' of the given Feature
        Set<SemiRange<?>> ranges = this.ranges.get(ftrTitle);
        //Checks if the given Feature contains continuous values
        if (ranges != null) {
            return ranges;
        }//end if

        return this.records
                   .parallelStream()
                   .map(r -> r.getFeatures().get(ftrTitle).getData())
                   .collect(Collectors.toSet());
    }

    /**
     * Splits this Table on a given Feature.
     * @param ftrTitle The title of the Feature to split this Table, based on.
     * @param minCapacity The minimum number of Record's' a Table must have,
     * after a split.
     * @return A Map with keys the splitting values of the Table, and values the
     * new split Tables.
     */
    public @NotNull
    Map<Object, Table<T>> split(@NotNull String ftrTitle,
                                final int minCapacity) {
        //Checks if this Table does not have the minimum number of Record's'
        //required for the split to take place
        if (this.records.size() < minCapacity) {
            return new LinkedHashMap<>();
        }//end if

        //Gets the Range's' of the given Feature
        Set<SemiRange<?>> ranges = this.ranges.get(ftrTitle);
        //Checks if the given Feature contains continuous values
        if (ranges != null) {
            //A Map with keys the splitting values of the Table, and values the
            //new split Tables.
            Map<Object, Table<T>> splitTables =
                    new LinkedHashMap<>(2);
            //Populates splitTables Map
            for (SemiRange sr : ranges) {
                //The split List with the Record's' with values in sr SemiRange
                List<Record<T>> records = this.records
                                              .parallelStream()
                                              .filter(r -> sr.contains(
                                                    r.getFeatures()
                                                     .get(ftrTitle)
                                                     .getData()))
                                              .collect(Collectors.toList());
                //Checks if records contains at least the minimum number of
                //Record's'
                if (records.size() < minCapacity) {
                    continue;
                }//end if

                //Puts in splitTables Map the new Table
                splitTables.put(sr, new Table<>(records));
            }//end for

            return splitTables;
        }//end if

        //A Map with keys the splitting values of the Table, and values the
        //new split Tables.
        Map<Object, Table<T>> splitTables = new LinkedHashMap<>();
        //Populates splitTables Map
        for (Object v : this.ftrValues(ftrTitle)) {
            //The split List with the Record's' with values v
            List<Record<T>> records = this.records
                                          .parallelStream()
                                          .filter(r -> v.equals(
                                                  r.getFeatures()
                                                   .get(ftrTitle)
                                                   .getData()))
                                          .collect(Collectors.toList());
            //Checks if records contains at least the minimum number of
            //Record's'
            if (records.size() < minCapacity) {
                continue;
            }//end if

            //Puts in splitTables Map the new Table
            splitTables.put(v, new Table<>(records));
        }//end for

        return splitTables;
    }

    /**
     * Calculates the weighted frequency of a given value of a Feature, in the
     * Record's' of this Table.
     * @param ftrTitle The title of the Feature the given value belongs to.
     * @param value A value of the given Feature.
     * @return The weighted frequency of a given value of a Feature, in the
     * Record's' of this Table. If there are no Record's' with the given value,
     * or the value does not belong in the value set of the given Feature,
     * returns 0.
     */
    private double ftrValueWFreq(@NotNull String ftrTitle,
            @NotNull Object value) {
        //The sum of the weights of all the Record's' of this Table
        final double WEIGHT_SUM = this.records
                                      .parallelStream()
                                      .mapToDouble(Record::getWeight)
                                      .sum();

        //Gets the Range's' of the given Feature
        Set<SemiRange<?>> ranges = this.ranges.get(ftrTitle);
        //Checks if the given Feature contains continuous values
        if (ranges != null) {
            //A variable to store a reference to the SemiRange containing value
            SemiRange range = (SemiRange) value;
            //Iterates over ranges to find the one that contains value
            /*for (SemiRange r : ranges) {
                //Checks if r contains value
                if (r.contains((Comparable) value)) {
                    //Store a reference to r
                    range = r;
                    //No need to continue iteration
                    break;
                }//end if
            }//end for

            //To make the compiler happy, for lambda use
            final SemiRange RANGE = range;*/
            return this.records
                       .parallelStream()
                       .filter(r -> range.contains(r.getFeatures()
                                                    .get(ftrTitle)
                                                    .getData()))
                       .mapToDouble(Record::getWeight)
                       .sum() / WEIGHT_SUM;
        }//end if

        return this.records
                .parallelStream()
                .filter(r -> r.getFeatures()
                              .get(ftrTitle)
                              .getData()
                              .equals(value))
                .mapToDouble(Record::getWeight)
                .sum() / WEIGHT_SUM;
    }

    /**
     * Calculates the frequency of a given value of a Feature, in the Record's'
     * of this Table.
     * @param ftrTitle The title of the Feature the given value belongs to.
     * @param value A value of the given Feature.
     * @return The frequency of a given value of a Feature, in the Record's' of
     * this Table. If there are no Record's' with the given value, or the value
     * does not belong in the value set of the given Feature, returns 0.
     */
    private long ftrValueFreq(@NotNull String ftrTitle,
            @NotNull Object value) {
        //Gets the Range's' of the given Feature
        Set<SemiRange<?>> ranges = this.ranges.get(ftrTitle);
        //Checks if the given Feature contains continuous values
        if (ranges != null) {
            //A variable to store a reference to the SemiRange containing value
            SemiRange range = null;
            //Iterates over ranges to find the one that contains value
            for (SemiRange r : ranges) {
                //Checks if r contains value
                if (r.contains((Comparable) value)) {
                    //Store a reference to r
                    range = r;
                    //No need to continue iteration
                    break;
                }//end if
            }//end for

            return range.isBoundInc() ? (this.records.size() + 2) / 2 :
                    this.records.size() - (this.records.size() + 2) / 2;
        }//end if

        return this.records
                   .parallelStream()
                   .map(r -> r.getFeatures().get(ftrTitle).getData())
                   .filter(value::equals)
                   .count();
    }

    /**
     * Calculates the information gain of splitting this Table by the given
     * Feature, based on the weights of the Record's'.
     * @param ftrTitle The title of the Feature to calculate its information
     * gain on split.
     * @return The information gain of splitting this Table by the given
     * Feature, based on the weights of the Record's'.
     */
    private double weightedInfoGain(@NotNull String ftrTitle) {
        //The entropy of this Table, for the target value of its Records.
        double entropy = Table.weightedEntropy(this.records.parallelStream());

        //Gets the Range's' of the given Feature
        Set<SemiRange<?>> ranges = this.ranges.get(ftrTitle);
        //Checks if the given Feature contains continuous values
        if (ranges != null) {
            //A Map with the the entropy of this Table, on its target variable, if
            //split by each value in ftrValues
            Map<Object, Double> entropies = new LinkedHashMap<>(ranges.size());
            //The information gain of splitting
            double infoGain = entropy;
            //Calculates the information gain
            for (SemiRange sr : ranges) {
                entropies.put(sr, Table.weightedEntropy(this.records
                                 .parallelStream()
                                 .filter(r -> sr.contains(r.getFeatures()
                                                           .get(ftrTitle)
                                                           .getData()))));
            }//end for

            //Calculates the information gain
            for (Map.Entry<Object, Double> e : entropies.entrySet()) {
                //Subtracts the term of e.getKey() from infoGain
                infoGain -= this.ftrValueWFreq(ftrTitle, e.getKey()) *
                        e.getValue();
            }//end for

            return infoGain;
        }//end if

        //A Set with all the values of the given Feature, in the Record's' of
        //this Table.
        Set<?> ftrValues = this.ftrValues(ftrTitle);
        //A Map with the the entropy of this Table, on its target variable, if
        //split by each value in ftrValues
        Map<Object, Double> entropies = new LinkedHashMap<>(ftrValues.size());
        //Populates entropies Map
        for (Object v : ftrValues) {
            entropies.put(v, Table.weightedEntropy(this.records
                     .parallelStream()
                     .filter(r -> r.getFeatures()
                                  .get(ftrTitle)
                                  .getData()
                                  .equals(v))));
        }//end for

        //The information gain of splitting
        double infoGain = entropy;
        //Calculates the information gain
        for (Map.Entry<Object, Double> e : entropies.entrySet()) {
            //Subtracts the term of e.getKey() from infoGain
            infoGain -= this.ftrValueWFreq(ftrTitle, e.getKey()) * e.getValue();
        }//end for

        return infoGain;
    }

    /**
     * Calculates the information gain of splitting this Table by the given
     * Feature.
     * @param ftrTitle The title of the Feature to calculate its information
     * gain on split.
     * @return The information gain of splitting this Table by the given
     * Feature.
     */
    private double infoGain(@NotNull String ftrTitle) {
        //The entropy of this Table, for the target value of its Records.
        double entropy = Table.entropy(this.records.parallelStream());

        //Gets the Range's' of the given Feature
        Set<SemiRange<?>> ranges = this.ranges.get(ftrTitle);
        //Checks if the given Feature contains continuous values
        if (ranges != null) {
            //The information gain of splitting
            double infoGain = entropy;
            //Calculates the information gain
            for (SemiRange sr : ranges) {
                //Calculates the relative frequency of the items that are
                //contained in SemiRange sr
                double relFreq = sr.isBoundInc() ? (this.records.size() + 2) /
                        2.0 : this.records.size() - (this.records.size() + 2) /
                        2.0;
                //Subtracts the term of SemiRange sr from infoGain
                infoGain -= relFreq * Table.entropy(
                        this.records
                            .parallelStream()
                            .filter(r -> sr.contains(r.getFeatures()
                                                      .get(ftrTitle)
                                                      .getData())));
            }//end for

            return infoGain;
        }//end if

        //A Set with all the values of the given Feature, in the Record's' of
        //this Table.
        Set<?> ftrValues = this.ftrValues(ftrTitle);
        //A Map with the the entropy of this Table, on its target variable, if
        //split by each value in ftrValues
        Map<Object, Double> entropies = new LinkedHashMap<>(ftrValues.size());
        //Populates entropies Map
        for (Object v : ftrValues) {
            entropies.put(v, Table.entropy(this.records
                                               .parallelStream()
                                               .filter(r -> r.getFeatures()
                                                             .get(ftrTitle)
                                                             .getData()
                                                             .equals(v))));
        }//end for

        //The information gain of splitting
        double infoGain = entropy;
        //Calculates the information gain
        for (Map.Entry<Object, Double> e : entropies.entrySet()) {
            //Subtracts the term of e.getKey() from infoGain
            infoGain -= ((double) this.ftrValueFreq(ftrTitle, e.getKey()) /
                    this.records.size()) * e.getValue();
        }//end for

        return infoGain;
    }

}//end class Table