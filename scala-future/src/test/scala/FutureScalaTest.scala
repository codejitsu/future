package future.scala

import java.util.concurrent.Executors
import java.util.logging.{Level, Logger}

import org.apache.commons.httpclient.{MultiThreadedHttpConnectionManager, HttpClient}
import org.apache.commons.httpclient.methods.GetMethod
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span, Millis}
import org.scalatest.{BeforeAndAfterAll, Matchers, FlatSpec}

import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Await, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}
import scala.util.control.NonFatal

/**
 * Scala Futures.
 */

case class GetResult(url: String, duration: Long)

class FutureScalaTest  extends FlatSpec with Matchers with BeforeAndAfterAll with ScalaFutures {
  implicit val context = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(10))
  implicit val defaultPatience =
    PatienceConfig(timeout = Span(30, Seconds), interval = Span(500, Millis))

  val client = new HttpClient(new MultiThreadedHttpConnectionManager)

  var urls: collection.mutable.ListBuffer[String] = _

  override def beforeAll(): Unit = {
    Logger.getLogger("org.apache.commons.httpclient").setLevel(Level.OFF)
    Logger.getLogger("httpclient").setLevel(Level.OFF)

    urls = ListBuffer[String]()

    urls += "http://www.google.com"
    urls += "http://www.yahoo.com"
    urls += "http://www.bing.com"
    urls += "http://www.notexisteddomain.org"
  }

  "Scala Futures" can "be created asyncronously" in {
    urls foreach { url =>
      val start = System.currentTimeMillis()

      info(s"Before future for $url. Timestamp: $start")

      val fut = getUrl(url)

      info(s"Future for $url created at $start, completed: ${fut.isCompleted}")
    }
  }

  it can "be handled blocking way" in {
    urls foreach { url =>
      val start = System.currentTimeMillis()

      info(s"Before future for $url. Timestamp: $start")

      val fut = getUrl(url)

      val res = Await.result(fut, 10 seconds)

      res match {
        case Success(getRes) => info(s"Future for ${getRes.url} created at $start, duration: ${getRes.duration} ms.")
        case Failure(e) => info(s"Future for $url created at $start failed, exception: ${e.getMessage}")
      }
    }
  }

  it can "be handled async way with callbacks" in {
    val all = Future.traverse(urls) { url =>
      val start = System.currentTimeMillis()

      info(s"Before future for $url. Timestamp: $start")

      val fut = getUrl(url)

      fut.onComplete { tryRes =>
        val res = tryRes.flatten

        res match {
          case Success(getRes) => info(s"Future for ${getRes.url} created at $start, duration: ${getRes.duration} ms.")
          case Failure(e) => info(s"Future for $url created at $start failed, exception: ${e.getMessage}")
        }
      }

      fut
    }

    whenReady(all) { res =>
      info("All futures completed.")
    }
  }

  def getUrl(url: String): Future[Try[GetResult]] = Future {
    val get = new GetMethod(url)

    val start = System.currentTimeMillis()

    try {
      client.executeMethod(get)
      Success(GetResult(url, System.currentTimeMillis() - start))
    } catch {
      case NonFatal(e) => Failure(e)
    }
  }
}
