package actors

import akka.actor.{ActorLogging, Props, Actor, ActorRef}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.JsValue

import scala.concurrent.duration._

class UserActor(out:ActorRef, tweetLoaderClient: ActorRef) extends Actor with ActorLogging {

    var maybeQuery: Option[String] = None

    val tick = context.system.scheduler.schedule(Duration.Zero, 5.seconds, self, UserActor.FetchTweets)

    def receive = {

        case UserActor.FetchTweets =>
            maybeQuery.foreach { query =>
                tweetLoaderClient ! TweetLoader.LoadTweet(query)
            }
        case TweetLoader.NewTweet(tweetUpdate) =>
            out ! tweetUpdate

        case message: JsValue =>
            log.info(s"setting query: $message")
            maybeQuery = (message \ "query").asOpt[String]

    }

    override def postStop() {
        tick.cancel()
    }

}

object UserActor {

    def props(out:ActorRef, tweetServiceClient: ActorRef): Props = {
        Props(new UserActor(out, tweetServiceClient))
    }

    case object FetchTweets



}