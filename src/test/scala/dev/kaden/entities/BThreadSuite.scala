package dev.kaden.entities

import cats.effect.IO
import weaver._
import dev.kaden.entities.Domain.BehaviorComponent.*
import dev.kaden.entities.Behavior.Decision

object BThreadSuite extends SimpleIOSuite {

  // Easy to test stuff with no side effects...
  pureTest("Collapse to single answer") {
    val components = List(
      Request("A"),
      Request("B"),
      BlockUntil("A", "C")
    )
    val reduced = Decision.reduce(components)

    val expected = List("B")

    expect(reduced.requests.sameElements(expected))
  }

  pureTest("Collapse to multi-answer") {
    val components = List(
      Request("A"),
      Request("B"),
      Request("C"),
      BlockUntil("A", "C")
    )
    val reduced = Decision.reduce(components)

    val expected = List("B", "C")

    expect(reduced.requests.sameElements(expected))
  }
}
