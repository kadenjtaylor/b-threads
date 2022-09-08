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

  val logger: SelfAwareStructuredLogger[IO] = LoggerFactory[IO].getLogger

  case class Program(
      bthreads: BThread*
  ) {
    import RunState.*
    def run(counter: Int): IO[Unit] = {
      val init = RunningProgram(bthreads, Frame())
      unfoldIO(init)(rpg => rpg.step()) {
        case Halt(n)                                               => Left(printHaltAt(n))
        case Go(RunningProgram(_, frame)) if frame.major > counter => Left(printHaltAt(frame))
        case Go(rpg)                                               => Right(rpg)
      }
    }

    private def printHaltAt(n: Frame): IO[Unit] = IO.println(s"Halted at frame: $n")
  }

  enum RunState:
    case Halt(frame: Frame)
    case Go(prg: RunningProgram)

  def advance(rpg: RunningProgram): RunState = {
    import RunState.*
    if (rpg.threads.isEmpty) {
      Halt(rpg.frame)
    } else {
      Go(RunningProgram(rpg.threads, rpg.frame.incMajor()))
    }
  }

  case class Frame(major: Int, minor: Int) {
    def incMajor(): Frame           = Frame(major + 1, 0)
    def incMinor(): Frame           = Frame(major, minor + 1)
    override def toString(): String = s"$major.$minor"
  }
  object Frame {
    def apply(): Frame = Frame(0, 0)
  }

  case class RunningProgram(threads: Seq[BThread], frame: Frame) {
    def step(): IO[RunState] = {
      val decision = Decision.reduce(threads.map(_.now))
      for {
        update <- decision.applyM(this)
        prog <- update match {
          case (events, prog) =>
            for {
              _ <- IO.println(s"${prog.frame}: [${events.map(_.msg).mkString(", ")}]")
            } yield prog
        }
      } yield advance(prog)
    }

    def update(req: String) = RunningProgram(threads.flatMap(_.react(req)), frame.incMinor())

    override def toString(): String = {
      val header  = s"\nFrame: $frame, Threads (${threads.size}):\n"
      val tString = threads.mkString("\n")
      header + tString
    }
  }

  case class Decision(requests: Seq[String]) {
    def apply(rpg: RunningProgram): (Seq[Event], RunningProgram) = {
      requests.foldLeft((Seq(), rpg))(mapAcc(updateProgram))
    }

    private def updateProgram(req: String, prog: RunningProgram): (Event, RunningProgram) = {
      (Event(req), prog.update(req))
    }

    def applyM(rpg: RunningProgram): IO[(Seq[Event], RunningProgram)] = {
      val a: IO[(Seq[Event], RunningProgram)] = IO.pure((Seq(), rpg))
      requests.foldLeft(a)(mapAccM(updateProgramM))
    }

    private def updateProgramM(req: String, prog: RunningProgram): IO[(Event, RunningProgram)] = {
      val (event, nextProg) = updateProgram(req, prog)
      logger.debug(s"Request: $event, Frame: ${nextProg.frame}") *>
        IO.pure((event, nextProg))
    }
  }

  object Decision {

    def reduce(components: Seq[BehaviorElement]): Decision = {
      val starter = DecisionBuilder(Seq(), Set())
      val builder = components.foldLeft(starter)(incorporate)
      builder.build()
    }

    private case class DecisionBuilder(reqs: Seq[String], blocking: Set[String]) {
      def build(): Decision = Decision(reqs)
    }

    private def incorporate(builder: DecisionBuilder, comp: BehaviorElement) = {
      import BehaviorElement.*
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
