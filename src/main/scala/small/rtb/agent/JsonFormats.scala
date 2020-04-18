package small.rtb.agent

import spray.json.DefaultJsonProtocol

object JsonFormats extends DefaultJsonProtocol {

  import model._

  implicit val geoJsonFormat = jsonFormat1(Geo)
  implicit val userJsonFormat = jsonFormat2(User)
  implicit val usersJsonFormat = jsonFormat1(Users)
  implicit val routeDescJsonFormat = jsonFormat2(RouteDesc)
  implicit val actionPerformedJsonFormat = jsonFormat1(ActionPerformed)
  implicit val siteJsonFormat = jsonFormat2(Site)
  implicit val deviceJsonFormat = jsonFormat2(Device)
  implicit val impressionJsonFormat = jsonFormat8(Impression)
  implicit val bannerJsonFormat = jsonFormat4(Banner)
  implicit val targetingJsonFormat = jsonFormat1(Targeting)
  implicit val campaignJsonFormat = jsonFormat5(Campaign)
  implicit val bidRequestJsonFormat = jsonFormat5(BidRequest)
  implicit val bidResponseJsonFormat = jsonFormat5(BidResponse)
  implicit val bidJsonFormat = jsonFormat3(Bid)
  implicit val bidsJsonFormat = jsonFormat1(Bids)
}
