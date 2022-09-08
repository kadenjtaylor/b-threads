package dev.kaden.util

import cats.effect.IO
import cats.Monad

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

  def mapAcc[A, B, C](fn: (C, B) => (A, B))(t: (Seq[A], B), c: C): (Seq[A], B) = {
    val res = fn(c, t._2)
    (t._1 :+ res._1, res._2)
  }

  def mapAccM[F[_]: Monad, A, B, C](
      fn: (C, B) => F[(A, B)]
  )(t: F[(Seq[A], B)], c: C): F[(Seq[A], B)] = {
    import cats.Monad.ops.toAllMonadOps
    for {
      tup <- t
      res <- fn(c, tup._2)
    } yield ((tup._1 :+ res._1, res._2))
  }

}
