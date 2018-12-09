package com.brightercode.antidominationbot.model

import com.brightercode.antidominationbot.Domination
import com.brightercode.discourse.model.{Category, Post}

class DominationWarningPost(domination: Domination, category: Category) extends
  Post(
    domination.topic.id,
    raw = s"@${domination.topic.author.username}, it has not escaped my attention that you created " +
      s"${domination.authorTopicCount} of the last ${domination.topicCount} topics in #${category.slug}.\n\n" +
      s"Please give others an opportunity to set the agenda."
  )
