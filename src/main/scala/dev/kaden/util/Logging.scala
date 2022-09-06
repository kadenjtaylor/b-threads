package dev.kaden.util

import cats.effect.IO
import org.typelevel.log4cats.slf4j.loggerFactoryforSync
import org.typelevel.log4cats.slf4j.Slf4jFactory
import org.typelevel.log4cats.LoggerFactory

object Logging {

  implicit val logging: LoggerFactory[IO] = Slf4jFactory[IO]

}
