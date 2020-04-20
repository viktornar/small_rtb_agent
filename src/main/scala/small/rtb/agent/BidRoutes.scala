package small.rtb.agent

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import small.rtb.agent.BidActor._
import small.rtb.agent.CampaignActor._

import scala.concurrent.{ExecutionContext, Future}

class BidRoutes(bidActor: ActorRef[BidActor.Command], campaignActor: ActorRef[CampaignActor.Command])(implicit val system: ActorSystem[_]) {

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
              entity(as[BidRequest]) { bidRequest =>
                onSuccess(getBidResponseForCampaigns(bidRequest)) { bidResponse: Option[BidResponse] =>
                  bidResponse match {
                    case None => complete(StatusCodes.NoContent)
                    case bidResponse => complete((StatusCodes.Created, bidResponse))
                  }
                }
              }
            }
          )
        }
      )
    }
  private implicit val ec: ExecutionContext = system.executionContext

  def getBids(): Future[Bids] =
    bidActor.ask(GetBids)

  def getBidResponseForCampaigns(bidRequest: BidRequest): Future[Option[BidResponse]] = {
    getMatchedCampaign(bidRequest).flatMap((campaign: Option[Campaign]) => {
      bidActor.ask(CreateBid(bidRequest, campaign, _))
    })
  }

  def getMatchedCampaign(bidRequest: BidRequest): Future[Option[Campaign]] = campaignActor.ask(GetMatchedCampaign(bidRequest, _))
}
