package small.rtb.agent

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.scaladsl.adapter._
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}

class BidRoutesSpec extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest {
  lazy val testKit = ActorTestKit()

  implicit def typedSystem = testKit.system

  lazy val routes = new BidRoutes(bidRegistry).routes
  val bidRegistry = testKit.spawn(BidRegistry())

  override def createActorSystem(): akka.actor.ActorSystem =
    testKit.system.toClassic

  import JsonFormats._
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import model._

  "BidRoutes" should {
    "return no content for bids (GET /bids)" in {
      val request = HttpRequest(uri = "/bids")

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)

        contentType should ===(ContentTypes.`application/json`)
        entityAs[String] should ===("""{"bids":[]}""")
      }
    }
    "be able to register bid (POST /bids)" in {
      val bidRequest = BidRequest("uniqueId", None, Site(1, ""), None, None)
      val bidEntity = Marshal(bidRequest).to[MessageEntity].futureValue
      val request = Post("/bids").withEntity(bidEntity)

      request ~> routes ~> check {
        status should ===(StatusCodes.Created)
        contentType should ===(ContentTypes.`application/json`)
        entityAs[String] should ===("""{"bidRequestId":"uniqueId","id":"","price":0.0}""")
      }
    }
  }
}
