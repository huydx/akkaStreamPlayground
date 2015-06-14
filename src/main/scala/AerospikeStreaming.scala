import akka.actor.{ Props, ActorSystem }
import akka.stream.ActorFlowMaterializer
import akka.stream.actor.ActorPublisher
import akka.stream.actor.ActorPublisherMessage._
import akka.stream.scaladsl._
import com.aerospike.client.{ Record, AerospikeClient }
import com.aerospike.client.query.{ RecordSet, Statement }

class AEPublisher(rs: RecordSet) extends ActorPublisher[Record] {
  def receive = {
    case Request(_) =>
      rs.next()
      onNext(rs.getRecord)
    case Cancel =>
      println("cancelled")
      context.stop(self)
  }
}

object AerospikeStreaming extends App {
  implicit val actorSystem = ActorSystem()
  implicit val flowMaterializer = ActorFlowMaterializer()

  val client = new AerospikeClient("127.0.0.1", 3000)
  val (ns, set) = ("huydx", "akkastream")

  val stmt = new Statement
  stmt.setNamespace(ns)
  stmt.setSetName(set)

  val recordSet = client.query(null, stmt)
  val in = Source[Record](Props(classOf[AEPublisher], recordSet))
  val out = Sink.foreach[Record](println)
  val g = FlowGraph { implicit builder =>
    import akka.stream.scaladsl.FlowGraphImplicits._
    in ~> out
  }
  g.run()
}
