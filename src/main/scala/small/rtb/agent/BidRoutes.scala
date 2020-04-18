package small.rtb.agent

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.unmarshalling.PredefinedFromStringUnmarshallers._
import akka.util.Timeout
import small.rtb.agent.BidRegistry._

import scala.concurrent.Future

class BidRoutes(bidRegistry: ActorRef[BidRegistry.Command])(implicit val system: ActorSystem[_]) {

  import JsonFormats._
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import small.rtb.agent.model._

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
              parameters(Symbol("matches").as(CsvSeq[String]).?) {
                matches => {
                  entity(as[BidRequest]) { bid =>
                    onSuccess(createBid(bid, matches)) { response =>
                      complete((StatusCodes.Created, response))
                    }
                  }
                }
              }

            }
          )
        }
      )
    }

  def getBids(): Future[Bids] =
    bidRegistry.ask(GetBids)

  def createBid(bid: BidRequest, matches: Option[Seq[String]]): Future[BidResponse] = {
    val matchBy = defaultMatchesValues.intersect(matches.getOrElse(defaultMatchesValues)) match {
      case List() => defaultMatchesValues
      case xs => xs
    }

    bidRegistry.ask(CreateBid(bid, matchBy, _))
  }
}
