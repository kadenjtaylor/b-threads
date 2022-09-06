package dev.kaden

// Logging imports
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.LoggerFactory
import dev.kaden.util.Logging.logging

import cats.effect.IOApp
import cats.effect.IO
import entities.Domain.*
import cats.effect.ExitCode
import dev.kaden.entities.BThread.SequenceThread
import dev.kaden.entities.BThread.CircularThread
import dev.kaden.entities.Behavior.Program

object Main extends IOApp {

  val logger: SelfAwareStructuredLogger[IO] = LoggerFactory[IO].getLogger

  override def run(args: List[String]): IO[ExitCode] = {

    import BehaviorComponent.*

    val waterLow = "water_low"
    val addHot   = "add_hot_water"
    val addCold  = "add_cold_water"

    val startWithWaterLow = SequenceThread("Low Water Alert", Request(waterLow))
    val addHotWhenLow = CircularThread(
      "Add Hot When Low",
      Await(waterLow),
      Request(addHot),
      Request(addHot),
      Request(addHot)
    )
    val addColdWhenLow =
      CircularThread(
        "Add Cold When Low",
        Await(waterLow),
        Request(addCold),
        Request(addCold),
        Request(addCold)
      )
    val alternateHotCold =
      CircularThread("Alternate Hot/Cold", BlockUntil(addHot, addCold), BlockUntil(addCold, addHot))

    // Comment out "alternateHotCold" to see what happens
    val prog = Program(startWithWaterLow, addColdWhenLow, addHotWhenLow, alternateHotCold)

    prog.run(40) *> IO.pure(ExitCode.Success)
  }
}
