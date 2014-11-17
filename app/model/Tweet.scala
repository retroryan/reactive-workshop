package model

import play.api.libs.json.Json

case class Tweet(text: String)

object Tweet {
    implicit val tweetReads = Json.reads[Tweet]
}