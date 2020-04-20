package small.rtb.agent

import small.rtb.agent.model._


object CampaignFilters {
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

  def countriesPredicate(userCountry: Option[String], deviceCountry: Option[String])(c: Campaign): Boolean = {
    (userCountry, deviceCountry) match {
      case (None, None) => true
      case (Some(uc), None) => c.country == uc
      case (None, Some(dc)) => c.country == dc
      case (Some(uc), Some(dc)) => c.country == uc || c.country == dc
    }
  }

  def filterByDimension(campaigns: LazyList[Campaign], imp: Impression): LazyList[Campaign] = {
    campaigns.filter(dimensionPredicate(imp))
  }

  def dimensionPredicate(imp: Impression)(c: Campaign): Boolean = {
    c.banners.exists(b => {
      var passed = false
      if (
        b.width == imp.w.getOrElse(0) && b.height == imp.h.getOrElse(0)
      ) {
        passed = true
      }

      if (
        imp.wmax.getOrElse(0) >= b.width && imp.wmin.getOrElse(0) <= b.width &&
          imp.hmax.getOrElse(0) >= b.height && imp.hmin.getOrElse(0) <= b.height
      ) {
        passed = true
      }

      passed
    })
  }

  def filterBySite(campaigns: LazyList[Campaign], site: Site): LazyList[Campaign] = {
    campaigns.filter(sitePredicate(site.id))
  }

  def sitePredicate(id: Int)(c: Campaign): Boolean = {
    c.targeting.targetedSiteIds.contains(id)
  }

  def filterByBidFloor(campaigns: LazyList[Campaign], imp: Impression): LazyList[Campaign] = {
    campaigns.filter(bidFloorPredicate(imp.bidFloor.getOrElse(0.0)))
  }

  def bidFloorPredicate(bidFloor: Double)(c: Campaign): Boolean = {
    c.bid <= bidFloor
  }
}
