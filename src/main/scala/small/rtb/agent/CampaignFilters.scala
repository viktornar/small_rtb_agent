package small.rtb.agent

import small.rtb.agent.model._


object CampaignFilters {

  import com.github.vickumar1981.stringdistance.StringConverter._

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
      case (Some(uc), None) => c.country.toLowerCase.soundex(uc.toLowerCase)
      case (None, Some(dc)) => c.country.toLowerCase.soundex(dc.toLowerCase)
      case (Some(uc), Some(dc)) => c.country.toLowerCase.soundex(uc.toLowerCase) || c.country.toLowerCase.soundex(dc.toLowerCase)
    }
  }

  def filterByDimension(campaigns: LazyList[Campaign], imp: Impressions): LazyList[Campaign] = {
    campaigns.filter(dimensionPredicate(imp))
  }

  def filterByBidFloor(campaigns: LazyList[Campaign], imp: Impressions): LazyList[Campaign] = {
    campaigns.filter(c => {
      imp.exists(i => {
        bidFloorPredicate(i.bidFloor.getOrElse(0))(c)
      })
    })
  }

  def dimensionPredicate(imp: Impressions)(c: Campaign): Boolean = {
    c.banners.exists(b => {
      imp.exists(i => {
        var passed = false
        if (
          b.width == i.w.getOrElse(0) && b.height == i.h.getOrElse(0)
        ) {
          passed = true
        }

        if (
          i.wmax.getOrElse(0) >= b.width && i.wmin.getOrElse(0) <= b.width &&
            i.hmax.getOrElse(0) >= b.height && i.hmin.getOrElse(0) <= b.height
        ) {
          passed = true
        }

        passed
      })
    })

  }

  def filterBySite(campaigns: LazyList[Campaign], site: Site): LazyList[Campaign] = {
    campaigns.filter(sitePredicate(site.id))
  }

  def sitePredicate(id: Int)(c: Campaign): Boolean = {
    c.targeting.targetedSiteIds.contains(id)
  }

  def bidFloorPredicate(bidFloor: Double)(c: Campaign): Boolean = {
    c.bid <= bidFloor
  }
}
