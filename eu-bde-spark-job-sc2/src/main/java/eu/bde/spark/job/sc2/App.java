package eu.bde.spark.job.sc2;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import kafka.serializer.DefaultDecoder;
import kafka.serializer.StringDecoder;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka.KafkaUtils;
import scala.Tuple2;
import org.apache.spark.storage.StorageLevel;

public class App {
    
    final static Logger LOGGER = Logger.getLogger(App.class.getName());

    //./spark-submit --class ${SPARK_APPLICATION_MAIN_CLASS} --master ${SPARK_MASTER_URL} ${SPARK_APPLICATION_JAR_LOCATION}
    public static void main(String[] args) {
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
        
        
        JavaStreamingContext ssc = new JavaStreamingContext(sc, new Duration(10000));

        Map<String, String> kafkaParams = new HashMap<>();
        kafkaParams.put("metadata.broker.list", KAFKA_METADATA_BROKER_LIST);
        kafkaParams.put("zk.connect", ZK_CONNECT);
        kafkaParams.put("zookeeper.connect", ZK_CONNECT);
        kafkaParams.put("group.id", KAFKA_GROUP_ID);
        Map<String, Integer> topicMap = new HashMap<>();
                             topicMap.put(KAFKA_TOPIC, 3);
        
        JavaPairDStream<String, byte[]> input = KafkaUtils.createStream(ssc, String.class, byte[].class,
                StringDecoder.class, DefaultDecoder.class, kafkaParams, topicMap, StorageLevel.MEMORY_ONLY());
        
        input.foreachRDD((JavaPairRDD<String, byte[]> rdd) -> {
            /* usage example below: by flume pipeline definition rdd.key = fileName, rdd.value = file data as byte[] */
            rdd.collect().forEach((Tuple2<String, byte[]> t) -> {
                try {
                    String fileName = t._1 != null ? t._1 : new String(MessageDigest.getInstance("MD5").digest((new Date()).toString().getBytes()));
                    FileUtils.writeByteArrayToFile(new File(("/home/bde/".concat(fileName))), t._2);
                } catch ( IOException | NoSuchAlgorithmException ex) {
                    LOGGER.fatal(ex);
                }
            });
            return null;
        });
        ssc.start();
        ssc.awaitTermination();
    }
}
