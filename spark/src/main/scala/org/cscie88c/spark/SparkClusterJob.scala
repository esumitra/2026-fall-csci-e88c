package org.cscie88c.spark

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{Dataset, DataFrame}

/**
  * Example data pipeline for Spark cluster job using parquet files as input and output.
  * This pipeline includes
  *  - raw ingestion
  *  - bronze layer for data quality
  *  - silver layer for transformation and enrichment
  *  - gold layer for aggregation and reporting
  * Run in docker using
  *   docker compose -f docker-compose-spark.yml up
  *   docker exec -it spark-master /bin/bash
  *   /opt/spark/bin/spark-submit --class org.cscie88c.spark.SparkClusterJob --master spark://spark-master:7077 /opt/spark-apps/SparkJob.jar /opt/spark-data/userdata1.parquet /opt/spark-data/results
  * Review results with
  *   duckdb -c "SELECT * FROM read_parquet('data/results/part-*.parquet') LIMIT 20;"
  */

case class UserData(
  registration_dttm: String,
  id: Int,
  first_name: String,
  last_name: String,
  email: String,
  gender: String,
  ip_address: String,
  cc: String,
  country: String,
  birthdate: String,
  salary: Option[Double],
  title: String,
  comments: String
)

object SparkClusterJob {

  def main(args: Array[String]): Unit = {
    if (args.length != 2) {
      System.err.println("Usage: SparkClusterJob <input> <output>")
      System.exit(2)
    }

    val inputPath  = args(0)
    val outputPath = args(1)

    val spark = SparkSession.builder()
      .appName("ClusterSparkJob")
      .config("spark.executor.memory", "1g")
      .getOrCreate()

    import spark.implicits._

    // 1. raw ingestion
    val rawData: Dataset[UserData] =
      spark
        .read
        .parquet(inputPath)
        .as[UserData]

    // 2. Bronze layer: Data quality
    // Perform data quality checks here (e.g., remove nulls, validate formats)
    val bronzeData = 
      rawData
        .filter(user => user.salary.isDefined)

    // 3. Silver layer: Transformation and enrichment
    // Perform transformations and enrichments here (e.g., add derived columns, join with reference data)
    val silverData: DataFrame = 
      bronzeData
        .withColumn("birth_year", year(try_to_timestamp($"birthdate", lit("M/d/yyyy"))))
        .withColumn("salary_band",
          when($"salary" < 50000, "<50k")
          .when($"salary" < 100000, "50–100k")
          .when($"salary" < 200000, "100–200k")
          .when($"salary".isNotNull, "200k+"))
        .drop("cc")   // keep raw card numbers out of downstream DataFrames
        .cache()

    // 4. Gold layer: Aggregation and reporting
    // Perform aggregations and generate reports here (e.g., compute metrics, create dashboards)
    val goldData: DataFrame = 
      silverData
        .groupBy("country", "salary_band")
        .agg(
          count("*").as("user_count"),
          avg("salary").as("avg_salary")
        )
        .cache()

    // 5. write the gold data to the output path
    goldData.write.mode("overwrite").parquet(outputPath)



    spark.stop()
  }
}
