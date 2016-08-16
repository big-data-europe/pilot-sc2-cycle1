FROM bde2020/spark-submit:1.5.1-hadoop2.6

RUN apt-get update && update-java-alternatives -s java-1.8.0-openjdk-amd64 && mkdir /app

ENV SPARK_MASTER_NAME=bigdata-one.semantic-web.at
ENV SPARK_APPLICATION_ARGS=
ENV SPARK_MASTER_URL=spark://bigdata-one.semantic-web.at:8177
ENV SPARK_MASTER_PORT=8177
ENV SPARK_APPLICATION_MAIN_CLASS=eu.bde.spark.job.sc2.App
ENV SPARK_APPLICATION_JAR_LOCATION=/app/eu-bde-spark-job-sc2-1.0.0-jar-with-dependencies.jar

COPY eu-bde-spark-job-sc2-1.0.0-jar-with-dependencies.jar /app 
