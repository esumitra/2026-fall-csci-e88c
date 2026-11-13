package org.cscie88c.kafka

import com.typesafe.scalalogging.Logger

import java.util.Properties

object KafkaSharedUtils {
  
  def checkAws(props: Properties, logger: Logger): Unit = {
    val isAws = System.getProperty("mode.aws", "false").toBoolean
    if (isAws) {
      println("Using aws mode")
      props.put("security.protocol", "SASL_SSL")
      props.put("sasl.mechanism", "AWS_MSK_IAM")
      props.put("sasl.jaas.config", "software.amazon.msk.auth.iam.IAMLoginModule required;")
      props.put("sasl.client.callback.handler.class", "software.amazon.msk.auth.iam.IAMClientCallbackHandler")
    }
    logger.info(s"security.protocol = ${props.getProperty("security.protocol")}")
    logger.info(s"sasl.mechanism = ${props.getProperty("sasl.mechanism")}")
    logger.info(s"bootstrap.servers = ${props.getProperty("bootstrap.servers")}") 
  }
  

}
