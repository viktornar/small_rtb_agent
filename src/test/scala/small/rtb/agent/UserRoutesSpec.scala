package small.rtb.agent

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.scaladsl.adapter._
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}

class UserRoutesSpec extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest {
  lazy val testKit = ActorTestKit()

  implicit def typedSystem = testKit.system

  override def createActorSystem(): akka.actor.ActorSystem =
    testKit.system.toClassic

  val userRegistry = testKit.spawn(UserRegistry())
  lazy val routes = new UserRoutes(userRegistry).routes

  import JsonFormats._
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import models._

  "UserRoutes" should {
    "return no users if no present (GET /users)" in {
      val request = HttpRequest(uri = "/users")

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)

        contentType should ===(ContentTypes.`application/json`)
        entityAs[String] should ===("""{"users":[]}""")
      }
    }
    "be able to add users (POST /users)" in {
      val user = User("Kapi", None)
      val userEntity = Marshal(user).to[MessageEntity].futureValue
      val request = Post("/users").withEntity(userEntity)

      request ~> routes ~> check {
        status should ===(StatusCodes.Created)
        contentType should ===(ContentTypes.`application/json`)
        entityAs[String] should ===("""{"description":"User Kapi created."}""")
      }
    }

    "be able to remove users (DELETE /users)" in {
      val request = Delete(uri = "/users/Kapi")

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)

        contentType should ===(ContentTypes.`application/json`)

        entityAs[String] should ===("""{"description":"User Kapi deleted."}""")
      }
    }
  }
}
