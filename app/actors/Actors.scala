package actors

import play.api._
import play.api.libs.concurrent.Akka
import stockActors.{SentimentActor, StockManagerActor}
import akka.actor.{Address, RootActorPath}

/**
 * Lookup for actors used by the web front end.
 */
object Actors {

    private def actors(implicit app: Application) = app.plugin[Actors]
        .getOrElse(sys.error("Actors plugin not registered"))

    /**
     * Get the tweet loader client.
     */
    def tweetLoader(implicit app: Application) = actors.tweetLoader

    def sentimentActor(implicit app: Application) = actors.sentimentActor

    def stockManagerActor(implicit app: Application) = actors.stockManagerActor
}

/**
 * Manages the creation of actors in the web front end.
 *
 * This is discovered by Play in the `play.plugins` file.
 */
class Actors(app: Application) extends Plugin {

    private def system = Akka.system(app)

    override def onStart() = {
    }

    private lazy val tweetLoader = system.actorOf(TweetLoader.props, "tweetLoader")

    private lazy val sentimentActor = system.actorOf(SentimentActor.props, "sentimentActor")

    //change to the stock manager to be looked up remotely
    private val address = Address("akka.tcp", "application", "localhost", 2555)
    private val path = RootActorPath(address) / "user" / "stockManager"
    private lazy val stockManagerActor = system.actorSelection(path)

}
