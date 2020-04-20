package small.rtb.agent

import small.rtb.agent.generator.CampaignsGenerator

import scala.io.Source

object Generator {

  import JsonFormats._
  import common._
  import small.rtb.agent.model._
  import spray.json._

  def apply(): Set[Campaign] = {
    loadGeneratedCampaigns.getOrElse(Set.empty)
  }

  def loadGeneratedCampaigns: Option[Set[Campaign]] = {
    import DefaultJsonProtocol._
    val campaignsJson: String = resourceAsStreamFromSrc(List[String]("campaigns.json")) match {
      case None => ""
      case fis => Source.fromInputStream(fis.get, "UTF-8").mkString
    }

    val jsonAst = campaignsJson.parseJson
    Some(jsonAst.convertTo[Set[Campaign]])
  }

  def main(args: Array[String]): Unit = {
    val campaigns: Set[Campaign] = CampaignsGenerator(Some(4), Some(10), Some(1000))
    println(campaigns.toJson.prettyPrint)
  }
}
