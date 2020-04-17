package small.rtb.agent

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

object BidRegistry {
  def apply(): Behavior[Command] = registry(Set.empty)

  import models._

  private def registry(bids: Set[Bid]): Behavior[Command] =
    Behaviors.receiveMessage {
      case CreateBid(request, matchBy, replyTo) =>
        val response = BidResponse("", request.id, 0.0, None, None)
        replyTo ! response
        registry(bids + Bid(request, response))
      case GetBids(replyTo: ActorRef[Bids]) =>
        replyTo ! Bids(bids.toSeq)
        Behaviors.same
    }

  sealed trait Command

  final case class CreateBid(request: BidRequest, matchBy: Seq[String], replyTo: ActorRef[BidResponse]) extends Command

  final case class GetBids(replyTo: ActorRef[Bids]) extends Command

}
