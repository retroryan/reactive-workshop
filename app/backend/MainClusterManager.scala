package backend

import akka.actor.ActorSystem
import stockActors.StockManagerActor

// import akka.contrib.pattern.ClusterSingletonManager

// import actors.{StockManagerActor, Settings}

import actors.Settings

/**
 * Main class for starting cluster nodes.
 */
object MainClusterManager extends BaseApp {

    override protected def initialize(system: ActorSystem, settings: Settings): Unit = {
        system.actorOf(StockManagerActor.props, "stockManager")
    }

}
