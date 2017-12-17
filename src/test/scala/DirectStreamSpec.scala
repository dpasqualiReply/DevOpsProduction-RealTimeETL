import it.reply.data.pasquali.engine.DirectStreamer
import org.scalatest.{BeforeAndAfterAll, FlatSpec}

import sys.process._


class DirectStreamSpec extends FlatSpec with BeforeAndAfterAll{

  val testTopic = "test-topic"
  var ds : DirectStreamer = null

  override def beforeAll(): Unit = {
    super.beforeAll()
    "/opt/confluent-3.3.0/bin/confluent start schema-registry" !

    "echo {\\\"key\\\": \\\"value\\\"} | kafka-avro-console-producer --broker-list localhost:9092 --topic test-topic --property value.schema='{\"type\":\"record\",\"name\":\"myrecord\",\"fields\":[{\"name\":\"key\",\"type\":\"string\"}]}'" !
  }

  override def afterAll(): Unit = {
    super.afterAll()
    "/opt/confluent-3.3.0/bin/confluent destroy" !
  }

  "The Spark DirectStreamer" must
    "init spark conf and streming context" in {

    ds = DirectStreamer()
      .initStreaming("direct streaming test", "local", 10)

    assert(ds.sc != null)
    assert(ds.ssc != null)
  }

  it must "listen to a Kafka test-topic" in {

    val message = ds
      .initKakfa("localhost", "9092", "smallest", "group1", "test-topic")
      .createDirectStream()

    message.foreachRDD(
      rdd => {
        if(!rdd.isEmpty())
        {
          val stringRDD = rdd.map(entry => entry._2)
          assert(stringRDD.collect()(0) == "{\"key\": \"value\"}")
        }
        else
          ds.ssc.stop()
      }
    )

  }



}
