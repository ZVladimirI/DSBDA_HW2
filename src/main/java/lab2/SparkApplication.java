package lab2;

import lombok.extern.slf4j.Slf4j;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

import java.util.Arrays;
import java.util.Map;

/**
 * Spark application entry point class
 */
@Slf4j
public class SparkApplication {

    /**
     * main() method
     * @param args input CLI arguments. Requires 3 arguments:
     *             - inputFile in HDFS
     *             - mappingFile in HDFS
     *             - output directory (should not exist)
     *
     */
    public static void main(String[] args) {
        if (args.length < 3) {
            throw new RuntimeException("Usage: java -jar path/to/jar inputFile mappingFile outputDirectory");
        }

        log.info("Application started!");
        log.debug("Application started");
        SparkConf conf =  new SparkConf().setAppName("SparkApplication").setMaster("local");
        JavaSparkContext sc = new JavaSparkContext(conf);
        JavaRDD<String> inputRdd = sc.textFile(args[0]);
        JavaRDD<String> mapRdd = sc.textFile(args[1]);
        Map<Integer, String> map = mapRdd
                .flatMap(s -> Arrays.asList(s.split("\n")).iterator())
                .mapToPair(x -> {
                    String[] row = x.split(",");
                    if (row.length != 2) throw new RuntimeException("Invalid number of columns in mapping csv");
                    return new Tuple2<>(Integer.parseInt(row[0]), row[1]);
                })
                .collectAsMap();

        log.info("===============Processing...================");
        JavaPairRDD<ReturnRecordKey, Long> result = StatProcessor.calculate(inputRdd, map);
        log.info("============SAVING FILE TO " + args[2] + " directory============");
        result.saveAsTextFile(args[2]);
        log.info("==============TASK COMPLETED===================");
    }
}