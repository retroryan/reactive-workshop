package stockActors

import akka.actor.{Props, ActorLogging, Actor, ActorRef}
import play.api.libs.json._
import play.Play
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import play.api.libs.json.JsNumber

import scala.collection.JavaConverters._
import stockActors.StockManagerActor.StockHistory
import actors.SettingsActor

/** The out actor is wired in by Play Framework when this Actor is created.
  * When a message is sent to out the Play Framework then sends it to the client WebSocket.
  *
  * */
class StockUserActor(out: ActorRef, stockManager: ActorRef) extends Actor with ActorLogging with SettingsActor {


    // watch the default stocks
    for (stockSymbol <- settings.DEFAULT_STOCKS.asScala) {
        stockManager ! StockManagerActor.WatchStock(stockSymbol)
    }

    def receive = {
        //Handle the FetchTweets message to periodically fetch tweets if there is a query available.
        case StockManagerActor.StockUpdate(symbol, price) =>
            val stockUpdateJson: JsObject = JsObject(Seq(
                "type" -> JsString("stockupdate"),
                "symbol" -> JsString(symbol),
                "price" -> JsNumber(price.doubleValue())
            ))
            out ! stockUpdateJson

        case StockHistory(symbol, history) =>
            val jsonHistList = JsArray(history.map(price => JsNumber(price)))
            val stockHistoryJson: JsObject = JsObject(Seq(
                "type" -> JsString("stockhistory"),
                "symbol" -> JsString(symbol),
                "history" -> jsonHistList
            ))
            out ! stockHistoryJson

        case message: JsValue =>
            (message \ "symbol").asOpt[String] match {
                case Some(symbol) =>
                    stockManager ! StockManagerActor.WatchStock(symbol)
                case None => log.error("symbol was not found in json: $message")
            }
    }

    override def postStop() {
        stockManager ! StockManagerActor.UnwatchStock(None)
    }
}


object StockUserActor {
    def props(out: ActorRef, stockManager: ActorRef): Props =
        Props(new StockUserActor(out, stockManager))

}
