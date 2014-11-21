package backend

import akka.actor.{PoisonPill, ActorSystem}
import stockActors.StockManagerActor
import akka.contrib.pattern.ClusterSingletonManager

import actors.{TweetLoader, Settings}
import backend.journal.SharedJournalSetter

/**
 * Main class for starting cluster nodes.
 */
object MainClusterManager extends BaseApp {

    override protected def initialize(system: ActorSystem, settings: Settings): Unit = {
        //system.actorOf(StockManagerActor.props, "stockManager")

        system.actorOf(
            ClusterSingletonManager.props(
                StockManagerActor.props,
                "stockManager",
                PoisonPill,
                Some("backend")
            ),
            "stockManager-singleton"
        )

        system.actorOf(
            ClusterSingletonManager.props(
                TweetLoader.props,
                "tweetLoader",
                PoisonPill,
                Some("backend")
            ),
            "tweetLoader-singleton"
        )

        //system.actorOf(SharedJournalSetter.props, "shared-journal-setter")
    }

}
