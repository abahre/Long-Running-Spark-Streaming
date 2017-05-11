# Long-Running-Spark-Streaming
Long-running Spark Streaming Jobs on YARN Cluster


#Install Python 2.7




Configure metrics.properties

cp /etc/spark/conf.dist/metrics.properties.template /etc/spark/conf/metrics.properties

nano /etc/spark/conf/metrics.properties

*.sink.graphite.class=org.apache.spark.metrics.sink.GraphiteSink
*.sink.graphite.host=127.0.0.1 #carbon server url
*.sink.graphite.port=2003 #carbon server port
*.sink.graphite.period=10

# Enable jvm source for instance master, worker, driver and executor
master.source.jvm.class=org.apache.spark.metrics.source.JvmSource

worker.source.jvm.class=org.apache.spark.metrics.source.JvmSource

driver.source.jvm.class=org.apache.spark.metrics.source.JvmSource

executor.source.jvm.class=org.apache.spark.metrics.source.JvmSource


####Start carbon server
service carbon-cache start
####Start graphite server
/opt/graphite/bin/run-graphite-devel-server.py /opt/graphite/
####Start Grafana server
service grafana-server start

####Start kafka server
bin/kafka-server-start.sh config/server.properties

####Start Kafka producer
bin/kafka-console-producer.sh --bror-list localhost:9092 --topic test

Run spark Jonb
export JAVA_HOME=/usr/java/jdk1.8.0_131/ && spark-submit \
--verbose --class StreamMain /home/cloudera/Downloads/spark-streaming-1.0-SNAPSHOT.jar quickstart.cloudera:2181 my-consumer-group test 1 \

--files=/etc/spark/conf/metrics.properties \
--conf spark.metrics.conf=metrics.properties \
--conf spark.metrics.namespace=my_app

