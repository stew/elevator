package elevator

import akka.Main
import akka.actor._
import akka.util.Timeout
import scala.concurrent.duration._

object ElevatorSimulation extends App {
  Main.main(Array("elevator.ElevatorSimulation"))
}

class ElevatorSimulation extends Actor {
  val simulation = SimulationParameters(context.system)
  var elevators: Set[ActorRef] = Set.empty

  implicit val executor = context.system.dispatcher

  override def preStart = {
    // Schedule Ticks
    context.system.scheduler.schedule(
      1 second,  // start simulation in one second
      simulation.tickLength, // how long between ticks
      self,
      Tick)

    // Schedule shutdown
    context.system.scheduler.scheduleOnce(
      simulation.simulationLength,
      self,
      PoisonPill)

    elevators = {
      for(i ← 0 until simulation.numElevators.value) yield {
        val ref = context.system.actorOf(Props(classOf[Elevator], i), "elevator" + i)
        context.system.eventStream.subscribe(ref,classOf[SystemMessages])
        simulation.building ! ElevatorBecameIdle(ref, simulation.groundFloor)
        ref
      }
    }.to[Set]
                                                    
    
  }

  def receive: Receive = {
    case Tick =>
      passengersArrive
      context.system.eventStream.publish(Tick)
  }

  /**
    * random chance that person arrives at a random floor
    */
  def passengersArrive {
    val dice = scala.util.Random.nextInt(100)
    if(dice < simulation.percentageChanceOrArrival) {
      val fromFloor = scala.util.Random.nextInt(simulation.numFloors.value)
      val toFloor = scala.util.Random.nextInt(simulation.numFloors.value)
      if(fromFloor != toFloor)
        simulation.building ! PersonWaiting(Floor(fromFloor), Person(Floor(toFloor)))
      }
  }
}
