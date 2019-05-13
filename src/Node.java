import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Represents a Node in a DecisionTree.
 * @param <T> The type of the output variable (class/category) of the Record's'
 * of this Node.
 */
public class Node<T> {

    /**
     * The title of the Feature this Node branches based on. Contains null if
     * this Node is a leaf Node.
     */
    private String featureTitle;

    /**
     * A Table with all the Record's' of this Node.
     */
    private Table<T> table;

    /**
     * A Map with all the child nodes of this Node. The key is an Object, to use
     * its .equals() method polymorphically, and the value is the child Node to
     * branch to.
     */
    private Map<Object, Node<T>> childNodes = new HashMap<>();

    /**
     * Creates a Node, given its Table.
     * @param table The Table with all the Record's' of this Node.
     */
    public Node(@NotNull Table<T> table) {
        this.table = table;
    }

    /**
     * Maps a given key to a child Node.
     * @param key The key of the child Node.
     * @param child The child Node of this Node.
     */
    @Deprecated
    public void addChild(@NotNull Object key, @NotNull Node<T> child) {
        //Puts child in the childNodes Maps with key as its key
        this.childNodes.put(key, child);
    }

    /**
     * Gets a child Node of this Node, given a value.
     * @param value The value of a Record, of the branching Feature of this
     * Node, in order to branch to the proper child Node.
     * @return The child Node to branch to, or null if there is no mapping for
     * to a child Node for the given value.
     */
    public @Nullable
    Node<T> branch(@NotNull Comparable value) {
        //Gets the Range's' of the branching Feature of this Node
        Set<SemiRange<?>> ranges = this.table.getRanges().get(
                this.featureTitle);
        //Checks if the branching Feature contains continuous values
        if (ranges != null) {
            //Finds the SemiRange that contains the given value
            for (SemiRange sr : ranges) {
                //Checks if sr contains value
                if (sr.contains(value)) {
                    return this.childNodes.get(sr);
                }//end if
            }//end for

            return null;
        }//end if

        return this.childNodes.get(value);
    }

    /**
     * Indicates if Node is a leaf node, i.e. contains 0 child Nodes.
     * @return True if this Node is a leaf node, otherwise false.
     */
    public boolean isLeaf() {
        return this.childNodes.isEmpty();
    }

    /**
     * Gets the title of the Feature this Node branches based on.
     * @return The title of the Feature this Node branches based on, or null if
     * this Node is a leaf node.
     */
    public @Nullable
    String getFtrTitle() {
        return this.featureTitle;
    }

    /**
     * Finds the dominant (most frequent) target value of the Table of this
     * Node. Breaks ties arbitrary.
     * @return The dominant (most frequent) target value of the Table of this
     * Node or an arbitrary one, if there are more than 1 with the same
     * frequency.
     */
    public @NotNull T dominantTarget() {
        return this.table.getRecords()
                         .parallelStream()
                         .map(Record::getTarget)
                         .collect(Collectors.groupingBy(Function.identity(),
                                 Collectors.counting()))
                         .entrySet()
                         .parallelStream()
                         .max(Comparator.comparing(Map.Entry::getValue))
                         .get()
                         .getKey();
    }

    /**
     * Gets a Collection with all the child Nodes of this node.
     * @return An unmodifiable Collection with all the child Nodes of this node.
     */
    public @NotNull
    Collection<Node<T>> getChildNodes() {
        return Collections.unmodifiableCollection(this.childNodes.values());
    }

    /**
     * Gets the number of Record's' the Table of this Node has.
     * @return How many Record's' the Table of this Node has.
     */
    public int tableSize() {
        return this.table.getRecords().size();
    }

    /**
     * Splits this Node, based on a Feature that will give the maximum
     * information gain. After that, automatically splits its child Node's' too.
     * If this Node is already split, this method does nothing.
     * @param minCapacity The minimum number of Record's' a Node must have,
     * after a split.
     */
    public void split(final int minCapacity) {
        //Checks if this Node is already split
        if (!this.childNodes.isEmpty()) {
            return;
        }//end if

        //Checks if this Node does not have the minimum number of Record's'
        //required for the split to take place
        if (this.tableSize() < minCapacity) {
            return;
        }//end if

        //Gets the title of the optimal Feature, for splitting this Node
        String optFtrTitle = this.table.optimalFeature();
        //The split Tables of this Node on the optimal Feature
        Map<Object, Table<T>> splitTables = this.table.split(optFtrTitle,
                minCapacity);

        //Checks if splitTables contains less than 2 elements
        if (splitTables.size() < 2) {
            return;
        }//end if

        //Populates this.childNodes Map
        for (Map.Entry<Object, Table<T>> e : splitTables.entrySet()) {
            //Adds a new child Node in this Node
            this.childNodes.put(e.getKey(), new Node<>(e.getValue()));
        }//end for

        //Sets the title of the Feature this Node examines
        this.featureTitle = optFtrTitle;

        //Splits the child Node's' on their optimal Feature
        for (Map.Entry<Object, Node<T>> e : this.childNodes.entrySet()) {
            //Splits the current child Node on its optimal Feature
            e.getValue().split(minCapacity);
        }//end for
    }

    /**
     * Splits this Node, based on a Feature that will give the maximum
     * information gain. After that, automatically splits its child Node's' too.
     * If this Node is already split or the maximum depth has been reached, i.e.
     * maxDepth < 1, this method does nothing.
     * @param minCapacity The minimum number of Record's' a Node must have,
     * after a split.
     * @param maxDepth The maximum depth the underlying tree of the splits will
     * have.
     */
    public void split(final int minCapacity, int maxDepth) {
        throw new UnsupportedOperationException();
    }

}//end class Node