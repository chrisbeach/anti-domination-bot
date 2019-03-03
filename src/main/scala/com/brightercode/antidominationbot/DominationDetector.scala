package com.brightercode.antidominationbot

import com.brightercode.antidominationbot.util.BotConfig
import org.slf4j.LoggerFactory
import com.brightercode.discourse.model._

class DominationDetector(botConfig: BotConfig) {
  require(botConfig.minTopics > 0)

  private val logger = LoggerFactory.getLogger(getClass)

  /**
    * Checks if author of latest topic has also authored proportionally more than [domination_threshold] of [topics].
    * If so, and if the topic hasn't previously been bookmarked, create a warning post on the topic and bookmark the
    * topic.
    *
    * @param topics in descending date order
    */
  def onDominationIn[T](topics: Seq[Topic])
                       (onDomination: Domination => T): Option[T] = {
    logger.debug(s"Topics (${authorTopicCountSummary(topics)}):\n\t${topics.mkString("\n\t")}\n")
    if (topics.size >= botConfig.minTopics) {
      topics.headOption.flatMap { latestTopic =>
        val topicsByAuthor = topics.count(_.author.id == latestTopic.author.id)
        logger.debug(s"Latest topic: $latestTopic. Recent topics by same author: $topicsByAuthor / ${topics.size}")

        if (topicsByAuthor.toDouble / topics.size.toDouble >= botConfig.dominationThreshold) {
          val domination = Domination(latestTopic, topicsByAuthor, topics.size)
          logger.debug(s"Detected $domination")
          Some(onDomination(domination))
        } else {
          None
        }
      }
    } else {
      None
    }
  }

  private def authorTopicCountSummary(topics: Seq[Topic]): String =
    topics.map(_.author.username)
      .groupBy(identity)
      .map { case (username, instances) => (username, instances.size) }
      .toList.sortBy { case (_, count) => -count }
      .map { case (username, count) => s"$count x @$username" }
      .mkString(", ")
}

case class Domination(topic: Topic, authorTopicCount: Int, topicCount: Int) {
  override def toString: String = s"Domination(user=@${topic.author.username} topic='${topic.title}' $authorTopicCount/$topicCount topics by author)"
}