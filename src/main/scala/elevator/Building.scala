package elevator

import akka.actor.{Actor,ActorRef}

class Building extends Actor {
  val simulation = SimulationParameters(context.system)

  val waiters: collection.mutable.Map[Int, (Set[Person],Set[Person])] =
    collection.mutable.Map.empty[Int, (Set[Person],Set[Person])].withDefaultValue((Set.empty[Person], Set.empty[Person]))

  var elevators: List[ActorRef] = Nil
  var elevatorsNeeded: Boolean = false

  def receive: Receive = {
    case PersonWaiting(floor, person) ⇒
      println("person waiting on " + floor.value + " to go to " + person.desiredFloor.value)
      val old = waiters(floor.value)
      var queue = false
      person.desiredFloor.desiredDirection(floor) match {
        case Up ⇒
          if(old._1.isEmpty) queue = true
          waiters += (floor.value → (old._1 + person, old._2))
        case Down ⇒
          if(old._2.isEmpty) queue = true
          waiters += (floor.value → (old._1, old._2 + person))
      }
      if(queue) queueAnElevator(floor)

    case ElevatorBecameIdle(ref,_) ⇒
      if(elevatorsNeeded)
        doSomething(ref)
      else
        elevators = ref :: elevators

    case ElevatorArrived(ref, floor, direction) ⇒ direction match {
      case Up ⇒ if(waiters(floor.value)._1.nonEmpty) {
        ref ! PickUp(waiters(floor.value)._1)
        waiters(floor.value) = Set.empty[Person] → waiters(floor.value)._2
      }
      case Down ⇒ if(waiters(floor.value)._2.nonEmpty) {
        ref ! PickUp(waiters(floor.value)._2)
        waiters(floor.value) = waiters(floor.value)._1 → Set.empty[Person]
      }
    }
  }

  def doSomething(ref: ActorRef) {
    elevatorsNeeded = false
    waiters.foreach { wait ⇒
      if(wait._2._1.nonEmpty) ref ! GoToFloor(Floor(wait._1))
      if(wait._2._2.nonEmpty) ref ! GoToFloor(Floor(wait._1))
    }

  }

  def queueAnElevator(floor: Floor) {
    elevators match {
      case Nil ⇒ elevatorsNeeded = true
      case a::as ⇒
        // TODO not smart
        a ! GoToFloor(floor)
        elevators = as
    }
  }
}

sealed trait BuildingMessages
case class PersonWaiting(floor: Floor, p: Person)
case class ElevatorArrived(elevator: ActorRef, floor: Floor, direction: Direction)
case class ElevatorBecameIdle(elevator: ActorRef, floor: Floor)

case class BuildingState(elevators: IndexedSeq[ElevatorState], waiters: Map[Int, Set[Person]]) {

}

