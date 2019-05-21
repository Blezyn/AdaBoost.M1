import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Represents a decision tree on a given List of data, for classification.
 * @param <T> The type of the output variable (class/category) of the Record's'
 * of this DecisionTree.
 */
public class DecisionTree<T> implements Classifier<T> {

    /**
     * The root Node of this DecisionTree.
     */
    private Node<T> root;

    /**
     * Creates a decision tree on a given Collection of data. After this
     * constructor ends, the tree is trained, and ready to receive
     * classification queries.
     * @param records A Collection with all the training data, to construct this
     * DecisionTree. All the given Record's' must have a non-null target value.
     * All the given data in case of String's' or Char's', are case sensitive,
     * i.e. "path", "PATH", "pAtH" are all considered different values. The type
     * of all the same Feature's' must be the same, otherwise this DecisionTree
     * will have undefined results.
     * @param minNodeCapacity The minimum number of Record's' a Node of this
     * DecisionTree can have.
     * @throws IllegalArgumentException If records.isEmpty() == true.
     * @throws IllegalArgumentException If minNodeCapacity <= 0.
     * @throws IllegalArgumentException If records.size() < minNodeCapacity.
     */
    public DecisionTree(@NotNull Collection<Record<T>> records,
            int minNodeCapacity) {
        //Validates that records Collection contains at least 1 element
        if (records.isEmpty()) {
            throw new IllegalArgumentException("Argument records Collection " +
                    "must contain at least 1 element.");
        }//end if

        //Validates that minNodeCapacity > 0
        if (minNodeCapacity <= 0) {
            throw new IllegalArgumentException("Argument minNodeCapacity " +
                    "must be > 0.");
        }//end if

        //Validates that records.size() >= minNodeCapacity
        if (records.size() < minNodeCapacity) {
            throw new IllegalArgumentException("Argument records Collection " +
                    "must have a size of at least minNodeCapacity.");
        }//end if

        //Creates the root Node of this DecisionTree
        Node<T> root = new Node<>(new Table<>(records));
        //Trains this DecisionTree
        root.split(minNodeCapacity);

        this.root = root;
    }

    /**
     * Creates a decision tree on a given Collection of data. After this
     * constructor ends, the tree is trained, and ready to receive
     * classification queries.
     * @param records A Collection with all the training data, to construct this
     * DecisionTree. All the given Record's' must have a non-null target value.
     * All the given data in case of String's' or Char's', are case sensitive,
     * i.e. "path", "PATH", "pAtH" are all considered different values. The type
     * of all the same Feature's' must be the same, otherwise this DecisionTree
     * will have undefined results.
     * @param minNodeCapacity The minimum number of Record's' a Node of this
     * DecisionTree can have.
     * @param maxDepth The maximum depth this DecisionTree will have.
     * @throws IllegalArgumentException If records.isEmpty() == true.
     * @throws IllegalArgumentException If minNodeCapacity <= 0.
     * @throws IllegalArgumentException If records.size() < minNodeCapacity.
     * @throws IllegalArgumentException if maxDepth < 1.
     */
    public DecisionTree(@NotNull Collection<Record<T>> records,
            int minNodeCapacity, int maxDepth) {

        //Validates that records Collection contains at least 1 element
        if (records.isEmpty()) {
            throw new IllegalArgumentException("Argument records Collection " +
                    "must contain at least 1 element.");
        }//end if

        //Validates that minNodeCapacity > 0
        if (minNodeCapacity <= 0) {
            throw new IllegalArgumentException("Argument minNodeCapacity " +
                    "must be <= the minimum number of Records .");
        }//end if

        //Validates that records.size() > minNodeCapacity
        if (records.size() < minNodeCapacity) {
            throw new IllegalArgumentException("Argument minNodeCapacity " +
                    "must be > 0.");
        }//end if

        //Validates that  maxDepth >= 1
        if (maxDepth < 1) {
            throw new IllegalArgumentException("Argument maxDepth " +
                    "must be >= 1.");
        }//end if


        Node<T> root = new Node<>(new Table<>(records));
        //Trains this DecisionTree
        root.split(minNodeCapacity);

        this.root = root;
    }

    /**
     * Predicts/Classifies the output value of a given record.
     * @param record A record to predict its output value.
     * @return The predicted output value of the given Record.
     */
    @Override
    public @NotNull T predict(@NotNull Record<T> record) {
        //The target Node, to make the prediction
        Node<T> node = this.root;
        //Follows a path of Nodes, until it reaches a Node that has no mapping
        //for a given value of the examined Feature
        while (node.getFtrTitle() != null) {
            //Gets the value of the examined Feature in node, of record
            Comparable value = record.getFeatures().get(
                    node.getFtrTitle()).getData();
            //The next Node to branch to, based on the value of the given Record
            //on the Feature examined by node
            node = node.branch(value);
        }//end while

        return node.dominantTarget();
    }

}//end class DecisionTree