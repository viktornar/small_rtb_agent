package small.rtb.agent

import scala.collection.immutable

package object model {
  type Banners = List[Banner]

  type Campaigns = List[Campaign]

  type BidRequests = List[BidRequest]

  type Impressions = List[Impression]

  final case class Campaign(id: Int, country: String, targeting: Targeting, banners: Banners, bid: Double)

  final case class Targeting(targetedSiteIds: List[Int])

  final case class Banner(id: Int, src: String, width: Int, height: Int)

  final case class Impression(id: String, wmin: Option[Int], wmax: Option[Int], w: Option[Int], hmin: Option[Int], hmax: Option[Int], h: Option[Int], bidFloor: Option[Double])

  final case class Site(id: Int, domain: String)

  final case class Device(id: String, geo: Option[Geo])

  final case class Geo(country: Option[String])

  final case class BidRequest(id: String, imp: Option[Impressions], site: Site, user: Option[User], device: Option[Device])

  final case class BidResponse(id: String, bidRequestId: String, price: Double, adid: Option[String], banner: Option[Banner])

  final case class Bid(request: BidRequest, response: BidResponse, campaign: Option[Campaign])

  final case class Bids(bids: immutable.Seq[Bid])

  final case class RouteDesc(path: String, info: String)

  final case class User(id: String, geo: Option[Geo])

  final case class Users(users: immutable.Seq[User])

  final case class ActionPerformed(description: String)

  val defaultMatchesValues = immutable.Seq("bidFloor", "country", "siteId", "dimension")
}
