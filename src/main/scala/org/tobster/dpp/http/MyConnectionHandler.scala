package org.tobster.dpp.http

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Terminated}
import akka.io.Tcp
import org.tobster.dpp.first.ResultXmlPackages
import spray.http.HttpMethods._
import spray.http.{HttpRequest, HttpResponse}

import scala.collection.mutable.HashMap

case class StartMsg(params: List[(String,String)], connection: ActorRef, requestor: ActorRef)

case class SearchRequestMsg(params: List[(String,String)], requestor: ActorRef, timestamp: Long)

object MyConnectionHandler {
  def props(remote: InetSocketAddress, connection: ActorRef, router: ActorRef): Props =
    Props(new MyConnectionHandler(remote, connection, router))
}

class MyConnectionHandler(remote: InetSocketAddress, connection: ActorRef,
                          router: ActorRef) extends Actor with ActorLogging {

  // We need to know when the connection dies without sending a `Tcp.ConnectionClosed`
  context.watch(connection)
  
  /*
   * Lookup an ActorRef with a given timestamp.
   */
  private val requestors = new HashMap[Long, ActorRef]

  def receive: Receive = {
    
    case HttpRequest(GET, uri, _, _, _) => {
        
        val timestamp: Long = System.currentTimeMillis
        
        requestors += timestamp -> sender
        
        // Forward to Worker MyHttpServer Actor
        router forward SearchRequestMsg(uri.query.toList, self, timestamp)
      }
      
      /*
       * Receive from MultiTalent...
       */
    case ResultXmlPackages(xml, timestampKey, totalPackages, computationTime, childCount) => {
        
        val httpClient = requestors(timestampKey)
        
        val payload = childCount + "--" + totalPackages + "--" + computationTime + "--" + xml.toString
        
        httpClient ! HttpResponse(entity = payload)
      }
      
    case _: Tcp.ConnectionClosed =>
      log.info("Stopping, because connection for remote address {} closed", remote)
      context.stop(self)
      
    case Terminated(`connection`) =>
      log.info("Stopping, because connection for remote address {} died", remote)
      context.stop(self)
  }
}

