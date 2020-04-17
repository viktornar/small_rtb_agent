package small.rtb.agent

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.scaladsl.adapter._
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

  "BidRoutes" should {
    "return no content for bids (GET /bids)" in {
      val request = HttpRequest(uri = "/bids")

      request ~> routes ~> check {
        status should ===(StatusCodes.NoContent)
      }
    }
    "be able to register bid (POST /bids)" in {
      //TODO: Implement
    }
  }
}
