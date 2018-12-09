package com.brightercode.antidominationbot.util

import com.brightercode.antidominationbot.util.Time._
import com.brightercode.discourse.DiscourseEndpointConfig
import com.typesafe.config.{Config, ConfigRenderOptions}

object ConfigHelper {
  implicit class ConfigDecorator(config: Config) {

    require(config.hasPath("discourse"), "application.conf (see README.md)")

    val discourseEndpoint = DiscourseEndpointConfig(
      baseUrl = config.getString("discourse.url"),
      username = config.getString("discourse.username"),
      key = config.getString("discourse.key"),
      timeout = config.getDuration("discourse.timeout")
    )

    val bot = BotConfig(
      category = config.getString("bot.category"),
      minTopics = config.getInt("bot.min_topics"),
      dominationThreshold = config.getDouble("bot.domination_threshold")
    )

    val loop: LoopConfig = LoopConfig(
      pollInterval = config.getDuration("bot.poll_interval"),
      initialErrorBackoffInterval = config.getDuration("bot.initial_error_backoff_interval")
    )

    private val configFormat =
      ConfigRenderOptions.defaults().setComments(false).setOriginComments(false)

    lazy val pretty: String =
      "Configuration:\n" +
        config.getConfig("discourse").root().withoutKey("key").render(configFormat) +
        config.getConfig("bot").root().render(configFormat)
  }
}

case class BotConfig(category: String, minTopics: Int, dominationThreshold: Double) {
  require(minTopics > 0, "Min topics must be greater than zero")
  require(category != null && category.trim.nonEmpty, "Category required")
}