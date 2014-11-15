package stockActors

import akka.actor._
import scala.collection.immutable.{Queue, HashSet}
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Random
import stockActors.StockActor._
import stockActors.StockManagerActor.WatchStock
import stockActors.StockManagerActor.StockUpdate
import stockActors.StockManagerActor.StockHistory
import stockActors.StockManagerActor.UnwatchStock
import akka.pattern.ask
import akka.util.Timeout

/**
 * There is one StockActor per stock symbol.  The StockActor maintains a list of users watching the stock and the stock
 * values.  Each StockActor updates a rolling dataset of randomly generated stock values.
 */

class StockActor(symbol: String) extends Actor {

    lazy val stockQuote: StockQuote = FakeStockQuote

    protected[this] var watchers: HashSet[ActorRef] = HashSet.empty[ActorRef]

    // A random data set which uses stockQuote.newPrice to get each data point
    var stockHistory: Queue[Double] = {
        lazy val initialPrices: Stream[Double] = (new Random().nextDouble * 800) #:: initialPrices.map(previous => stockQuote.newPrice(previous))
        initialPrices.take(50).to[Queue]
    }

    // Fetch the latest stock value every 75ms
    val stockTick = context.system.scheduler.schedule(Duration.Zero, 75.millis, self, FetchLatest)

    def receive = {
        case FetchLatest =>
            // add a new stock price to the history and drop the oldest
            val newPrice = stockQuote.newPrice(stockHistory.last.doubleValue())
            stockHistory = stockHistory.drop(1) :+ newPrice
            // notify watchers
            watchers.foreach(_ ! StockUpdate(symbol, newPrice))
        case WatchStock(_) =>
            // send the stock history to the user
            sender ! StockHistory(symbol, stockHistory.toList)
            // add the watcher to the list
            watchers = watchers + sender
        case UnwatchStock(_) =>
            watchers = watchers - sender
            if (watchers.size == 0) {
                stockTick.cancel()
                context.stop(self)
            }
    }

    implicit val identifyAskTimeout: Timeout = Duration(10, SECONDS)

    private def addWatcherIfAlive(watcher: ActorRef) {
        (watcher ? Identify(watcher.path.name)).mapTo[ActorIdentity].map {
            actorIdentity =>
                actorIdentity.ref match {
                    case Some(watcher) => self ! AddWatcherAfterRecover(watcher)
                    case None => self ! UnwatchStock(Option(symbol))
                }

        }.recover {
            case failure =>
                self ! UnwatchStock(Option(symbol))
        }
    }
}

object StockActor {

    def props(symbol: String): Props =
        Props(new StockActor(symbol))

    case object FetchLatest

    case class EventStockPriceUpdated(price: Double)

    case class EventWatcherAdded(watcher: ActorRef)

    case class EventWatcherRemover(watcher: ActorRef)

    case class TakeSnapshot(stockHistory: Queue[Double], watchers: HashSet[ActorRef])

    case class AddWatcherAfterRecover(watcher: ActorRef)

    case object Snap


}