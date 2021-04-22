package lab2;

import lombok.AllArgsConstructor;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import scala.Tuple2;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Map;

/**
 * StatProcessor class contains the algorithm of data processing
 */
@AllArgsConstructor
public class StatProcessor {
    /**
     * DateTimeFormatter to parse timestamps from input file
     */
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Method, which aggregates source data
     * @param inputRdd input data represented as RDD of strings
     * @param interactionTypeMapping map, which contains associations between interaction type id and
     *                               interaction type name
     * @return RDD with aggregated data
     */
    public static JavaPairRDD<ReturnRecordKey, Long> calculate(JavaRDD<String> inputRdd,
                                                                Map<Integer, String> interactionTypeMapping){
        return inputRdd
                 .flatMap(s -> Arrays.asList(s.split("\n")).iterator())
                 .map(x -> {
                     String [] row = x.split(",");
                     if (row.length != 4) throw new RuntimeException("Invalid number of columns in data csv");
                     return new InputRecordType(Integer.parseInt(row[0]), Integer.parseInt(row[1]),
                             LocalDateTime.parse(row[2], formatter), Integer.parseInt(row[3]));
                 })
                .mapToPair(x -> new Tuple2<>(new ReturnRecordKey(x.getPostId(),
                        interactionTypeMapping.get(x.getInteractionTypeId())), 1L))
                .reduceByKey(Long::sum);
    }
}
