package small.rtb.agent

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import small.rtb.agent.generator.CampaignGenerator


object CampaignActor {

  import small.rtb.agent.model._

  def apply(): Behavior[Command] = registry(Generator())

  private def registry(campaigns: Set[Campaign]): Behavior[Command] =
    Behaviors.receiveMessage {
      case GetMatchedCampaign(bidRequest, matchBy, replyTo) =>
        replyTo ! getMatchedCampaign(bidRequest, matchBy, campaigns)
        Behaviors.same
    }

  private def getMatchedCampaign(bidRequest: BidRequest, matchBy: Seq[String], campaigns: Campaigns): Option[Campaign] = {
    bidRequest match {
      case BidRequest(_, None, site, None, None) => filterBySite(campaigns, site)
      case BidRequest(_, None, site, user, device) => filterBySiteUserAndDevice(campaigns, site, user, device)
      case BidRequest(_, Some(imp), site, user, device) => filterByAll(campaigns, imp, site, user, device)
      case _ => None
    }
    Some(CampaignGenerator(None, None, None))
  }

  def filterBySite(campaigns: Set[Campaign], site: Site): LazyList[Campaign] = {
    campaigns.to(LazyList)
  }

  def filterBySiteUserAndDevice(campaigns: Set[Campaign], site: Site, user: Option[User], device: Option[Device]): LazyList[Campaign] = {
    filterBySiteUserAndDevice(campaigns: Set[Campaign], site: Site, user: Option[User], device: Option[Device])
    campaigns.to(LazyList)
  }

  def filterByUserAndDevice(campaigns: Set[Campaign], site: Site, user: Option[User], device: Option[Device]): LazyList[Campaign] = {
    val userGeo: Option[Geo] = user.getOrElse(None) match { case None => None; case Some(u: User) => u.geo}
    val userCountry: Option[String] = userGeo.getOrElse(None) match { case None => None; case Some(g: Geo) => g.country}

    val deviceGeo: Option[Geo] = device.getOrElse(None) match { case None => None; case Some(d: User) => d.geo}
    val deviceCountry: Option[String] = deviceGeo.getOrElse(None) match { case None => None; case Some(g: Geo) => g.country}

    campaigns.to(LazyList)
  }


  def filterAndGroupByImpression(campaigns: Set[Campaign], impression: Impression): Campaign = {
    campaigns.to(LazyList)
      .filter(CampaignPredicates.bidFloorPredicate(impression.bidFloor.getOrElse(0.0)))
      .filter(c => {
        c.banners.filter(b => {
          var passed = false
          if (
            b.width == impression.w.getOrElse(0) && b.height == impression.h.getOrElse(0)
          ) {
            passed = true
          }

          if (
            impression.wmax.getOrElse(0) >= b.width && impression.wmin.getOrElse(0) <= b.width &&
              impression.hmax.getOrElse(0) >= b.height && impression.hmin.getOrElse(0) <= b.height
          ) {
            passed = true
          }

          passed
        }).toList.length > 0
      })

    campaigns.to(LazyList)
  }

  def filterByAll(campaigns: Set[Campaign], imp: Impressions, site: Site, user: Option[User], device: Option[Device]): LazyList[Campaign] = {
    imp.map[Campaign](impression => filterAndGroupByImpression(campaigns, impression)).toSet
    campaigns.to(LazyList)
  }

  sealed trait Command

  final case class GetMatchedCampaign(bidRequest: BidRequest, matchBy: Seq[String], replyTo: ActorRef[Option[Campaign]]) extends Command

}
