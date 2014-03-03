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

/**
  * This elevator is exchanging passengers at a given floor, after which it will continue in the given direction
  */

case class ElevatorExchange(direction: Direction, floor: Floor, passengers: Set[Person], ticksInState: Int) extends ElevatorState {
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
case class ElevatorTravelling(floor: Floor, direction: Direction, passengers: Set[Person], ticksInState: Int) extends ElevatorState {
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
      println(s"elevator ${car.value} starts travelling to ${floor.value}")
      if(floor == currentState.floor) {
        currentState = ElevatorExchange(Up, floor, Set.empty[Person], 0)
        simulation.building ! ElevatorBecameIdle(self, floor)
      } else {
        currentState = ElevatorTravelling(currentState.floor, currentState.floor.desiredDirection(floor), Set.empty[Person],0)
        context.become(notIdle)
      }
    case Tick ⇒ // do nothing
  }

  def notIdle: Receive = {
    case Tick ⇒
      currentState match {
      case ElevatorExchange(direction, floor, passengers, ticksInState) if(ticksInState == simulation.numTicksToExchangePassengers) ⇒
        if(passengers.isEmpty) {
          currentState = ElevatorIdle(floor)
          simulation.building ! ElevatorBecameIdle(self, floor)
        } else {
          currentState = ElevatorTravelling(floor, direction, passengers, 0)
        }
        case ElevatorExchange(direction, floor, passengers, ticksInState) ⇒
          currentState = ElevatorExchange(direction, floor, passengers, ticksInState +1)
      case ElevatorTravelling(floor, direction, passengers, ticksInStaate) if(ticksInStaate >= simulation.numTicksBetweenFloors) ⇒
          val newFloor = floor.next(direction)
          dropPassengers(ElevatorTravelling(newFloor, direction, passengers, 0))

        case ElevatorTravelling(floor, direction, passengers, ticksInStaate) ⇒
          currentState = ElevatorTravelling(floor, direction, passengers, ticksInStaate + 1)
    }

    case PickUp(passengers) ⇒
      currentState match {
        case ElevatorTravelling(floor, direction, oldPassengers, ticksInStaate) ⇒
          val newPassengers = passengers ++ oldPassengers
          newPassengers.find(p ⇒ floor.desiredDirection(p.desiredFloor) == direction) match {
            case Some(x) ⇒ // we keep travalling that direction
              currentState = ElevatorExchange(direction, floor, newPassengers, 0)
            case None ⇒ // we switch directions
              currentState = ElevatorExchange(if(direction == Up) Down else Up, floor, newPassengers, 0)
          }
          
          println(s"elevator ${car.value} picked up ${passengers.size} passengers)")
      }
  }

  def dropPassengers(state: ElevatorTravelling): Unit = {
    println(s"elevator ${car} arriving on floor ${state.floor}")
    val (getOff, stayOn) = state.passengers.partition(_.desiredFloor==state.floor)


    if(getOff.nonEmpty) {

      getOff foreach { person ⇒
        println(s"person: $person gets off elevator: ${car.value}")
      }
      currentState = ElevatorExchange(state.direction,state.floor.next(state.direction), stayOn, 0)
    } else {
      currentState = state
    }
    simulation.building ! ElevatorArrived(self, state.floor, state.direction)
  }



  def elevatorTravelling: Receive = {
    case Tick ⇒ 
  }
}

