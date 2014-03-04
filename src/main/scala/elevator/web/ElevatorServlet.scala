package elevator
package web

import akka.actor.ActorSystem
import org.json4s.jackson.Serialization
import org.scalatra._
import scalate.ScalateSupport
import org.scalatra.atmosphere._
import org.scalatra.json.{JValueResult, JacksonJsonSupport}
import org.json4s._
import JsonDSL._

class ElevatorServlet(system: ActorSystem) extends ElevatorWebStack {
  implicit val executor = system.dispatcher
  val simulation = SimulationParameters(system)
  simulation.servlet = this

  get("/") {
    contentType="text/html"
    ssp("/index.ssp")
  }

  atmosphere("/elevator") {
    new AtmosphereClient {
      def receive = {
        case Connected =>
        case Disconnected(disconnector, Some(error)) =>
        case Error(Some(error)) =>
        case TextMessage(text) =>
        case JsonMessage(json) =>
      }
    }
  }

  def publish(state: WorldState) {
    AtmosphereClient.broadcast("/elevator", Serialization.write(state))
  }
}
