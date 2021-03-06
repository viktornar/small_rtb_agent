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
      val campaignActor = context.spawn(CampaignActor(), "CampaignRegistryActor")
      context.watch(campaignActor)
      val bidActor = context.spawn(BidActor(), "BidRegistryActor")
      context.watch(bidActor)

      val bidRoutes = new BidRoutes(bidActor, campaignActor)(context.system)

      startHttpServer(concat(bidRoutes.routes), context.system)

      Behaviors.empty
    }
    val system = ActorSystem[Nothing](rootBehavior, "SimpleRtbAgentHttpServer")
  }
}
