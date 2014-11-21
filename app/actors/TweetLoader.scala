package actors

import akka.actor._
import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.json._
import scala.util.Failure
import scala.util.Success
import utils.WSUtils

/**
 * Tweet Loader Actor
 */
class TweetLoader extends Actor with ActorLogging with SettingsActor {

    implicit val ec: ExecutionContext = context.system.dispatcher

    override def receive: Receive = {

        case TweetLoader.LoadTweet(search) => {
            val querySender = sender()
            fetchTweets(search) onComplete {
                case Success(respJson) ⇒ {
                    querySender ! TweetLoader.NewTweet(respJson)
                }
                case Failure(f) ⇒ {
                    log.info(s"tweet loader failed!")
                    sender() ! Status.Failure(f)
                }
            }
        }

    }

    // searches for tweets based on a query
    def fetchTweets(query: String)(implicit ec: ExecutionContext): Future[JsValue] = {
        WSUtils.url(settings.TWEET_SEARCH_URL)
            .withQueryString("q" -> query)
            .get()
            .map {
            resp => resp.json
        }
    }
}

object TweetLoader {

    case class LoadTweet(search: String)

    case class NewTweet(tweet: JsValue)

    def props(): Props = {
        Props(new TweetLoader())
    }
}
