package eu.bde.spark.job.sc2;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import kafka.serializer.StringDecoder;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaPairInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka.KafkaUtils;

public class App 
{
    //./spark-submit --class ${SPARK_APPLICATION_MAIN_CLASS} --master ${SPARK_MASTER_URL} ${SPARK_APPLICATION_JAR_LOCATION}
    public static void main( String[] args ) {  
        // e.g. kafka-sandbox
        String APP_NAME = System.getenv("APP_NAME");
        // e.g.: /app/ , /home/turnguard/bin/apache/spark-1.6.0-bin-hadoop2.6
        String SPARK_HOME = System.getenv("SPARK_HOME");
        // e.g. eu.bde.spark.job.sc2.App
        String SPARK_APPLICATION_MAIN_CLASS = System.getenv("SPARK_APPLICATION_MAIN_CLASS");
        // e.g. spark://bigdata-one.semantic-web.at:8177
        String SPARK_MASTER_URL = System.getenv("SPARK_MASTER_URL");
        // e.g. bigdata-one.semantic-web.at:9092
        String KAFKA_METADATA_BROKER_LIST = System.getenv("KAFKA_METADATA_BROKER_LIST");
        // e.g. 192.168.88.219:2181/kafka
        String ZK_CONNECT = System.getenv("ZK_CONNECT");
        // e.g. sc2
        String KAFKA_GROUP_ID = System.getenv("KAFKA_GROUP_ID");
        // e.g. flume
        String KAFKA_TOPIC = System.getenv("KAFKA_TOPIC");
        
        SparkConf conf = new SparkConf()
                .setAppName(APP_NAME)
                .setSparkHome(SPARK_HOME)
                .setExecutorEnv("SPARK_APPLICATION_MAIN_CLASS", SPARK_APPLICATION_MAIN_CLASS)
                .setMaster(SPARK_MASTER_URL);                
        
        JavaSparkContext sc = new JavaSparkContext(conf);
        JavaStreamingContext ssc = new JavaStreamingContext(sc, new Duration(5000));

        Map<String, String> kafkaParams = new HashMap<>();
                            kafkaParams.put("metadata.broker.list", KAFKA_METADATA_BROKER_LIST);
                            kafkaParams.put("zk.connect", ZK_CONNECT);
                            kafkaParams.put("group.id", KAFKA_GROUP_ID);
        Set<String> topics = Collections.singleton(KAFKA_TOPIC);

        JavaPairInputDStream<String, String> directKafkaStream = KafkaUtils.createDirectStream(ssc,
        String.class, String.class, StringDecoder.class, StringDecoder.class, kafkaParams, topics);

        directKafkaStream.foreachRDD( rdd -> {             
            System.out.println("--- New RDD with " + rdd.partitions().size() + " partitions and " + rdd.count() + " records");
            rdd.foreach(record -> System.out.println(record._2));    
            /* process file here, convert to rdf and store in virtuoso */
            return null;
        });

        ssc.start();
        ssc.awaitTermination();
    }
}
