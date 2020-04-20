package small.rtb.agent

import org.scalatest.{Matchers, WordSpec}

import scala.io.Source

class CampaignFilterSpec extends WordSpec with Matchers {

  import small.rtb.agent.CampaignFilters._
  import small.rtb.agent.model._

  var campaigns = loadGeneratedTestCampaigns.to(LazyList)

  "CampaignFilters" should {
    "should return campaign filtered by user (country)" in {
      val matchedCampaign = filterByUserOrDevice(campaigns, Some(User("111", Some(Geo(Some("Oman"))))), None)
      matchedCampaign.toList.head.id should ===(1)
    }

    "should return campaign filtered by device (country)" in {
      val matchedCampaign = filterByUserOrDevice(campaigns, None, Some(Device("111", Some(Geo(Some("Oman"))))))
      matchedCampaign.toList.head.id should ===(1)
    }

    "should return campaign filtered by user (country) or device (country)" in {
      val matchedCampaigns = filterByUserOrDevice(campaigns, Some(User("111", Some(Geo(Some("Pakistan"))))), Some(Device("111", Some(Geo(Some("Oman"))))))
      matchedCampaigns.length should ===(2)
      matchedCampaigns.toList.head.id should ===(1)
      matchedCampaigns.toList.tail.head.id should ===(2)
    }

    "should return campaign filtered by targeting site" in {
      val matchedCampaigns = filterBySite(campaigns, Site(6, domain = ""))
      matchedCampaigns.length should ===(1)
      matchedCampaigns.toList.head.id should ===(2)
    }

    "should return campaign filtered by targeting site and country(user or device)" in {
      val matchedCampaigns = filterBySiteAndUserAndDevice(campaigns, Site(6, domain = ""), Some(User("111", Some(Geo(Some("Pakistan"))))), Some(Device("111", Some(Geo(Some("Oman"))))))
      matchedCampaigns.length should ===(1)
      matchedCampaigns.toList.head.id should ===(2)
    }
  }

  // Should be moved to test common module?
  def loadGeneratedTestCampaigns: Set[Campaign] = {
    import JsonFormats._
    import small.rtb.agent.common._
    import small.rtb.agent.model._
    import spray.json._

    val campaignsJson: Option[String] = resourceAsStreamFromSrc(List[String]("campaigns-test_3_5_10.json")) match {
      case None => None
      case fis => Option(Source.fromInputStream(fis.get, "UTF-8").mkString)
    }

    campaignsJson match {
      case None => Set.empty
      case Some(c) =>
        val jsonAst = c.parseJson
        jsonAst.convertTo[Set[Campaign]]
    }
  }
}
