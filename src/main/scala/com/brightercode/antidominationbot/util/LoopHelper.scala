package com.brightercode.antidominationbot.util

import java.lang.Thread.sleep

import scala.concurrent.duration.FiniteDuration

trait LoopHelper {
  def loop(config: LoopConfig,
           onException: Exception => Any)
          (operation: => Any): Unit = {

    var errorBackoff: FiniteDuration = config.initialErrorBackoffInterval

    while (true) {
      try {
        operation
        errorBackoff = config.initialErrorBackoffInterval
      } catch {
        case e: Exception =>
          onException(e)
          sleep(errorBackoff.toMillis)
          errorBackoff = errorBackoff * config.errorBackoffFactor
      }
      sleep(config.pollInterval.toMillis)
    }
  }
}

case class LoopConfig(pollInterval: FiniteDuration,
                      initialErrorBackoffInterval: FiniteDuration,
                      errorBackoffFactor: Int = 2)
