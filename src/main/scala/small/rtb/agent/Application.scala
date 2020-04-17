package small.rtb.agent

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.RouteConcatenation.concat

import scala.util.{Failure, Success}

object Application {
  private def startHttpServer(routes: Route, system: ActorSystem[_]): Unit = {
    implicit val classicSystem: akka.actor.ActorSystem = system.toClassic
    import system.executionContext

    val futureBinding = Http().bindAndHandle(routes, "localhost", 8080)

    futureBinding.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system.log.info("Server online at http://{}:{}/", address.getHostString, address.getPort)
      case Failure(ex) =>
        system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
        system.terminate()
    }
  }

  def main(args: Array[String]): Unit = {
    val rootBehavior = Behaviors.setup[Nothing] { context =>
      val userRegistryActor = context.spawn(UserRegistry(), "UserRegistryActor")
      val bidRegistryActor = context.spawn(BidRegistry(), "BidRegistryActor")
      context.watch(userRegistryActor)
      context.watch(bidRegistryActor)

      val userRoutes = new UserRoutes(userRegistryActor)(context.system)
      val bidRoutes = new BidRoutes(bidRegistryActor)(context.system)

      startHttpServer(concat(userRoutes.routes, bidRoutes.routes), context.system)

      Behaviors.empty
    }
    val system = ActorSystem[Nothing](rootBehavior, "SimpleRtbAgentHttpServer")
  }
}
