package small.rtb.agent

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

object BidRegistry {
  def apply(): Behavior[Command] = registry(Set.empty)

  import models._

  private def registry(bids: Set[BidRequest]): Behavior[Command] =
    Behaviors.receiveMessage {
      case CreateBid(bid, replyTo) =>
        replyTo ! ActionPerformed(s"Bid ${bid.id} registered.")
        registry(bids + bid)
    }

  sealed trait Command

  final case class CreateBid(bid: BidRequest, replyTo: ActorRef[ActionPerformed]) extends Command

}
