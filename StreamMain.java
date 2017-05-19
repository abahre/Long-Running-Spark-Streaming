/**
 * Created by cloudera on 4/11/17.
 */

    /*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.function.VoidFunction2;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.Time;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaPairReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka.KafkaUtils;
import scala.Tuple2;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Consumes messages from one or more topics in Kafka and does wordcount.
 * <p>
 * Usage: JavaKafkaWordCount <zkQuorum> <group> <topics> <numThreads>
 * <zkQuorum> is a list of one or more zookeeper servers that make quorum
 * <group> is the name of kafka consumer group
 * <topics> is a list of one or more kafka topics to consume from
 * <numThreads> is the number of threads the kafka consumer should use
 * <p>
 * To run this example:
 * `$ bin/run-example org.apache.spark.examples.streaming.JavaKafkaWordCount zoo01,zoo02, \
 * zoo03 my-consumer-group topic1,topic2 1`
 */
/*class Get implements Function<String, Integer> {

    public void call(JavaPairRDD<String, Integer> stringIntegerJavaPairRDD) {

        List<Tuple2<String, Integer>> output =  stringIntegerJavaPairRDD.collect();

        System.out.println("\n>>>RESULTS START\n");

        System.out.println("Output Size : " + output.size());

        output.forEach( t -> System.out.println(t._1() + ":" + t._2()));

        System.out.println("\n>>>RESULTS END\n");}

    @Override
    public Integer call(String s) throws Exception {
        return null;
    }
}*/

public class StreamMain {


    private static final Pattern SPACE = Pattern.compile(" ");
    JavaPairRDD<String, Integer> stringIntegerJavaPairRDD;

    private StreamMain() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 4) {
            System.err.println("Usage: JavaKafkaWordCount <zkQuorum> <group> <topics> <numThreads>");
            System.exit(1);
        }

        // StreamingExamples.setStreamingLogLevels();
        SparkConf sparkConf = new SparkConf().setAppName("StreamMain");
        // Create the context with 2 seconds batch size
        JavaStreamingContext jssc = new JavaStreamingContext(sparkConf, new Duration(10000));

        int numThreads = Integer.parseInt(args[3]);
        Map<String, Integer> topicMap = new HashMap<>();
        String[] topics = args[2].split(",");
        for (String topic : topics) {
            topicMap.put(topic, numThreads);
        }

        JavaPairReceiverInputDStream<String, String> messages = KafkaUtils.createStream(jssc, args[0], args[1], topicMap);

        JavaDStream<String> lines = messages.map(Tuple2::_2);

        JavaDStream<String> words = lines.flatMap(x -> Arrays.asList(SPACE.split(x)).iterator());

        JavaPairDStream<String, Integer> wordCounts = words.mapToPair(s -> new Tuple2<>(s, 1))
                .reduceByKey((i1, i2) -> i1 + i2);

        wordCounts.foreachRDD(new VoidFunction2<JavaPairRDD<String, Integer>, Time>() {
            @Override
            public void call(JavaPairRDD<String, Integer> stringIntegerJavaPairRDD, Time time) throws Exception {

                List<Tuple2<String, Integer>> output = stringIntegerJavaPairRDD.collect();

                System.out.println("\n>>>RESULTS START\n");

                System.out.println("Output Size : " + output.size());

                output.forEach(t -> System.out.println(t._1() + ":" + t._2()));

                System.out.println("\n>>>RESULTS END\n");
            }
        });
        //wordCounts.foreachRDD();
        jssc.start();

        jssc.awaitTermination();
    }
}


