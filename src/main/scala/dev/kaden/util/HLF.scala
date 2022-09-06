package dev.kaden.util

import cats.effect.IO

object HLF {
  def unfoldIO[A, B](init: A)(forth: A => IO[B])(back: B => Either[IO[Unit], A]): IO[Unit] = {
    for {
      b <- forth(init)
      nextStep <- back(b) match {
        case Left(ioUnit) => ioUnit
        case Right(a)     => unfoldIO(a)(forth)(back)
      }
    } yield nextStep
  }
}
