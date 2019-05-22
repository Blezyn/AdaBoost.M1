import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class AdaIrisTest {

    public static void main(String[] args) {
        //A List with all the lines of the .csv file as String's'
        List<String> lines = null;
        //Tries to read the lines of the file in lines List
        try {
            lines = Files.readAllLines(Paths.get(args[0]));
        } catch (IOException e) {
            e.printStackTrace();
        }//end try

        //Validates that lines List contains at least 2 lines, its header, and
        //at least 1 record
        if (lines.size() < 2) {
            throw new IllegalArgumentException("Argument args[0] must " +
                    "contain a file with at least 2 lines.");
        }//end if

        //A List with all the String tokens of the header of the records.
        List<String> header = AdaIrisTest.tokenize(lines.get(0));
        //Hides the first element of header List, which is the number of records
        //the .csv file has, as it is no longer needed
        header = header.subList(1, header.size());
        //A List with only the records in tokenized form, without the header
        List<List<String>> strRecords = AdaIrisTest.strRecords(lines.subList(1,
                lines.size()));

        //A List with all the training records
        List<List<String>> trainStrRecords = AdaIrisTest.trainRecords(strRecords);
        //A Collection of Record's', form trainStrRecords
        Collection<Record<String>> trainRecords =
                AdaIrisTest.constructRecords(header, trainStrRecords);
        //Creates a DecisionTree with the training records
        AdaBoostM1<String> dt = new AdaBoostM1<>(trainRecords, DecisionStump::new, 10);

        //A List with all the evaluation records
        List<List<String>> evalStrRecords =
                AdaIrisTest.evaluationRecords(strRecords, trainStrRecords);
        //A Collection of Record's', form evalStrRecords
        Collection<Record<String>> evalRecords =
                AdaIrisTest.constructRecords(header, evalStrRecords);
        //Prints the success prediction rate of DecisionTree dt
        System.out.println(AdaIrisTest.successRate(dt, evalRecords));

        /* If a second argument is given, the output file with all the targets
         * is extracted.
         */
        if (args.length >= 2)
            saveFile(args[1], evalRecords);
    }

    /**
     * Tokenizes a String, by splitting it at ',' and returns a List with all
     * the tokens.
     * @param str A String to tokenize at ','.
     * @return A List with all the tokens of the splitting.
     */
    private static @NotNull List<String> tokenize(@NotNull String str) {
        String[] tokens;
        List<String> listOfTokens = new ArrayList<String> ();

        tokens = str.split(",");
        for (int i = 0; i < tokens.length; i++)
            listOfTokens.add(tokens[i]);

        return listOfTokens;
    }

    /**
     * Creates a Feature.Generator, for a given tab in the data. The tab must be
     * in the form: featureTitle:0 for discrete Feature's' or featureTitle:1 for
     * continuous Feature's'.
     * @param tab A String that contains the title and the type of the Feature
     * the Feature.Generator will be made for. Its form is: featureTitle:0 for
     * discrete Feature's' or featureTitle:1 for continuous Feature's'.
     * @param <T> The type of the Feature.Generator.
     * @return A Feature.Generator for Feature the given tab represents.
     */
    private static <T extends Comparable<T>> Feature.@NotNull Generator<T>
    getGenerator(@NotNull String tab) {
        int type;
        String[] tokens;

        tokens = tab.split(":");
        type = Integer.parseInt(tokens[1]);

        if (type == 0)
            return new Feature.Generator<> (tokens[0], Feature.Type.DISCRETE);
        else if (type == 1)
            return new Feature.Generator<> (tokens[0], Feature.Type.CONTINUOUS);
        else
            throw new IllegalArgumentException("The type of the given tab " +
                    "must be either 0 for discrete or 1 for continuous.");
    }

    /**
     * Creates a List with all the lines of the file, in a tokenized form, i.e.
     * a List of String tokens.
     * @param lines A List with all the lines of the file in String's'.
     * @return A List with all the lines of the file, in a tokenized form, i.e.
     * a List of String tokens.
     */
    private static @NotNull List<List<String>> strRecords(
            @NotNull List<String> lines) {
        List<List<String>> list = new ArrayList<List<String>>();

        for (String l : lines) {
            List<String> tokens = tokenize(l);
            list.add(tokens);
        }

        return list;
    }

    /**
     * Creates a List that contains all the values of a column in String form.
     * The output List contains all the records, but only their values at the
     * column/Feature described by the given index, that applies in the input
     * List.
     * @param strRecords A List with all the records, in a tokenized form, i.e.
     * a List of String tokens. This List must not contain the header of the
     * .csv file, it must contain only the records, all with tokenized String
     * values. The List at the last index must contain the target values.
     * @param index The index of the column, in strRecords List.
     * @return A List that contains all the values of the column described by
     * index, in String form.
     */
    private static @NotNull List<String> columnRecords(@NotNull List<List<String>> strRecords, int index) {
        List<String> ListOfString = (List<String>) new ArrayList<String>(strRecords.size());
        String col;
        for (int i = 0; i < strRecords.size(); i++) {
            col = strRecords.get(i).get(index);
            ListOfString.add(col);
        }
        return ListOfString;
    }

    /**
     * Creates a new List of Double, out of a List of String, given that the
     * String's' actually represent Double's' and can be parsed.
     * @param list A List of String values, that represent Double's'.
     * @return A List with the values of the input List, in Double form.
     */
    private static @NotNull List<Double> toDouble(@NotNull List<String> list) {
        List<Double> ListOfDoubleArr = (List<Double>) new ArrayList<Double>(list.size());
        for (Iterator<String> it = (Iterator<String>) list.iterator(); it.hasNext();) {
            ListOfDoubleArr.add(Double.parseDouble(String.valueOf(it.next())));
        }//end for loop which iterates over List<String>
        return ListOfDoubleArr;
    }

    /**
     * Arbitrary selects a percentage from a List of records, to form a List in
     * the same form, that contains the training records. The selection must not
     * be entirely random, in order to prevent extinction of any target value.
     * To prevent that case, this method arbitrary selects a percentage of every
     * target value, for the training set.
     * @param records A List with all the records, in a tokenized form, i.e. a
     * List of String tokens. This List must not contain the header of the .csv
     * file, it must contain only the records, all with tokenized String values.
     * The List at the last index must contain the target values.
     * @return The input List, filtered to contain only the training records.
     */
    private static @NotNull List<List<String>> trainRecords(
            @NotNull List<List<String>> records) {
        //The percentage of the training records
        final double PERCENTAGE = 0.75;
        //The number of training records
        int size = (int) Math.round(PERCENTAGE * records.size());
        //A List with the training records
        List<List<String>> trainRecords = new ArrayList<>(size);

        //A Map with the target values as keys and the a List with all the
        //records they belong to
        Map<String, List<List<String>>> targets = new HashMap<>();
        //Iterates records List
        for (List<String> r : records) {
            //The target value of record r
            String key = r.get(r.size() - 1);
            //Gets the List with all the records, of the target of record r
            List<List<String>> targetRecords = targets.computeIfAbsent(key,
                    k -> new ArrayList<>());
            //Adds record r in targetRecords List
            targetRecords.add(r);
        }//end for

        //Populates trainRecords List
        for (Map.Entry<String, List<List<String>>> e : targets.entrySet()) {
            //Shuffles the records of the current target value
            Collections.shuffle(e.getValue());
            //Adds a percentage of the records of the current target value, in
            //trainRecords List
            trainRecords.addAll(e.getValue().subList(0, (int)
                    Math.round(PERCENTAGE * e.getValue().size())));
        }//end for

        return trainRecords;
    }

    /**
     * Takes 2 List's' of records, and returns their difference.
     * @param records A List with all the records, in a tokenized form, i.e. a
     * List of String tokens. This List must not contain the header of the .csv
     * file, it must contain only the records, all with tokenized String values.
     * The List at the last index must contain the target values.
     * @param trainRecords A List that is a subset of records List, and contains
     * only the training records.
     * @return A List containing all the records that exist in records List, but
     * don't exist in trainRecords List, i.e. the evaluation records.
     */
    private static @NotNull List<List<String>> evaluationRecords(
            @NotNull List<List<String>> records,
            @NotNull List<List<String>> trainRecords) {
        //A Set from records List with all the evaluation records
        Set<List<String>> evalRecordsSet = new HashSet<>(records);
        //A Set from trainRecords List with all the training records
        Set<List<String>> trainRecordsSet = new HashSet<>(trainRecords);
        //Removes all the trainRecords from evalRecordsSet
        evalRecordsSet.removeAll(trainRecordsSet);

        return new ArrayList<>(evalRecordsSet);
    }

    /**
     * Calculates the percentage of the given Record's' that were successfully
     * classified by the given DecisionTree.
     * @param dt A DecisionTree to classify the given Record's'.
     * @param records A Collection of Record's' to be classified by the given
     * DecisionTree. They must contain their target values.
     * @return A value in range [0.0, 1.0] describing the percentage of the
     * given Record's' that were successfully classified, by the given
     * DecisionTree. A value of 0.0 means total failure to correctly classify
     * even a single Record, while a value of 1.0 means that all the Record's'
     * were correctly classified.
     */
    private static double successRate(@NotNull AdaBoostM1<String> dt,@NotNull Collection<Record<String>> records) {
        int sum = records.size();
        int success=0;
        String t;
        for (Record<String> e:records ){
            t= dt.predict(e);
            String target= e.getTarget();
            if (target.equals(t)){success ++;}
        }
        return  (double) success / sum;
    }

    /**
     * Creates Record's' for the DecisionTree, for training.
     * @param header A List with all the String tokens of the header of the
     * records.
     * @param strRecords A List with all the records, in a tokenized form, i.e.
     * a List of String tokens. This List must not contain the header of the
     * .csv file, it must contain only the records, all with tokenized String
     * values. The List at the last index must contain the target values.
     * @return A Collection of Record's' with target variable of String,
     * constructed from the given records in strRecords.
     */
    private static @NotNull Collection<Record<String>> constructRecords(
            @NotNull List<String> header,
            @NotNull List<List<String>> strRecords) {
        //A Feature.Generator for the 1st Feature
        Feature.Generator<Double> gen1 = AdaIrisTest.getGenerator(header.get(0));
        //A Feature.Generator for the 2nd Feature
        Feature.Generator<Double> gen2 = AdaIrisTest.getGenerator(header.get(1));
        //A Feature.Generator for the 3rd Feature
        Feature.Generator<Double> gen3 = AdaIrisTest.getGenerator(header.get(2));
        //A Feature.Generator for the 4th Feature
        Feature.Generator<Double> gen4 = AdaIrisTest.getGenerator(header.get(3));

        //A List with all the data of the 1st column, in String format
        List<String> column1 = AdaIrisTest.columnRecords(strRecords, 0);
        //Converts column1 in Double form
        List<Double> inData1 = AdaIrisTest.toDouble(column1);

        //A List with all the data of the 2nd column, in String format
        List<String> column2 = AdaIrisTest.columnRecords(strRecords, 1);
        //Converts column2 in Double form
        List<Double> inData2 = AdaIrisTest.toDouble(column2);

        //A List with all the data of the 3rd column, in String format
        List<String> column3 = AdaIrisTest.columnRecords(strRecords, 2);
        //Converts column3 in Double form
        List<Double> inData3 = AdaIrisTest.toDouble(column3);

        //A List with all the data of the 4th column, in String format
        List<String> column4 = AdaIrisTest.columnRecords(strRecords, 3);
        //Converts column1 in Double form
        List<Double> inData4 = AdaIrisTest.toDouble(column4);

        //A List with all the data of the output column
        List<String> outData = AdaIrisTest.columnRecords(strRecords, header.size()
                - 1);

        return AdaIrisTest.constructRecords(Arrays.asList(inData1, inData2,
                inData3, inData4), Arrays.asList(gen1, gen2, gen3, gen4),
                outData);
    }

    /**
     * Creates a Collection of Record's', given all the data of the columns,
     * Feature.Generator's', and a List with all the output values, of the
     * records.
     * @param data A List of columns, with each column containing the values of
     * all the records, for that column.
     * @param generators A List with all Feature.Generator's' to create the
     * Feature's' of the Record's'.
     * @param output A List with all the output values of the records.
     * @param <Τ> The type of the output variable.
     * @return A Collection with all the Record's', to train a DecisionTree.
     */
    private static <Τ> @NotNull Collection<Record<Τ>> constructRecords(
            @NotNull List<List<? extends Comparable<?>>> data,
            @NotNull List<Feature.Generator> generators,
            @NotNull List<Τ> output) {
        final int RECORDS_NUM = data.get(0).size();
        Collection<Record<Τ>> records = new ArrayList<>(RECORDS_NUM);
        for (int i = 0; i < RECORDS_NUM; ++i) {
            List<Feature<?>> features = new ArrayList<>(data.size());
            for (int j = 0; j < data.size(); ++j) {
                features.add(generators.get(j).generate(data.get(j).get(i)));
            }//end for

            records.add(new Record<>(features, output.get(i)));
        }//end for

        return records;
    }

    /**
     * Saves all the targets to the output file.
     * @param path The path of the output file.
     * @param records A collection with all the records
     */
    private static void saveFile(String path, Collection<Record<String>> records) {
        try {
            // Create a new file
            BufferedWriter writer = new BufferedWriter(new FileWriter(path));
            try {
                // Write the target of each record in the each line of output file
                for (Record<String> record : records) {
                    writer.write(record.getTarget() + "\n");
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            writer.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

}//end class AdaIrisTest