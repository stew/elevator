package elevator

import akka.actor.ActorRef

sealed trait Direction
case object Up extends Direction
case object Down extends Direction

case class Floor(value: Int) extends AnyVal {
  def desiredDirection(fromFloor: Floor) = if(value - fromFloor.value > 0)  Down else Up
  def next(direction: Direction) = direction match {
    case Up ⇒ Floor(value + 1)
    case Down ⇒ Floor(value - 1)
  }
  override def toString = value.toString
}

case class Car(value: Int) extends AnyVal {
  override def toString = value.toString
}
