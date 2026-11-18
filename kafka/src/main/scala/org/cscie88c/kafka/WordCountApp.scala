package org.cscie88c.kafka

import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.kstream.Produced
import org.apache.kafka.streams.{KafkaStreams, StreamsBuilder, StreamsConfig}
import org.cscie88c.kafka.model.AccountJoiner

import java.util.Properties
import scala.jdk.CollectionConverters._

/**
 * Counts the words and outputs them
 * This overrides App, so no need to define main
 */
object WordCountJava extends App with LazyLogging{

  val inputWordTopic = System.getProperty("input_word.topic", "input_word")
  val outputTopic = System.getProperty("output.topic", "output_word")
  //an app suffix allows us to rerun this, consuming as a different consumer group
  //so we can consume from the beginning of the topic
  val appIdSuffix = System.getProperty("app.suffix", "1")
  val bootstrapServers = System.getProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
  val props = new Properties()
  props.put(StreamsConfig.APPLICATION_ID_CONFIG, "wordcount"+appIdSuffix)
  props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
  // Configure default serdes
  props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, classOf[Serdes.StringSerde])
  props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, classOf[Serdes.StringSerde])
  
  //this is needed in case we run on AWS
  KafkaSharedUtils.checkAws(props,logger)


  val builder = new StreamsBuilder  // Java StreamsBuilder
  val textLines = builder.stream[String, String](inputWordTopic)

  val joiner = new AccountJoiner();
  //uses the Java API, so convert to Java list
  val wordCounts = textLines
    .flatMapValues(_.toLowerCase.split("\\W+").toList.asJava)
    //this produces a grouped stream
    .groupBy((_, word) => word)
    //now we aggregate, producing a KTable, which is a local cache
    .count()
    //if we serialize as a long, then data will show up as bytes
    //use string output for readability
    .mapValues(_.toString)


  wordCounts.toStream.to(outputTopic, Produced.`with`(Serdes.String(), Serdes.String()))

  val streams = new KafkaStreams(builder.build(), props)
  streams.start()
}
