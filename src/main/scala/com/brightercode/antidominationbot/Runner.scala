package com.brightercode.antidominationbot

import com.brightercode.discourse.DiscourseForumApiClient
import com.brightercode.discourse.model.Topic.Created
import com.brightercode.discourse.model.{Post, Topic}
import com.brightercode.antidominationbot.util.ConfigHelper._
import com.brightercode.antidominationbot.util.LoopHelper
import com.typesafe.config.ConfigRenderOptions.concise
import com.typesafe.config.{ConfigFactory, ConfigRenderOptions}
import org.slf4j.LoggerFactory

import scala.concurrent.Await


object Runner extends App with LoopHelper {

  protected val logger = LoggerFactory.getLogger(getClass)

  private val config = ConfigFactory.load()
  logger.info(s"Discourse API config: ${config.getConfig("api").root().render(concise())}")
  logger.info(s"Bot config: ${config.getConfig("bot").root().render(concise())}")

  private val forum = new DiscourseForumApiClient(config.url, config.key, config.username)
  private val detector = new DominationDetector(config.minTopics, config.dominationThreshold)
  private val category =
    Await.result(forum.categories.list(), config.timeout)
      .find(_.name == config.category)
      .getOrElse(sys.error(s"Category ${config.category} not found"))


  loop(config.loop, onException = e => logger.error(e.getMessage, e)) {
    detector.onDominationIn(relevantTopics()) { domination =>
      if (previouslyPostedOn(domination.topic)) {
        logger.debug("Already posted warning")
      } else {
        logger.info("Bookmarking topic and creating warning post")
        markPostedOn(domination.topic)
        forum.posts.create(dominationWarningPost(domination))
      }
    }
  }


  private def previouslyPostedOn(topic: Topic) =
    topic.topicPostBookmarked

  private def markPostedOn(topic: Topic) =
    forum.topics.bookmark(topic.id)

  private def relevantTopics(): Seq[Topic] =
    Await.result(forum.topics.list(category.slug, order = Some(Created)), config.timeout)
      .filterNot(_.pinned) // Ignore pinned topics (e.g. the "about this category" topic)

  private def dominationWarningPost(domination: Domination) =
    Post(
      domination.topic.id,
      raw = s":robot: It has not escaped my attention that you created " +
        s"${domination.authorTopicCount} of the last ${domination.topicCount} topics in #${category.slug}.\n\n" +
        s"Please give others an opportunity to set the agenda."
    )
}