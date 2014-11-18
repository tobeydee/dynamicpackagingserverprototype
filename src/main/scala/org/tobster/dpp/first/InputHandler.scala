package org.tobster.dpp.first

import org.joda.time.DateTime
import akka.actor.{Actor, ActorLogging, Props, ActorRef}
import spray.http.HttpMethods._ // GET
import org.tobster.dpp.http.StartMsg

case class SearchParams(orig: String, dest: String, pax: Int, start: DateTime,
                        end: DateTime, connection: ActorRef, requestor: ActorRef,
                        inputHandler: ActorRef)

class InputHandler extends Actor with ActorLogging {
  
  private var resultCollectorRef: ActorRef = null

  private def parseParams(params: List[Tuple2[String, String]]) = {
    val orig = params(0)._2
    val dest = params(1)._2
    val pax = params(2)._2.toInt
    val d1 = params(3)._2.split('.').map(_.toInt)
    val d2 = params(4)._2.split('.').map(_.toInt)
    val startDate = new DateTime(d1(2), d1(1), d1(0), 0, 0, 0, 0)
    val endDate = new DateTime(d2(2), d2(1), d2(0), 0, 0, 0, 0)
    Tuple5(orig, dest, pax, startDate, endDate)
  }
  
  def receive = {
    
    case StartMsg(urlParams, connection, requestor) => {

        resultCollectorRef = context.actorOf(Props[ResultCollector])
        val param = parseParams(urlParams)
        resultCollectorRef ! SearchParams(param._1, param._2, param._3, param._4,
                                          param._5, connection, requestor, self)
        
      }

  }
  
  override def postStop {
    context.stop(resultCollectorRef)
  }

  
}
