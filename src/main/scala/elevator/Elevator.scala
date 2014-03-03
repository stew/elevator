package elevator

import akka.actor._

sealed trait ElevatorCommand
case class GoToFloor(floor: Floor)
case class PickUp(passengers: Set[Person])

sealed trait ElevatorState {
  def floor: Floor

  // Estimate how long it would take you to travel to a given floor
  def timeToFloor(simulation: SimulationParameters, desired: Floor): Int
}

/** this elevator currently has nothing to do
  * 
  */
case class ElevatorIdle(floor: Floor) extends ElevatorState {
  def timeToFloor(simulation: SimulationParameters, desired: Floor): Int = {
    Math.abs(floor.value - desired.value) * simulation.numTicksBetweenFloors
  }
}
case class AwaitingInstruction(floor: Floor) extends ElevatorState {
  def timeToFloor(simulation: SimulationParameters, desired: Floor): Int = {
    Math.abs(floor.value - desired.value) * simulation.numTicksBetweenFloors
  }
}

/**
  * This elevator is exchanging passengers at a given floor, after which it will continue in the given direction
  */

case class ElevatorExchange(floor: Floor, destinationFloor: Floor, direction: Direction, passengers: Set[Person], ticksInState: Int) extends ElevatorState {
  def timeToFloor(simulation: SimulationParameters, desired: Floor): Int = {
    // TODO we don't yet take in account dropping off our current passengers
    (simulation.numTicksToExchangePassengers - ticksInState) + (
      if(direction == floor.desiredDirection(desired))
      (Math.abs(floor.value - desired.value) * simulation.numTicksBetweenFloors)
    else // TODO we assume they have to hit the top floor
      (((simulation.numFloors.value - floor.value) * simulation.numTicksBetweenFloors) +
         (simulation.numFloors.value - desired.value) * simulation.numTicksBetweenFloors))
  }
}

/**
  * This elevator is travelling to another floor
  */
case class ElevatorTravelling(floor: Floor, destinationFloor: Floor, direction: Direction, passengers: Set[Person], ticksInState: Int) extends ElevatorState {
  // TODO DRY, this is the same as ElevatorExchange
  // TODO we don't yet take in account dropping off our current passengers
  def timeToFloor(simulation: SimulationParameters, desired: Floor): Int = {
    // TODO we don't yet take in account dropping off our current passengers
    (simulation.numTicksToExchangePassengers - ticksInState) + (
      if(direction == floor.desiredDirection(desired))
      (Math.abs(floor.value - desired.value) * simulation.numTicksBetweenFloors)
    else // TODO we assume they have to hit the top floor
      (((simulation.numFloors.value - floor.value) * simulation.numTicksBetweenFloors) +
         (simulation.numFloors.value - desired.value) * simulation.numTicksBetweenFloors))
  }

}

class Elevator(car: Car) extends Actor {
  val simulation = SimulationParameters(context.system)

  println("elevator: " + car.value + " on floor " + simulation.groundFloor)
  var currentState: ElevatorState = ElevatorIdle(simulation.groundFloor)

  def receive: Receive = idle

  def idle: Receive = {
    case GoToFloor(floor) ⇒
      if(floor == currentState.floor) {
        println(s"elevator ${car} already on floor ${floor}")
        simulation.building ! ElevatorArrived(self, currentState.floor)
      } else {
        println(s"elevator ${car.value} starts travelling to ${floor.value}")
        currentState = ElevatorTravelling(currentState.floor, floor, currentState.floor.desiredDirection(floor), Set.empty[Person],0)
      }
      context.become(notIdle)
    case Tick ⇒ //Do Nothing

  }
  def notIdle: Receive = {
    case Tick ⇒
      currentState match {
        case ElevatorExchange(floor, destFloor, direction, passengers, ticksInState) if(ticksInState >= simulation.numTicksToExchangePassengers) ⇒
          if(passengers.isEmpty && floor==destFloor) {
            becomeIdle(floor)
          } else {
            currentState = ElevatorTravelling(floor, destFloor, direction, passengers, 0)
          }
        case ElevatorExchange(floor, destFloor, direction, passengers, ticksInState) ⇒
          currentState = ElevatorExchange(floor, destFloor, direction, passengers, ticksInState +1)
        case ElevatorTravelling(floor, destFloor, direction, passengers, ticksInStaate) if(ticksInStaate >= simulation.numTicksBetweenFloors) ⇒
          val newFloor = floor.next(direction)
          dropPassengers(ElevatorTravelling(newFloor, destFloor, direction, passengers, 0))

        case ElevatorTravelling(floor, destFloor, direction, passengers, ticksInStaate) ⇒
          currentState = ElevatorTravelling(floor, destFloor, direction, passengers, ticksInStaate + 1)
        case AwaitingInstruction(floor) ⇒
          becomeIdle(floor)
      }

    case PickUp(passengers) ⇒
      currentState match {
        case ElevatorTravelling(floor, destFloor, direction, oldPassengers, ticksInStaate) ⇒
          println(s"building told me to stop on my way to $destFloor to pick up $passengers")
          val newPassengers = passengers ++ oldPassengers
          currentState = ElevatorExchange(floor, destFloor, direction, newPassengers, 0)
          println(s"elevator ${car.value} picked up ${passengers.size} passengers")

        case ElevatorExchange(floor, destFloor, direction, oldPassengers, ticksInState) ⇒
          println(s"elevator ${car} picks up ${passengers.size} passengers on floor ${floor}")
          currentState = ElevatorExchange(floor, newDest(direction, destFloor, passengers), direction, oldPassengers ++ passengers, 0)
          println(s"elevator ${car.value} picked up ${passengers.size} passengers")

        case ElevatorIdle(floor) ⇒
          floor.desiredDirection(passengers.head.desiredFloor) match { // YOLO head
            case Up ⇒
              currentState = ElevatorExchange(floor, newDest(Up, Floor(0), passengers), Up, passengers, 0)
            case Down ⇒
              currentState = ElevatorExchange(floor, newDest(Down, simulation.numFloors, passengers), Down, passengers, 0)
          }
          println(s"elevator ${car.value} picked up ${passengers.size} passengers")
          
        case AwaitingInstruction(floor) ⇒
          floor.desiredDirection(passengers.head.desiredFloor) match { // YOLO head
            case Up ⇒
              currentState = ElevatorExchange(floor, newDest(Up, Floor(0), passengers), Up, passengers, 0)
            case Down ⇒
              currentState = ElevatorExchange(floor, newDest(Down, simulation.numFloors, passengers), Down, passengers, 0)
          }
          println(s"elevator ${car.value} picked up ${passengers.size} passengers")
      }
  }

  def newDest(dir: Direction, oldDest: Floor, ps: Iterable[Person]) : Floor = {
    dir match {
      case Up ⇒
        Floor(ps.foldLeft(oldDest.value)((r,p) ⇒ Math.max(r,p.desiredFloor.value)))
      case Down ⇒
        Floor(ps.foldLeft(oldDest.value)((r,p) ⇒ Math.min(r,p.desiredFloor.value)))

    }
  }

  def becomeIdle(floor: Floor) {
    println(s"elevator ${car} becomes idle")
    currentState = ElevatorIdle(floor)
    simulation.building ! ElevatorBecameIdle(self, floor)
    context.become(idle)
  }

  def dropPassengers(state: ElevatorTravelling): Unit = {
    println(s"elevator ${car} arriving on floor ${state.floor}")
    val (getOff, stayOn) = state.passengers.partition(_.desiredFloor==state.floor)

    if(getOff.nonEmpty) {

      getOff foreach { person ⇒
        println(s"person: $person gets off elevator: ${car.value}")
      }
      currentState = ElevatorExchange(state.floor,state.destinationFloor,state.direction, stayOn, 0)
      simulation.building ! ElevatorStopping(self, state.floor, state.direction)
    } else {
      if(state.floor == state.destinationFloor) {
        simulation.building ! ElevatorArrived(self, state.floor)
        currentState = AwaitingInstruction(state.floor)
      } else currentState = state
    }
  }



  def elevatorTravelling: Receive = {
    case Tick ⇒ 
  }
}

