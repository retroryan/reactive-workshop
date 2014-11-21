package controllers

import play.api.mvc.{AnyContent, WebSocket, Action, Controller}
import play.api.libs.json.{JsString, Json, JsObject, JsValue}

import play.api.Play.current
import akka.util.Timeout
import scala.concurrent.duration._
import akka.pattern.ask


import scala.concurrent.ExecutionContext.Implicits.global
import stockActors.{SentimentActor, StockUserActor}
import actors.Actors

object AkkaStocks extends Controller {

    def stocks = Action {
        Ok(views.html.stocks())
    }

    def stockFeed = WebSocket.acceptWithActor[JsValue, JsValue] { implicit request => out =>
        StockUserActor.props(out, Actors.stockManagerActor)
    }

    implicit val sentimentAskTimeout: Timeout = Duration(20, SECONDS)

    def get(symbol: String): Action[AnyContent] = Action.async {

        (Actors.sentimentActor ? SentimentActor.GetSentiment(symbol)).mapTo[JsObject].map {
            sentimentJson => {
                Ok(sentimentJson)
            }
        }.recover {
            case nsee: NoSuchElementException =>
                InternalServerError(Json.obj("error" -> JsString("Could not fetch the tweets")))
        }
    }

}
