package com.brightercode.antidominationbot.util

import com.brightercode.antidominationbot.util.Time._
import com.typesafe.config.{Config, ConfigRenderOptions}

import scala.concurrent.duration.FiniteDuration

object ConfigHelper {
  implicit class ConfigDecorator(config: Config) {
    val loop: LoopConfig = LoopConfig(
      pollInterval = config.getDuration("bot.poll_interval"),
      initialErrorBackoffInterval = config.getDuration("bot.initial_error_backoff_interval")
    )

    val category: String = config.getString("bot.category")
    require(category != null && category.trim.nonEmpty, "Category required")
    val minTopics: Int = config.getInt("bot.min_topics")
    val dominationThreshold: Int = config.getInt("bot.domination_threshold")

    val url: String = config.getString("api.url")
    require(url != "https://your.discourse.forum", "Please customise application.conf before running")
    val username: String = config.getString("api.username")
    val key: String = config.getString("api.key")
    val timeout: FiniteDuration = config.getDuration("api.timeout")

    private def configFormat =
      ConfigRenderOptions.defaults().setComments(false).setOriginComments(false)

    def pretty() = {
      config.getConfig("api").root().render(configFormat) +
        config.getConfig("bot").root().render(configFormat)
    }
  }
}
