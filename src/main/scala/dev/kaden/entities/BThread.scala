package dev.kaden.entities

import dev.kaden.entities.Domain.*

object BThread {
  trait BThread {
    import BehaviorElement.*

    def name: BehaviorName
    def now: BehaviorElement
    def next: Option[BThread]
    def react(msg: String): Option[BThread] = now match {
      case Request(`msg`)       => next
      case Await(`msg`)         => next
      case BlockUntil(_, `msg`) => next
      case _                    => Some(this)
    }
  }

  case class SequenceThread(name: BehaviorName, index: Int, components: List[BehaviorElement])
      extends BThread {
    import BehaviorElement.*
    def now: BehaviorElement = components(index)
    def next: Option[BThread] =
      if index == components.size - 1 then None
      else Some(SequenceThread(name, index + 1, components))
  }
  object SequenceThread {
    def apply(name: BehaviorName, components: BehaviorElement*): SequenceThread = {
      SequenceThread(name, 0, components.toList)
    }
  }

  case class CircularThread(name: BehaviorName, index: Int, components: List[BehaviorElement])
      extends BThread {
    import BehaviorElement.*
    def now: BehaviorElement = components(index)
    def next: Option[BThread] = Some(
      CircularThread(name, (index + 1) % components.size, components)
    )
  }
  object CircularThread {
    def apply(name: BehaviorName, components: BehaviorElement*): CircularThread = {
      CircularThread(name, 0, components.toList)
    }
  }
}
