package small.rtb.agent.generator
import com.github.javafaker.Faker

import scala.collection.mutable.ListBuffer


object BidRequestGenerator {
  import small.rtb.agent.model._


  def apply(isUser: Boolean, isDevice: Boolean, isGeo: Boolean, impressionLength: Option[Int], targetedSiteIdsLength: Int): BidRequest = {
    val faker = new Faker()

    var impressionsBuffer = new ListBuffer[Impression]()
    for(_ <- 1 to impressionLength.getOrElse(100)) {
      impressionsBuffer += Impression(
        faker.internet().uuid(),
        Some(faker.number().numberBetween(200, 600)),
        Some(faker.number().numberBetween(200, 600)),
        Some(faker.number().numberBetween(200, 600)),
        Some(faker.number().numberBetween(200, 600)),
        Some(faker.number().numberBetween(200, 600)),
        Some(faker.number().numberBetween(200, 600)),
        Some(faker.number().randomDouble(2,1,100))
      )
    }
    val impressions = Some(impressionsBuffer.toList)

    val site = Site(faker.random().nextInt(1, targetedSiteIdsLength), faker.internet().domainName())

    val geo:Option[Geo] = isGeo match {
      case false => None
      case true => Some(Geo(Some(faker.country().name())))
    }

    val user:Option[User] = isUser match {
      case false => None
      case true => Some(User(faker.internet().uuid(), geo))
    }

    val device:Option[Device] = isDevice match {
      case false => None
      case true => Some(Device(faker.internet().uuid(), geo))
    }

    BidRequest(faker.internet().uuid(), impressions, site, user, device)
  }
}
