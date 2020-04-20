package small.rtb.agent

import small.rtb.agent.generator.CampaignsGenerator

import scala.io.Source

object Generator {

  import JsonFormats._
  import common._
  import small.rtb.agent.model._
  import spray.json._

  def apply(): Set[Campaign] = {
    loadGeneratedCampaigns
  }

  def loadGeneratedCampaigns: Set[Campaign] = {
    val campaignsJson: Option[String] = resourceAsStreamFromSrc(List[String]("campaigns.json")) match {
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

  def main(args: Array[String]): Unit = {
    val campaigns: Set[Campaign] = CampaignsGenerator(Some(20), Some(10), Some(1000))
    println(campaigns.toJson.prettyPrint)
  }
}
