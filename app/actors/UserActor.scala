package actors

import akka.actor.{ActorLogging, Props, Actor, ActorRef}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.JsValue

import scala.concurrent.duration._

class UserActor(out: ActorRef, tweetLoader: ActorRef) extends Actor with ActorLogging {

    var maybeQuery: Option[String] = None

    val tick = context.system.scheduler.schedule(Duration.Zero, 5.seconds, self, UserActor.FetchTweets)

    def receive = {

        case UserActor.FetchTweets =>
        //if there is a query available send a message to tweetLoader to fetch the tweets

        // handle new tweets that are sent back from tweet loader
        // case TweetLoader.NewTweet(tweetUpdate) =>
        // by sending a message to out the actor sends a message to the websocket and up to the client
        // out ! tweetUpdate

        case message: JsValue =>
            maybeQuery = (message \ "query").asOpt[String]

    }

    override def postStop() {
        tick.cancel()
    }

}

object UserActor {

    def props(out: ActorRef, tweetServiceClient: ActorRef): Props = {
        Props(new UserActor(out, tweetServiceClient))
    }

    case object FetchTweets

}