package org.cscie88c.kafka

import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.streams.errors.LogAndContinueExceptionHandler
import org.apache.kafka.streams.kstream.GlobalKTable
import org.apache.kafka.streams.scala._
import org.apache.kafka.streams.scala.kstream._
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig, Topology}
import org.cscie88c.kafka.model.{AccountJoiner, Transaction}

import java.time.Duration
import java.util.Properties
import scala.util.Try


object TransactionApp extends LazyLogging {


  def createStreamsConfig: Properties = {
    val applicationId = System.getProperty(StreamsConfig.APPLICATION_ID_CONFIG, "kafka-streams-app")
    val config = new Properties()
    val bootstrapServers = System.getProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
    val clientId = System.getProperty(StreamsConfig.CLIENT_ID_CONFIG, "kafka-streams-client-0")
    //default app suffix, which can be overridden
    val appSuffix = System.getProperty("app.suffix", "1")
    config.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, applicationId+appSuffix)
    config.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
    config.setProperty(StreamsConfig.CLIENT_ID_CONFIG, clientId)
    //needed for serialization and deserialization
    config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, org.apache.kafka.common.serialization.Serdes.String().getClass.getName)
    config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, org.apache.kafka.common.serialization.Serdes.String().getClass.getName)
    config.setProperty(
      StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG,
      classOf[LogAndContinueExceptionHandler].getName
    )
    config.setProperty(
      StreamsConfig.COMMIT_INTERVAL_MS_CONFIG,
      10000.toString
    )
    //configure for AWS if needed
    KafkaSharedUtils.checkAws(config,logger)
    config
  }

  /**
    * run the streaming application:
      kafka/target/universal/stage/bin/kafka \
        -Dinput.topic=transactions-input-topic \
        -Doutput.topic=transactions-output-topic
    */  
  def main(args: Array[String]): Unit = {
    // read input and output topics
    // here, we have 2 topics, one with customers and another with transactions
    val inputTxnTopic = System.getProperty("input-txn.topic", "input_txn")
    val inputCustTopic = System.getProperty("input-cust.topic", "input_cust")
    val outputTopic = System.getProperty("output.topic", "output_txn")
    logger.info(s"now using output topic: $outputTopic")
    val props = createStreamsConfig
    logger.info(s"now using bootstrap servers: ${props.getProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG)}")
    runStreamingApp(inputTxnTopic, inputCustTopic, outputTopic, props)
  }

  def runStreamingApp(
    inputTxnTopic: String,
    inputCustTopic: String,
    outputTopic: String,
    props: Properties
  ): Unit = {

    // create serdes for Transaction using circe and implicits
    import Serdes._
    import org.apache.kafka.streams.scala.ImplicitConversions._

    // create builder and stream
    val builder: StreamsBuilder = new StreamsBuilder()
    val transactions: KStream[String, String] = builder.stream[String, String](inputTxnTopic)
    //The customers is a global KTable, which means it does not need to be copied and serialized
    val customers: GlobalKTable[String, String] = builder.globalTable[String, String](inputCustTopic)

    // process the stream, change some of the transactions
    val processedTransactions: KStream[String, Transaction] = 
      // first, convert each CSV row to Transaction case class
      transactions.mapValues { transaction =>
        Try(Transaction(transaction)).toOption
      }
      // filter out any failed conversions, handling a nested Option
      .filter {(_, transactionOpt) => transactionOpt.flatten.isDefined}
      .mapValues { transactionOpt => transactionOpt.get.get }
      // increase amount by 200% , shorten number, change acct # to be 0 - 9
      .mapValues { transaction =>
        transaction.copy(
          amount = transaction.amount * 200,
          number = "T"+transaction.number.substring(0,4)+"...",
          //for demo only, we change the account number because there are only 10 customers
          accountNumber = "000"+transaction.accountNumber.toLong % 9
        )
      }

    val joiner = new AccountJoiner();
    // write the processed stream to output topic
    processedTransactions
      .join(customers)(
        (_, txn) => txn.accountNumber,
        (txn, acct) => joiner.apply(txn, acct)
      )
      //change the key when outputting
      .selectKey((_,wrap)=> wrap.txn.accountNumber)
      .mapValues( _.toString )
      // log each processed transaction
      .peek((key, transaction) =>
        logger.info(s"processed transaction: key=$key, transaction=$transaction")
      )
      .to(outputTopic)

    // build topology and start streams application
    val topology: Topology = builder.build()
    logger.info(s"topology description: ${topology.describe()}")
    val streams: KafkaStreams = new KafkaStreams(topology, props)

    // add shutdown hook to respond to SIGTERM and gracefully close Kafka Streams
    sys.ShutdownHookThread {
      streams.close(Duration.ofSeconds(10))
    }

    // start the streams application
    streams.start()
  } 
  
}
