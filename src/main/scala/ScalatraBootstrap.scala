import akka.actor._
import akka.util.Switch
import org.scalatra._
import javax.servlet.ServletContext
import elevator._
import elevator.web._

class ScalatraBootstrap extends LifeCycle {
  var system: ActorSystem = _
  val systemSwitch = new Switch 
  var state: ActorRef = _
  override def init(context: ServletContext) {
    systemSwitch switchOn {
      system = context.getOrElseUpdate("elevator", ActorSystem("elevator")).asInstanceOf[ActorSystem]
    }

    state = system.actorOf(Props[WorldStateActor])
    system.eventStream.subscribe(state,classOf[WorldStateMessages])
    context.mount(new ElevatorServlet(system), "/*")

    system.actorOf(Props[ElevatorSimulation])
  }

}
