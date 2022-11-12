package io.weaviate.spark

import org.apache.spark.sql.connector.catalog.{Table, TableProvider}
import org.apache.spark.sql.connector.expressions.Transform
import org.apache.spark.sql.sources.DataSourceRegister
import org.apache.spark.sql.types.{DataTypes, Metadata, StringType, StructField, StructType}
import org.apache.spark.sql.util.CaseInsensitiveStringMap
import technology.semi.weaviate.client.Config
import technology.semi.weaviate.client.WeaviateClient

import java.util
import scala.jdk.CollectionConverters._

class WeaviateResultError(s: String) extends Exception(s) {}
class WeaviateClassNotFoundError(s: String) extends Exception(s) {}

class Weaviate extends TableProvider with DataSourceRegister {
  override def shortName(): String = "weaviate"
  override def inferSchema(options: CaseInsensitiveStringMap): StructType = {
    val weaviateOptions = new WeaviateOptions(options)
    val config = new Config(weaviateOptions.scheme, weaviateOptions.host)
    val client = new WeaviateClient(config)
    val className = options.get("className")
    val result = client.schema.classGetter.withClassName(className).run
    if (result.hasErrors) throw new WeaviateResultError(result.getError.getMessages.toString)
    if (result.getResult == null) throw new WeaviateClassNotFoundError("Class "+className+ " was not found.")
    val properties = result.getResult.getProperties.asScala
    // TODO p.getDataType returns List<string> so need to think about how to convert to Spark datatype
    val structFields = properties.map(p => StructField(p.getName(), Utils.weaviateToSparkDatatype(p.getDataType), true, Metadata.empty))
    new StructType(structFields.toArray)
  }
  override def getTable(schema: StructType, partitioning: Array[Transform], properties: util.Map[String, String]): Table = {
    val weaviateOptions = new WeaviateOptions(new CaseInsensitiveStringMap(properties))
    WeaviateCluster(weaviateOptions)
  }
}
