package dev.kaden.entities

object Domain {
  type BehaviorName = String

  enum BehaviorComponent:
    case Await(trigger: String)
    case Request(msg: String)
    case BlockUntil(blocked: String, trigger: String)

  case class Event(msg: String)
}
