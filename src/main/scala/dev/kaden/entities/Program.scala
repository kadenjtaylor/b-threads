package dev.kaden.entities

// Logging imports
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.LoggerFactory
import dev.kaden.util.Logging.logging

import cats.effect.IO
import cats.Monad
import dev.kaden.entities.BThread.BThread
import dev.kaden.entities.Domain.*
import dev.kaden.util.HLF.*

object Behavior {

  case class Program(bthreads: BThread*) {
    import RunState.*
    def run(counter: Int): IO[Unit] = {
      val init = RunningProgram(bthreads, 0)
      unfoldIO(init)(rpg => rpg.step()) {
        case Halt(n)                                         => Left(printHaltAt(n))
        case Go(RunningProgram(_, frame)) if frame > counter => Left(printHaltAt(frame))
        case Go(rpg)                                         => Right(rpg)
      }
    }

    private def printHaltAt(n: Int): IO[Unit] = IO.println(s"Halted at frame: $n")
  }

  enum RunState:
    case Halt(n: Int)
    case Go(prg: RunningProgram)

  case class RunningProgram(threads: Seq[BThread], frame: Int) {
    def step(): IO[RunState] = {
      val decision = Decision.reduce(threads.map(_.now))
      for {
        prog <- decision(this)
      } yield prog
    }

    override def toString(): String = {
      val header  = s"\nFrame: $frame, Threads (${threads.size}):\n"
      val tString = threads.mkString("\n")
      header + tString
    }
  }

  case class Decision(requests: Seq[String]) {
    import RunState.*
    def apply(rpg: RunningProgram): IO[RunState] = {
      if (requests.isEmpty) {
        IO.pure(Halt(rpg.frame))
      } else {
        val starter: (Seq[Event], RunningProgram) = (Seq(), rpg)
        val stuff = requests
          .foldLeft(starter) {
            case ((events, rpg), req) => {
              updateProgram(rpg, req) match {
                case (e, newProg) => (events :+ e, newProg)
              }
            }
          }
        stuff match {
          case (events, RunningProgram(threads, frame)) =>
            val retVal = if (threads.isEmpty) {
              Halt(frame)
            } else {
              Go(RunningProgram(threads, frame + 1))
            }
            IO.println(s"$frame: $events") *> IO.pure(retVal)
        }
      }
    }

    private def updateProgram(prog: RunningProgram, req: String): (Event, RunningProgram) = {
      (Event(req), RunningProgram(prog.threads.flatMap(_.react(req)), prog.frame))
    }
  }

  object Decision {

    def reduce(components: Seq[BehaviorComponent]): Decision = {
      val starter = DecisionBuilder(Seq(), Set())
      val builder = components.foldLeft(starter)(incorporate)
      builder.build()
    }

    private case class DecisionBuilder(reqs: Seq[String], blocking: Set[String]) {
      def build(): Decision = Decision(reqs)
    }

    private def incorporate(builder: DecisionBuilder, comp: BehaviorComponent) = {
      import BehaviorComponent.*
      comp match {
        case Request(msg) if !builder.blocking.contains(msg) => {
          DecisionBuilder(builder.reqs :+ msg, builder.blocking)
        }
        case BlockUntil(blocked, _) => {
          DecisionBuilder(builder.reqs.filter(_ != blocked), builder.blocking + blocked)
        }
        case _ => {
          builder
        }
      }
    }

  }

}
