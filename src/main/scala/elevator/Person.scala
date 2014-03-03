package elevator

import akka.actor._

case class Person(desiredFloor: Floor)


/*
sealed trait MessagesToPerson
case class ElevatorPickUp(floor: Floor)
case class ElevatorDropOff(floor: Floor)

case class WaitingPerson(onFloor: Floor, desiredFloor: Floor, person: ActorRef)

/**
   * A simulated person that rides the elevators.  They wait for an el
   */
class Person(initialDesiredFloor: Floor) extends Actor {
  import context._
  val simulation = SimulationParameters(context.system)

  var desiredFloor = initialDesiredFloor
  var currentFloor = simulation.groundFloor

  override def preStart(): Unit = {
    become(waitingForElevator)
  }
  
  def receive = waitingForElevator

  def waitingForElevator: Receive = {
    case ElevatorPickUp(floor: Floor) ⇒ become(ridingToDestination)
  }

  def ridingToDestination: Receive = {
    case ElevatorDropOff(floor) ⇒ 
      assert(floor == desiredFloor)
  }

  def ridingToExit: Receive = {
    case ElevatorDropOff(floor) ⇒ 
      self ! PoisonPill // this person is done riding the elevators
  }
}
 */
