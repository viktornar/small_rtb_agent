package small.rtb.agent

import small.rtb.agent.model.Campaign


object CampaignPredicates {
  def bidFloorPredicate(bidFloor: Double, applyPredicate: Boolean = true)(c: Campaign): Boolean = {
    if (!applyPredicate) return true;
    c.bid <= bidFloor
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
}
