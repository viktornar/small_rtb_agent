package small.rtb.agent

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import com.github.javafaker.Faker

object BidActor {

  def apply(): Behavior[Command] = registry(Set.empty)

  import small.rtb.agent.CampaignFilters.getMatchedBanner
  import small.rtb.agent.model._

  def getBidResponse(bidRequest: BidRequest, campaign: Option[Campaign]): Option[BidResponse] = {
    campaign match {
      case None => None
      case Some(c) =>
        val faker = new Faker() // Better to initialize in apply?
        // Not nice solution. Basically CampaignFilters should be responsible for formatting correct
        // campaign with best matched banner :( on top
        val banner = getMatchedBanner(c, bidRequest.imp)
        Some(BidResponse(faker.internet().uuid(), bidRequest.id, c.bid, Some(c.id), banner))
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
