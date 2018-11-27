package com.brightercode.antidominationbot

import com.brightercode.antidominationbot.util.ConfigHelper._
import com.brightercode.antidominationbot.util.LoopHelper
import com.brightercode.discourse.DiscourseForumApiClient
import com.brightercode.discourse.DiscourseForumApiClient.withForum
import com.brightercode.discourse.model.Topic.Created
import com.brightercode.discourse.model.{Category, Post, Topic}
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

import scala.concurrent.Await


object Runner extends App with LoopHelper {

  protected val logger = LoggerFactory.getLogger(getClass)

  private val config = ConfigFactory.load()
  logger.info(config.pretty)

  private val detector = new DominationDetector(config.minTopics, config.dominationThreshold)


  withForum(config.url, config.key, config.username) { implicit forum =>

    val category =
      Await.result(forum.categories.list(), config.timeout)
        .find(_.name == config.category)
        .getOrElse(sys.error(s"Category ${config.category} not found"))

    loop(config.loop, onException = e => logger.error(e.getMessage, e)) {
      detector.onDominationIn(relevantTopics(forum, category)) { domination =>
        if (previouslyPostedOn(domination.topic)) {
          logger.debug("Already posted warning")
        } else {
          logger.info("Bookmarking topic and creating warning post")
          markPostedOn(forum, domination.topic)
          forum.posts.create(dominationWarningPost(domination, category))
        }
      }
    }
  }

  def dominationWarningPost(domination: Domination, category: Category) =
    Post(
      domination.topic.id,
      raw = s":robot: It has not escaped my attention that you created " +
        s"${domination.authorTopicCount} of the last ${domination.topicCount} topics in #${category.slug}.\n\n" +
        s"Please give others an opportunity to set the agenda."
    )

  def relevantTopics(forum: DiscourseForumApiClient, category: Category): Seq[Topic] =
    Await.result(forum.topics.list(category.slug, order = Some(Created)), config.timeout)
      .filterNot(_.pinned) // Ignore pinned topics (e.g. the "about this category" topic)

  def markPostedOn(forum: DiscourseForumApiClient, topic: Topic) =
    forum.topics.bookmark(topic.id)

  private def previouslyPostedOn(topic: Topic) = topic.topicPostBookmarked
}