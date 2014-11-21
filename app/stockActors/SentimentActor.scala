package stockActors

import akka.actor.{Address, Props, Actor, ActorLogging}
import play.api.libs.json.{JsObject, JsString, Json, JsValue}
import scala.concurrent.Future
import play.api.libs.ws.WSResponse
import actors.SettingsActor
import utils.WSUtils

import akka.pattern.pipe
import model.Tweet
import akka.cluster.Cluster

class SentimentActor extends Actor with ActorLogging with SettingsActor {

    import context.dispatcher

    private val selfAddress: Address = Cluster.get(context.system).selfAddress
    log.info(s"SentimentActor running at ${selfAddress}")

    override def receive: Receive = {
        case SentimentActor.GetSentiment(symbol) => {
            val origSender = sender()
            log.info(s"GetSentiment = $symbol}")
            val futureStockSentiments: Future[JsObject] = for {
                tweets <- getTweets(symbol) // get tweets that contain the stock symbol
                futureSentiments = loadSentimentFromTweets(tweets.json) // queue web requests each tweets' sentiments
                sentiments <- Future.sequence(futureSentiments) // when the sentiment responses arrive, set them
            } yield {
                var json = sentimentJson(sentiments)
                log.info(s"sentiment json = $json}")
                json
            }
            futureStockSentiments.pipeTo(origSender)
        }
    }

    def getTextSentiment(text: String): Future[WSResponse] =
        WSUtils.url(settings.SENTIMENT_URL) post Map("text" -> Seq(text))

    def getAverageSentiment(responses: Seq[WSResponse], label: String): Double = responses.map { response =>
        (response.json \\ label).head.as[Double]
    }.sum / responses.length.max(1) // avoid division by zero

    def loadSentimentFromTweets(json: JsValue): Seq[Future[WSResponse]] =
        (json \ "statuses").as[Seq[Tweet]] map (tweet => getTextSentiment(tweet.text))

    def getTweets(symbol: String): Future[WSResponse] = {
        WSUtils.url(settings.TWEET_SEARCH_URL).withQueryString("q" -> symbol).get.withFilter { response =>
            response.status == 200 //HTTP 200 OK Response
        }
    }

    def sentimentJson(sentiments: Seq[WSResponse]) = {
        val neg = getAverageSentiment(sentiments, "neg")
        val neutral = getAverageSentiment(sentiments, "neutral")
        val pos = getAverageSentiment(sentiments, "pos")

        val response = Json.obj(
            "probability" -> Json.obj(
                "neg" -> neg,
                "neutral" -> neutral,
                "pos" -> pos
            )
        )

        val classification =
            if (neutral > 0.5)
                "neutral"
            else if (neg > pos)
                "neg"
            else
                "pos"

        response + ("label" -> JsString(classification))
    }
}

object SentimentActor {

    case class GetSentiment(symbol: String)

    case class SentimentResults(results: JsValue)

    def props(): Props =
        Props(new SentimentActor())

}
