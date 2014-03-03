package elevator

import akka.actor._
import scala.concurrent.duration._

sealed trait SystemMessages
case object Tick extends SystemMessages

class SimulationParameters(val system: ExtendedActorSystem) extends Extension {
  val tickLength: FiniteDuration = 200 millisecond
  val numTicksBetweenFloors: Int = 3
  val numTicksToExchangePassengers: Int = 5
  val numFloors: Floor = Floor(10)
  val numElevators: Car = Car(3)
  val groundFloor = Floor(1)

  val percentageChanceOrArrival: Int = 3

  val simulationLength: FiniteDuration = 100 seconds

  lazy val building: ActorRef = system.actorOf(Props[Building])
}

object SimulationParameters extends ExtensionId[SimulationParameters] with ExtensionIdProvider {
  def lookup() = SimulationParameters

  def createExtension(system: ExtendedActorSystem) = new SimulationParameters(system)
}
