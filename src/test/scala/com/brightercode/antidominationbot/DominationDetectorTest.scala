package com.brightercode.antidominationbot

import com.brightercode.antidominationbot.util.BotConfig
import com.brightercode.discourse.model.{SimpleUser, Topic, User}
import org.scalatest.{Matchers, Succeeded, WordSpec}

class DominationDetectorTest extends WordSpec with Matchers {
  private val minTopics = 10
  private val detector = new DominationDetector(BotConfig("someCategory", minTopics, 0.5))

  def userWithId(id: Int) = SimpleUser(id, id.toString, Some(id.toString), "")

  private def someTopics(count: Int, authorFunction: Int => User = userWithId) =
    (1 to count).map(i =>
      Topic(Some(i), "some topic", pinned = false,
        postCount = 1, replyCount = 1, views = 1, author = authorFunction(i), topicPostBookmarked = false)
    )

  "Domination detector" should {
    "do nothing if no topics supplied" in {
      detector.onDominationIn(Seq.empty) { _ =>
        fail("Unexpected detection of domination")
      }
    }

    "do nothing if fewer than minTopics supplied" in {
      detector.onDominationIn(someTopics(minTopics - 1)) { _ =>
        fail("Unexpected detection of domination")
      }
    }

    "do nothing if topics all by different authors" in {
      detector.onDominationIn(someTopics(minTopics - 1, authorFunction = userWithId)) { _ =>
        fail("Unexpected detection of domination")
      }
    }

    "detect domination if half of topics are by same author" in {
      detector.onDominationIn(someTopics(minTopics, authorFunction = id => userWithId(id % 2))) { domination =>
        domination.topicCount should be(minTopics)
        domination.authorTopicCount should be(minTopics / 2)
      } should be(Some(Succeeded))
    }
  }
}
