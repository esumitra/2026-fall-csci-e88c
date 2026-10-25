package org.cscie88c.beam

import com.spotify.scio._

/**
 *  Beam word count example
 *  run locally using
 *  sbt "beam/run --input-file=../data/alice_in_wonderland.txt --output-file=target/alice_word_count"
 *  OR
 *  sbt beam/assembly
 *  java -jar beam/target/scala-2.13/BeamJob.jar --input-file=data/alice_in_wonderland.txt --output-file=beam/target/alice_word_count
 */
object BeamJob {
  def main(args: Array[String]): Unit = {
    val (sc: ScioContext, jobArgs: Args) = ContextAndArgs(args)
    implicit val scImplicit: ScioContext = sc
    val inputFile = jobArgs.getOrElse("input-file", "data/alice_in_wonderland.txt")
    val outputFile = jobArgs.getOrElse("output-file", "beam/target/alice_word_count")
    // Define the pipeline

    val wordCounts = sc.textFile(inputFile)
      .flatMap(_.split("\\W+"))
      .map(_.toLowerCase)
      .countByValue

    // Write the output
    wordCounts.saveAsTextFile(outputFile)
    sc.run().waitUntilDone()
  }
}
