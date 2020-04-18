package small.rtb.agent.generator
import com.github.javafaker.Faker

import scala.collection.mutable.ListBuffer

object CampaignGenerator {
  import small.rtb.agent.model._

  def apply(compaignId: Option[Int], bannersLength: Option[Int], targetingSiteIdsLength: Option[Int]): Campaign = {
    val faker = new Faker()

    var siteIdsBuffer = new ListBuffer[Int]()
    for (i <- 1 to targetingSiteIdsLength.getOrElse(10000)) {
      siteIdsBuffer += i
    }
    val targeting = Targeting(targetedSiteIds = siteIdsBuffer.toList)

    var bannersBuffer = new ListBuffer[Banner]()
    for (_ <- 1 to bannersLength.getOrElse(100)) {
      bannersBuffer += Banner(
        faker.number().randomDigit(),
        faker.internet().url() + "/banner.png",
        faker.number().numberBetween(200, 600),
        faker.number().numberBetween(200, 600)
      )
    }
    val banners: Banners = bannersBuffer.toList

    Campaign(
      compaignId.getOrElse(faker.random().nextInt(1, 1000)),
      faker.country().name(),
      targeting,
      banners,
      faker.number().randomDouble(2, 1, 100)
    )
  }
}
