package controllers

import actors.{TweetLoader, Actors, UserActor}
import play.api.Play.current
import play.api.Play.configuration
import play.api.libs.json.JsValue
import play.api.mvc.{WebSocket, Action, Controller}

import scala.concurrent.Future
import play.api.libs.ws.WS
import play.api.libs.concurrent.Akka
import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import play.api.libs.json.JsObject

// Uses Play's standard execution context
import play.api.libs.concurrent.Execution.Implicits._

object Tweets extends Controller {

    def index = Action {
        Ok(views.html.index("TweetMap"))
    }

    implicit val sentimentAskTimeout: Timeout = Duration(10, SECONDS)

    def search(query: String) = Action.async {
        val tweetLoaderRef: ActorRef = Akka.system.actorOf(TweetLoader.props())
        tweetLoaderRef ! TweetLoader.LoadTweet(query)

        (tweetLoaderRef ? TweetLoader.LoadTweet(query)).mapTo[JsObject].map {
            tweetJson => {
                println(s"returning tweetJson: $tweetJson")
                Ok(tweetJson)
            }
        }
    }

    private val tweetUrl = configuration.getString("tweet.url").get

    def loadUrl(query: String): Future[JsValue] = {
        WS.url(tweetUrl)
            .withQueryString("q" -> query)
            .get()
            .map {
            resp => resp.json
        }
    }

    def tweetFeed = WebSocket.acceptWithActor[JsValue, JsValue] { request => out =>
        UserActor.props(out, Actors.tweetLoader)
    }
}
