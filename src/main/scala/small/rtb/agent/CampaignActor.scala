package small.rtb.agent

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import small.rtb.agent.generator.CampaignGenerator


object CampaignActor {

  import small.rtb.agent.CampaignFilters._
  import small.rtb.agent.model._

  def apply(): Behavior[Command] = registry(Generator())

  private def registry(campaigns: Set[Campaign]): Behavior[Command] =
    Behaviors.receiveMessage {
      case GetMatchedCampaign(bidRequest, replyTo) =>
        replyTo ! getMatchedCampaign(bidRequest, campaigns)
        Behaviors.same
    }

  private def getMatchedCampaign(bidRequest: BidRequest, campaigns: Set[Campaign]): Option[Campaign] = {
    bidRequest match {
      case BidRequest(_, None, site, None, None) =>
        filterBySite(campaigns.to(LazyList), site)
      case BidRequest(_, None, site, user, device) =>
        filterByUserOrDevice(
          filterBySite(campaigns.to(LazyList), site),
          user,
          device,
        )
      case BidRequest(_, Some(imp), site, user, device) =>
        filterByDimension(filterByBidFloor(filterByUserOrDevice(
          filterBySite(campaigns.to(LazyList), site),
          user,
          device,
        ), imp), imp)
      case _ => None
    }
    Some(CampaignGenerator(None, None, None))
  }

  sealed trait Command

  final case class GetMatchedCampaign(bidRequest: BidRequest, replyTo: ActorRef[Option[Campaign]]) extends Command

}
