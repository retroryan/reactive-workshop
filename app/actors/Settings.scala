package actors

import akka.actor.{ Actor, ExtendedActorSystem, Extension, ExtensionKey }
import akka.util.Timeout
import scala.concurrent.duration.{ Duration, FiniteDuration, MILLISECONDS => Millis }

object Settings extends ExtensionKey[Settings]

class Settings(system: ExtendedActorSystem) extends Extension {

    private val config = system.settings.config

    implicit val askTimeout: Timeout =
        Duration(config.getDuration("ask-timeout", Millis), Millis)

    val SENTIMENT_URL = config.getString("sentiment.url")

    val TWEET_SEARCH_URL = config.getString("tweet.url")

    val GEOCODE_URL = config.getString("geocode.url")


}

trait SettingsActor {
  this: Actor =>

  val settings: Settings =
    Settings(context.system)
}
