package stockActors

import stockActors.StockManagerActor.{EventWatchStock, UnwatchStock, WatchStock}
import akka.actor.{ActorRef, ActorLogging, Props, Actor}
import akka.persistence.PersistentActor

class StockManagerActor extends PersistentActor with ActorLogging {

    def persistenceId: String = {
        return "stockManager_" + context.self.path.name
    }

    override def receiveCommand: Receive = {
        case watchStock@WatchStock(symbol) =>
            persist(EventWatchStock(symbol)) { eventWatchStock =>
                log.info(s"StockManagerActor WatchStock $symbol")
                // get or create the StockActor for the symbol and forward this message
                context.child(symbol).getOrElse {
                    context.actorOf(StockActor.props(symbol), symbol)
                } forward watchStock
            }
        case unwatchStock@UnwatchStock(Some(symbol)) =>
            // if there is a StockActor for the symbol forward this message
            context.child(symbol).foreach(_.forward(unwatchStock))
        case unwatchStock@UnwatchStock(None) =>
            // if no symbol is specified, forward to everyone
            context.children.foreach(_.forward(unwatchStock))
    }

    override def receiveRecover: Receive =
    {
        case EventWatchStock(symbol) => context.child(symbol).getOrElse {
            context.actorOf(StockActor.props(symbol), symbol)
        }
    }
}

object StockManagerActor {
    def props(): Props =
        Props(new StockManagerActor())

    case class WatchStock(symbol: String)

    case class UnwatchStock(symbol: Option[String])

    case class StockUpdate(symbol: String, price: Number)

    case class StockHistory(symbol: String, history: List[Double])

    case class EventWatchStock(symbol: String)

}
