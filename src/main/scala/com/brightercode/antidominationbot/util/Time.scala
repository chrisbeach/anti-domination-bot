package com.brightercode.antidominationbot.util

import scala.concurrent.duration.FiniteDuration
import scala.language.implicitConversions

object Time {
  implicit def asFiniteDuration(d: java.time.Duration): FiniteDuration =
    scala.concurrent.duration.Duration.fromNanos(d.toNanos)
}
