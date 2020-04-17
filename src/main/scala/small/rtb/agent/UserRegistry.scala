package small.rtb.agent

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import small.rtb.agent.models.{ActionPerformed, User, Users}

object UserRegistry {

  sealed trait Command

  final case class GetUsers(replyTo: ActorRef[Users]) extends Command

  final case class CreateUser(user: User, replyTo: ActorRef[ActionPerformed]) extends Command

  final case class GetUser(name: String, replyTo: ActorRef[GetUserResponse]) extends Command

  final case class DeleteUser(name: String, replyTo: ActorRef[ActionPerformed]) extends Command

  final case class GetUserResponse(maybeUser: Option[User])

  def apply(): Behavior[Command] = registry(Set.empty)

  private def registry(users: Set[User]): Behavior[Command] =
    Behaviors.receiveMessage {
      case GetUsers(replyTo) =>
        replyTo ! Users(users.toSeq)
        Behaviors.same
      case CreateUser(user, replyTo) =>
        replyTo ! ActionPerformed(s"User ${user.id} created.")
        registry(users + user)
      case GetUser(id, replyTo) =>
        replyTo ! GetUserResponse(users.find(_.id == id))
        Behaviors.same
      case DeleteUser(id, replyTo) =>
        replyTo ! ActionPerformed(s"User $id deleted.")
        registry(users.filterNot(_.id == id))
    }
}
