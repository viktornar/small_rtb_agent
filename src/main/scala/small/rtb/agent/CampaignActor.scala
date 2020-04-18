package small.rtb.agent

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import small.rtb.agent.generator.CampaignGenerator

object CampaignActor {

  import small.rtb.agent.model._

  def apply(): Behavior[Command] = registry(Generator())

  private def registry(campaigns: Set[Campaign]): Behavior[Command] =
    Behaviors.receiveMessage {
      case GetMatchedCampaign(bidRequest, matchBy, replyTo) =>
        replyTo ! getMatchedCampaign(bidRequest, matchBy, campaigns)
        Behaviors.same

    }

  private def getMatchedCampaign(bidRequest: BidRequest, matchBy: Seq[String], campaigns: Campaigns): Campaign = {
    CampaignGenerator(None, None, None)
  }

  sealed trait Command

  final case class GetMatchedCampaign(bidRequest: BidRequest, matchBy: Seq[String], replyTo: ActorRef[Campaign]) extends Command

}
