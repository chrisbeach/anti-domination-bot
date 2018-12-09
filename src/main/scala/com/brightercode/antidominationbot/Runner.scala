package com.brightercode.antidominationbot

import com.brightercode.antidominationbot.model.DominationWarningPost
import com.brightercode.antidominationbot.util.ConfigHelper._
import com.brightercode.antidominationbot.util.LoopHelper
import com.brightercode.discourse.DiscourseForumApiClient
import com.brightercode.discourse.DiscourseForumApiClient.withForum
import com.brightercode.discourse.model.Topic.Created
import com.brightercode.discourse.model.{Category, Topic}
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

import scala.concurrent.Await


object Runner extends App with LoopHelper {

  private val logger = LoggerFactory.getLogger(getClass)

  private val config = ConfigFactory.load()
  logger.info(config.pretty)

  private val detector = new DominationDetector(config.bot)


  withForum(config.discourseEndpoint) { forum =>

    val category =
      Await.result(forum.categories.list(), config.discourseEndpoint.timeout)
        .find(_.name == config.bot.category)
        .getOrElse(sys.error(s"Category ${config.bot.category} not found"))

    resilientLoop(config.loop, onException = e => logger.error(e.getMessage, e)) {
      val topics = relevantTopics(forum, category)
      detector.onDominationIn(topics)(postWarningIfNotAlready)
    }

    def postWarningIfNotAlready(domination: Domination) =
      if (previouslyPostedOn(domination.topic)) {
        logger.debug("Already posted warning")
      } else {
        logger.info("Bookmarking topic and creating warning post")
        markPostedOn(domination.topic)
        forum.posts.create(new DominationWarningPost(domination, category))
      }

    def markPostedOn(topic: Topic) = forum.topics.bookmark(topic.id)
    def previouslyPostedOn(topic: Topic) = topic.topicPostBookmarked

    def relevantTopics(forum: DiscourseForumApiClient, category: Category): Seq[Topic] =
      Await.result(forum.topics.list(category.slug, order = Some(Created)), config.discourseEndpoint.timeout)
        .filterNot(_.pinned) // Ignore pinned topics (e.g. the "about this category" topic)
  }
}