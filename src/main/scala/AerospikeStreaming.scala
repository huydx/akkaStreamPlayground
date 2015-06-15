import akka.actor.{ Props, ActorSystem }
import akka.stream.{ ActorFlowMaterializerSettings, ActorFlowMaterializer }
import akka.stream.actor.ActorSubscriberMessage.{ OnError, OnNext }
import akka.stream.actor.{ WatermarkRequestStrategy, ActorSubscriber, ActorPublisher }
import akka.stream.actor.ActorPublisherMessage._
import akka.stream.scaladsl._
import com.aerospike.client.{ Record, AerospikeClient }
import com.aerospike.client.query.{ RecordSet, Statement }

import scala.concurrent.Future

class AESubscriber extends ActorSubscriber {
  import context.dispatcher
  override val requestStrategy = WatermarkRequestStrategy(4, 2)
  def receive = {
    case OnNext(rec: Record) =>
      val fprint = Future { println(s"next $rec"); Thread.sleep(1000) }
      fprint onComplete {
        case _ => request(5)
      }
    case OnError(t: Throwable) => // Flow内で何かしらExceptionを吐いた場合
      context.stop(self)
    case OnCompleteSink(_) => println("completed")
  }
}

class AEPublisher(rs: RecordSet) extends ActorPublisher[Record] {
  def receive = {
    case Request(_) =>
      if (totalDemand > 0) {
        if (rs.next()) onNext(rs.getRecord)
        else { onComplete() }
      }
    case Cancel => context.stop(self)
  }
}

object AerospikeStreaming extends App {
  implicit val actorSystem = ActorSystem()
  implicit val mat = ActorFlowMaterializer(
    // input bufferの数でActorの立ち上がる数が決まるのでパフォーマンスに影響がでる
    ActorFlowMaterializerSettings(actorSystem).withInputBuffer(initialSize = 4, maxSize = 4)
  )

  val client = new AerospikeClient("127.0.0.1", 3000)
  val (ns, set) = ("huydx", "akkastream")

  val stmt = new Statement
  stmt.setNamespace(ns)
  stmt.setSetName(set)

  val recordSet = client.query(null, stmt)
  val in = Source[Record](Props(classOf[AEPublisher], recordSet))
  val out = Sink[Record](Props(classOf[AESubscriber]))
  val g = FlowGraph { implicit builder =>
    import akka.stream.scaladsl.FlowGraphImplicits._
    in ~> out
  }
  g.run()
}
