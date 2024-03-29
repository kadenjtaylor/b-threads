package dev.kaden.entities

import cats.effect.IO
import cats.effect.kernel.Ref

object System {

  type ThreadName = String

  type Key = String

  enum BehaviorElement:
    case Request(keys: Set[Key])
    case BlockUntil(block: Set[Key], until: Set[Key] => Boolean)

  trait BThread {
    def name: ThreadName
    def peek: BehaviorElement
    def react(keys: Set[Key]): Option[BThread]
  }

  case class Event(key: Key)

  object BehaviorElement {
    // TODO: Helper functions
  }

  case class Frame(major: Int, minor: Int) {
    def incMajor(): Frame           = Frame(major + 1, 0)
    def incMinor(): Frame           = Frame(major, minor + 1)
    override def toString(): String = s"$major.$minor"
  }
  object Frame {
    def apply(): Frame = Frame(0, 0)
  }

  enum ProgramState:
    case Paused
    case Running

  case class Program(threads: Map[ThreadName, BThread], frame: Frame, state: ProgramState)

  trait Pullable[F[_], T] {
    def pull(): F[T]
  }

  trait Pushable[F[_], T] {
    def push(t: T): F[Unit]
  }

  trait PushPull[F[_], T] extends Pushable[F, T] with Pullable[F, T]

  case class Decision[F[_]]() {
    def apply(): IO[(Seq[Event], Program)]
  }
  object Decision {
    def create[F[_]](elements: BehaviorElement): F[Decision[F]] = ???
  }

  // Domain above
  // ---------------------------------------------------------------------
  // Impl Below

  import Program.*

  case class BThreadSystem(
      private val q: PushPull[IO, Seq[BehaviorElement]],
      private val o: Pushable[IO, Seq[Event]],
      private val progRef: Ref[IO, Program]
  ) extends Pushable[IO, Seq[BehaviorElement]] {


    // def run(): IO[Unit] = {
    //   // TODO: Do steps until the program halts (None) or pauses Some(Pause)
    // }

    private def doThing(p: Program, elements: Seq[BehaviorElement]) = {
      import ProgramState.*
      p.state match {
        case Paused => q.push(elements) *> run()
        case Running => q.push(elements)
      }
    }
    
    def push(t: Seq[BehaviorElement]): IO[Unit] = for {
      prog <- progRef.get
      _ <- IO.println(prog)
    } yield ()

    def externalStep(): IO[Unit] = for {
      inEvents  <- q.pull()
      outEvents <- internalStep(progRef, inEvents)
      _         <- o.push(outEvents)
    } yield ()

    private def internalStep(
        progRef: Ref[IO, Program],
        input: Seq[BehaviorElement]
    ): IO[Seq[Event]] = for {
      prog     <- progRef.get
      decision <- Decision.create(prog.threads.)
      // TODO: Compute stuff
      // TODO: Update ref
      // TODO: Return output events
    } yield Seq()
  }

}
