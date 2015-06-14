import com.aerospike.client.{ Bin, Key, AerospikeClient }

object ImportDataAerospike extends App {
  val client = new AerospikeClient("127.0.0.1", 3000)
  val (ns, bin) = ("huydx", "akkastream")
  for {
    i <- 1 to 1000
  } yield {
    val key = new Key(ns, bin, i)
    val bin1 = new Bin("bin1", s"value$i")
    client.put(null, key, bin1)
  }
  client.close()
}
