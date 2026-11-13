package org.cscie88c.kafka

import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.clients.producer.{
  KafkaProducer,
  ProducerConfig,
  ProducerRecord
}
import org.apache.kafka.streams.StreamsConfig

import java.util.{Date, Properties}

/**
 * Example of a producer using the send API
 */
object ProducerExample extends App with LazyLogging{
    val outputTopic = System.getProperty("output.topic", "output_timestamp")
    val bootstrapServers = System.getProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")

    val props = new Properties()
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
    KafkaSharedUtils.checkAws(props, logger)

    val producer = new KafkaProducer[String, String](props)

    val record = new ProducerRecord[String, String](outputTopic, "key", "Hello Kafka from Scala at "+new Date())
    producer.send(record)
    producer.close()
}
