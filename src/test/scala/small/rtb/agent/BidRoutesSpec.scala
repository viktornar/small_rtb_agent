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

  lazy val routes = new BidRoutes(bidActor, campaignActor).routes
  val bidActor = testKit.spawn(BidActor())
  val campaignActor = testKit.spawn(CampaignActor())

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
    "be able to get bid response (POST /bids)" in {
      import spray.json._
      val bidRequest = BidRequest("8102f7db-ff5e-4cd7-828e-7515ffa4d860", None, Site(6, ""), None, None)
      val bidEntity = Marshal(bidRequest).to[MessageEntity].futureValue

      println(bidRequest.toJson)
      val request = Post("/bids").withEntity(bidEntity)

      request ~> routes ~> check {
        status should ===(StatusCodes.Created)
        contentType should ===(ContentTypes.`application/json`)
        entityAs[BidResponse].bidRequestId should ===("8102f7db-ff5e-4cd7-828e-7515ffa4d860")
        entityAs[BidResponse].adid.get should ===(4)
      }
    }
  }
}
