package eu.bde.spark.job.sc2;

import com.google.common.collect.Lists;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import kafka.serializer.DefaultDecoder;
import kafka.serializer.StringDecoder;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.io.FileUtils;
import static org.apache.commons.lang3.StringUtils.SPACE;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaPairInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka.KafkaUtils;
import scala.Tuple2;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.storage.StorageLevel;

public class App {

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
        
        input.foreachRDD(new Function<JavaPairRDD<String, byte[]>, Void>() {            
            @Override            
            public Void call(JavaPairRDD<String, byte[]> rdd) throws Exception {    
                
                rdd.values().collect().forEach(b -> {
                    try {
                        /* b = byte[], each byte[] represents a file */
                        /* example: */
                        FileUtils.writeByteArrayToFile(new File(("/xxx")), b);
                        /* feed byte[] to rdf translator */
                    } catch (IOException ex) {
                        ex.printStackTrace();                        
                    }
                });
                return null;
            }
        });
        ssc.start();
        ssc.awaitTermination();
    }
}
