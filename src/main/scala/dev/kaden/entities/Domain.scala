package dev.kaden.entities

object Domain {
  type BehaviorName = String

  type Key = String

  // TODO: Enhance the language for specifying B-Threads

  enum BehaviorElement:
    case Await(trigger: Key)
    case Request(msg: Key)
    case BlockUntil(blocked: Key, trigger: Key)

  // IF We were using functions from Key => Boolean instead of the key itself,
  // We could do fun stuff like matching AnyOf("a", "b", "c") vs AllOf("a", "b", "c")

  // And triggers were for sets of Keys instead of a single Key,
  // Then any version of Await can be rewritten as a special case of BlockUntil
  // with a trigger of Set.empty

  case class Event(msg: String)
}
