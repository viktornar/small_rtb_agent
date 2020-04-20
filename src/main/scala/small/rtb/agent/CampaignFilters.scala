package small.rtb.agent

import small.rtb.agent.model._


object CampaignFilters {
  def filterBySiteAndUserAndDevice(campaigns: LazyList[Campaign], site: Site, user: Option[User], device: Option[Device]): LazyList[Campaign] = {
    filterByUserOrDevice(
      filterBySite(campaigns, site),
      user,
      device
    )
  }

  def filterBySite(campaigns: LazyList[Campaign], site: Site): LazyList[Campaign] = {
    campaigns.to(LazyList).filter(sitePredicate(site.id))
  }

  def sitePredicate(id: Int, applyPredicate: Boolean = true)(c: Campaign): Boolean = {
    if (!applyPredicate) return true;
    c.targeting.targetedSiteIds.contains(id)
  }

  def filterByUserOrDevice(campaigns: LazyList[Campaign], user: Option[User], device: Option[Device]): LazyList[Campaign] = {
    val userGeo: Option[Geo] = user.getOrElse(None) match {
      case None => None;
      case u: User => u.geo
    }
    val userCountry: Option[String] = userGeo.getOrElse(None) match {
      case None => None;
      case g: Geo => g.country
    }

    val deviceGeo: Option[Geo] = device.getOrElse(None) match {
      case None => None;
      case d: Device => d.geo
    }
    val deviceCountry: Option[String] = deviceGeo.getOrElse(None) match {
      case None => None;
      case g: Geo => g.country
    }

    campaigns.to(LazyList).filter(countriesPredicate(userCountry, deviceCountry))
  }

  def countriesPredicate(userCountry: Option[String], deviceCountry: Option[String], applyPredicate: Boolean = true)(c: Campaign): Boolean = {
    if (!applyPredicate) return true;

    (userCountry, deviceCountry) match {
      case (None, None) => true
      case (Some(uc), None) => c.country == uc
      case (None, Some(dc)) => c.country == dc
      case (Some(uc), Some(dc)) => c.country == uc || c.country == dc
    }
  }

  def filterAndGroupByImpression(campaigns: LazyList[Campaign], impression: Impression): LazyList[Campaign] = {
    campaigns.to(LazyList)
      .filter(CampaignFilters.bidFloorPredicate(impression.bidFloor.getOrElse(0.0)))
      .filter(c => {
        c.banners.exists(b => {
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
        })
      })

    campaigns.to(LazyList)
  }

  def bidFloorPredicate(bidFloor: Double, applyPredicate: Boolean = true)(c: Campaign): Boolean = {
    if (!applyPredicate) return true;
    c.bid <= bidFloor
  }

  def filterByAll(campaigns: LazyList[Campaign], imp: Impressions, site: Site, user: Option[User], device: Option[Device]): LazyList[Campaign] = {
    //    imp.map[Campaign](impression => filterAndGroupByImpression(campaigns, impression)).toSet
    campaigns.to(LazyList)
  }
}
