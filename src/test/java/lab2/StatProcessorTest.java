package lab2;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.junit.Test;

import static lab2.StatProcessor.calculate;

import java.util.*;

/**
 * Tests for Spark application
 */
public class StatProcessorTest {

    private final String case1 = "138,551,2021-04-12 09:10:00,2";
    private final String case2 = "138,761,2021-04-12 09:10:00,3";
    private final String case3 = "138,527,2021-04-12 09:10:00,2";
    private final String case4 = "111,500,2021-04-12 09:10:00,2";

    private final static JavaSparkContext sc =
            new JavaSparkContext(new SparkConf().setAppName("SparkApplication").setMaster("local"));
    private final static Map<Integer, String> map = StatProcessorTest.init();

    private static Map<Integer, String> init() {
        Map<Integer, String> a = new HashMap<>();
        a.put(1, "opened and viewed");
        a.put(2, "opened for preview");
        a.put(3, "did not interacted");
        return a;
    }

    /**
     * Case with the single log
     */
    @Test
    public void testOneLog() {

        JavaRDD<String> input = sc.parallelize(Collections.singletonList(case1));
        JavaPairRDD<ReturnRecordKey, Long> result = calculate(input, map);
        Map <ReturnRecordKey, Long> res = result.collectAsMap();

        Map <ReturnRecordKey, Long> trueRes = new HashMap<>();
        trueRes.put(new ReturnRecordKey(138, "opened for preview"), 1L);

        assert res.equals(trueRes);
    }

    /**
     * Case with two identical interactions with the same post
     */
    @Test
    public void testTwoLogsSamePostSameType(){
        JavaRDD<String> input = sc.parallelize(Arrays.asList(case1, case3));
        JavaPairRDD<ReturnRecordKey, Long> result = calculate(input, map);
        Map <ReturnRecordKey, Long> res = result.collectAsMap();

        Map <ReturnRecordKey, Long> trueRes = new HashMap<>();
        trueRes.put(new ReturnRecordKey(138, "opened for preview"), 2L);

        assert res.equals(trueRes);

    }


    /**
     * Case with two different interactions with the same post
     */
    @Test
    public void testTwoLogsSamePostDifferentType(){
        JavaRDD<String> input = sc.parallelize(Arrays.asList(case1, case2));
        JavaPairRDD<ReturnRecordKey, Long> result = calculate(input, map);
        Map <ReturnRecordKey, Long> res = result.collectAsMap();

        Map <ReturnRecordKey, Long> trueRes = new HashMap<>();
        trueRes.put(new ReturnRecordKey(138, "opened for preview"), 1L);
        trueRes.put(new ReturnRecordKey(138, "did not interacted"), 1L);

        assert res.equals(trueRes);
    }

    /**
     * Case with two identical interactions with different posts
     */
    @Test
    public void testTwoLogsDifferentPostSameType(){
        JavaRDD<String> input = sc.parallelize(Arrays.asList(case1, case4));
        JavaPairRDD<ReturnRecordKey, Long> result = calculate(input, map);
        Map <ReturnRecordKey, Long> res = result.collectAsMap();

        Map <ReturnRecordKey, Long> trueRes = new HashMap<>();
        trueRes.put(new ReturnRecordKey(138, "opened for preview"), 1L);
        trueRes.put(new ReturnRecordKey(111, "opened for preview"), 1L);

        assert res.equals(trueRes);
    }

    /**
     * Case with all types above mixed
     */
    @Test
    public void testFourLogs(){
        JavaRDD<String> input = sc.parallelize(Arrays.asList(case1, case2, case3, case4));
        JavaPairRDD<ReturnRecordKey, Long> result = calculate(input, map);
        Map <ReturnRecordKey, Long> res = result.collectAsMap();

        Map <ReturnRecordKey, Long> trueRes = new HashMap<>();
        trueRes.put(new ReturnRecordKey(138, "opened for preview"), 2L);
        trueRes.put(new ReturnRecordKey(138, "did not interacted"), 1L);
        trueRes.put(new ReturnRecordKey(111, "opened for preview"), 1L);

        assert res.equals(trueRes);
    }

}