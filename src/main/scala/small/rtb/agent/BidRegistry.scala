package small.rtb.agent

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

object BidRegistry {
  def apply(): Behavior[Command] = registry(Set.empty)

  import models._

  private def registry(bids: Set[Bid]): Behavior[Command] =
    Behaviors.receiveMessage {
      case CreateBid(request, replyTo) =>
        val response = BidResponse("", request.id, 0.0, None, None)
        replyTo ! response
        registry(bids + Bid(request, response))
    }

  sealed trait Command

  final case class CreateBid(request: BidRequest, replyTo: ActorRef[BidResponse]) extends Command

}
