package small.rtb.agent

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

object BidActor {

  def apply(): Behavior[Command] = registry(Set.empty)

  import small.rtb.agent.model._

  def getBidResponse(bidRequest: BidRequest, campaign: Option[Campaign]): Option[BidResponse] = {
    campaign match {
      case None => None
      case Some(campaign) => Some(BidResponse("", bidRequest.id, 0.0, None, None))
    }
  }

  private def registry(bids: Set[Bid]): Behavior[Command] = {
    Behaviors.receiveMessage {
      case GetBids(replyTo) =>
        replyTo ! Bids(bids.toSeq)
        Behaviors.same
      case CreateBid(bidRequest, campaign, replyTo) =>
        val bidResponse = getBidResponse(bidRequest, campaign)
        replyTo ! bidResponse
        registry(bids + Bid(bidRequest, bidResponse, campaign))
    }
  }

  sealed trait Command
  final case class GetBids(replyTo: ActorRef[Bids]) extends Command

  final case class CreateBid(bidRequest: BidRequest, campaign: Option[Campaign], replyTo: ActorRef[Option[BidResponse]]) extends Command
}
