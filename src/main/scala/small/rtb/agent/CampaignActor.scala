package small.rtb.agent

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}


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
    val filteredCampaigns: Option[List[Campaign]] = bidRequest match {
      case BidRequest(_, None, site, None, None) =>
        Some(filterBySite(campaigns.to(LazyList), site).toList)
      case BidRequest(_, None, site, user, device) =>
        Some(filterByUserOrDevice(
          filterBySite(campaigns.to(LazyList), site),
          user,
          device,
        ).toList)
      case BidRequest(_, Some(imp), site, user, device) =>
        Some(filterByDimension(filterByBidFloor(filterByUserOrDevice(
          filterBySite(campaigns.to(LazyList), site),
          user,
          device,
        ), imp), imp).toList)
      case _ => None
    }

    filteredCampaigns match {
      case Some(cs) => Some(cs.sortWith(_.bid > _.bid).head) // The first one should be more suitable for bid response? Basically, the first element have the biggest bid so it should be prioritized
      case _ => None
    }
  }

  sealed trait Command

  final case class GetMatchedCampaign(bidRequest: BidRequest, replyTo: ActorRef[Option[Campaign]]) extends Command

}
