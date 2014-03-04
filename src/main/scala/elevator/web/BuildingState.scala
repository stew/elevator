package elevator.web

import akka.actor._
import elevator._
import org.json4s.jackson.Serialization
import org.scalatra.ScalatraBase
import org.scalatra.SessionSupport
import org.scalatra.atmosphere.AtmosphereClient
import org.scalatra.atmosphere.AtmosphereSupport
import org.scalatra.atmosphere._
import org.scalatra.json.JsonSupport
import org.scalatra.json.{JValueResult, JacksonJsonSupport}
import org.json4s._
import JsonDSL._
import org.scalatra.scalate.ScalateSupport

case class ElevatorState(currentFloor: Int, up: Boolean, passengers: List[Int], idle: Boolean)
case class WorldState(waiting: Array[List[Int]], elevators: Array[ElevatorState])

sealed trait WorldStateMessages
case class RefreshClient(ref: ActorRef) extends WorldStateMessages
case class NewWaiting(floor: Int, waiters: List[Int]) extends WorldStateMessages
case class NewElevator(car: Int, state: ElevatorState) extends WorldStateMessages

class WorldStateActor extends Actor {
  protected implicit val jsonFormats: Formats = DefaultFormats
  implicit val executor = context.system.dispatcher
  val simulation = SimulationParameters(context.system)
  var worldState: WorldState = initWorldState

  def emptyFloors: Array[List[Int]] = (0 until simulation.numFloors.value).map(_ ⇒ List.empty[Int]).to[Array]
  def emptyElevators: Array[ElevatorState] = (0 until simulation.numElevators.value).map(_ ⇒ ElevatorState(simulation.groundFloor.value, false, Nil, true)).to[Array]

  def initWorldState: WorldState = WorldState(emptyFloors, emptyElevators)

  def receive: Receive = {
    case RefreshClient(ref) ⇒ ref ! worldState
    case NewWaiting(floor, waiters) ⇒ worldState.waiting(floor) = waiters
      if(simulation.servlet != null)
        simulation.servlet.publish(worldState)
    case NewElevator(car, state) ⇒ worldState.elevators(car) = state
      if(simulation.servlet != null)
        simulation.servlet.publish(worldState)
  }
}


