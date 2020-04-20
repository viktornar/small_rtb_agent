package small.rtb.agent.generator

import scala.collection.mutable.ListBuffer

object CampaignsGenerator {
  import small.rtb.agent.model._

  def apply(campaignsLength: Option[Int], bannersLength: Option[Int], targetingSiteIdsLength: Option[Int]): Set[Campaign] = {
    var campaignsBuffer = new ListBuffer[Campaign]()
    for (id <- 1 to campaignsLength.getOrElse(10)) {
      campaignsBuffer += CampaignGenerator(Some(id), bannersLength, targetingSiteIdsLength)
    }

    campaignsBuffer.toSet
  }
}
