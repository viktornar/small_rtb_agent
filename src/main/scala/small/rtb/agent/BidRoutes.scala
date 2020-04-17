package small.rtb.agent

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{as, complete, concat, entity, get, onSuccess, pathEnd, pathPrefix, post}
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import small.rtb.agent.BidRegistry._

import scala.concurrent.Future

class BidRoutes(bidRegistry: ActorRef[BidRegistry.Command])(implicit val system: ActorSystem[_]) {

  import JsonFormats._
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import models._

  private implicit val timeout = Timeout.create(system.settings.config.getDuration("small-rtb-agent-app.routes.ask-timeout"))

  val routes: Route =
    pathPrefix("bids") {
      concat(
        pathEnd {
          concat(
            get {
              complete(getBids())
            },
            post {
              entity(as[BidRequest]) { bid =>
                onSuccess(createBid(bid)) { response =>
                  complete((StatusCodes.Created, response))
                }
              }
            }
          )
        }
      )
    }

  def getBids(): Future[Bids] =
    bidRegistry.ask(GetBids)

  def createBid(bid: BidRequest): Future[BidResponse] =
    bidRegistry.ask(CreateBid(bid, _))
}
