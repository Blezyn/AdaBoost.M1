import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * A mechanism to pick random items, based on their weight. The higher their
 * weight, the more likely an item to be picked.
 * @param <T> The type of the items this Picker selects.
 */
public class Picker<T> {

    /**
     * Finds the index of the floor value of a given target value from a non
     * decreasing ordered array.
     * @param arr An ordered non decreasing array.
     * @param target The target value to finds its floor inside the given array.
     * By floor, means the largest value inside the array that is <= target.
     * @return The index of the floor value of the given target. By floor, means
     * the largest value inside the array that is <= target. If no such value
     * exists, returns -1.
     */
    private static int floor(double[] arr, double target) {
        //The low index of the search region
        int l = 0;
        //The high index of the search region
        int h = arr.length - 1;
        //The index of the center of the search region, rounded down
        int m;
        //Performs binary search to find the index of the floor value of target
        while (l <= h) {
            //Calculates the center of the search region, rounded down
            m = (l + h) / 2;
            //Finds which border to change, in order to limit the search range
            if (arr[m] < target) {
                l = m + 1;
            } else if (arr[m] > target) {
                h = m - 1;
            } else {
                return m;
            }//end if
        }//end while

        return h;
    }

    /**
     * A List with all the Map.Entry's', that have a non 0.0 value.
     */
    private List<Map.Entry<T, Double>> map;

    /**
     * The cumulative relative frequency of the weights of the items. It is used
     * to pick a random item based on its weight (the larger the weight, the
     * more likely the item will be selected). Its first element is 0.0 and its
     * last 1.0. It is used for performance reasons.
     */
    private double[] addRelFreq;

    /**
     * Selects 1 item from the given items, based on weights (the larger the
     * weight, the more likely an item to be selected). Runs in O(n) time if and
     * only if isReady() returns false, prior to the invocation of this method,
     * otherwise runs in O(log2(n)) time, where n is the number of items.
     * @param entries A List with Map.Entry instances, that contain an item
     * and its weight.
     * @param rand A Random instance to generate random values.
     * @return The selected item.
     */
    public T pick(List<Map.Entry<T, Double>> entries, Random rand) {
        //Checks if the cumulative relative frequency is not calculated yet
        if (!this.isReady()) {
            //Calculates the cumulative relative frequency of the values of
            //the keys
            this.refresh(entries);
        }//end if

        //Checks if all the given keys have 0.0 value
        if (this.map.isEmpty()) {
            return entries.get(rand.nextInt(entries.size())).getKey();
        }//end if

        //Selects a random index from this.map List, based on the values of
        //the keys
        int rndIndex = Picker.floor(this.addRelFreq, rand.nextDouble());
        return this.map.get(rndIndex).getKey();
    }

    /**
     * Discards all the data needed to pick a random item, based on its
     * weight.
     */
    public void discard() {
        //Removes all the cumulative relative frequencies
        this.addRelFreq = null;
        //Removes the index mappings of the keys
        this.map = null;
    }

    /**
     * Indicates if this Picker is ready to receive queries, to pick random
     * items.
     * @return True if this Picker is ready to receive queries, to pick
     * random items, otherwise false.
     */
    public boolean isReady() {
        return this.addRelFreq != null;
    }

    /**
     * Calculates and caches the cumulative relative frequency of the values
     * of the keys. Runs in O(n) time, where n is the number of elements in
     * entries List.
     * @param entries A List with Map.Entry instances, that contain a key
     * and its value.
     */
    private void refresh(List<Map.Entry<T, Double>> entries) {
        //Finds all the keys that have a non 0.0 value
        this.map = entries.parallelStream()
                          .filter(e -> e.getValue() != 0.0)
                          .collect(Collectors.toList());
        //Allocates space for the cumulative relative frequency array
        this.addRelFreq = new double[map.size() + 1];

        //Checks if all the given keys have 0.0 value
        if (this.map.isEmpty()) {
            return;
        }//end if

        //Calculates the sum of the values of all the keys that have a non
        //0.0 value
        double sum = this.map
                         .parallelStream()
                         .mapToDouble(Map.Entry::getValue)
                         .sum();
        //Populates this.addRelFreq array, by calculating the relative
        //frequency, based on the values of every key that has non 0.0
        //value. Starts from index 1 in this.addRelFreq, as index 0 has
        //value 0.0
        for (int i = 0; i < this.map.size(); ++i) {
            this.addRelFreq[i + 1] = this.map.get(i).getValue() / sum;
        }//end for

        //Calculates the cumulative relative frequency of the values of the
        //keys, based on their relative frequency
        for (int i = 1; i < addRelFreq.length; ++i) {
            //Calculates the cumulative relative frequency of key at index
            //i, and stores it in this.addRelFreq array
            addRelFreq[i] = addRelFreq[i] + addRelFreq[i - 1];
        }//end for

        //Sets the last element of this.addRelFreq array to 1.0, as this is
        //the value it should have, but is possible that it won't, due to
        //precision loss from floating point numerical operations
        this.addRelFreq[this.addRelFreq.length - 1] = 1.0;
    }

}//end class Picker