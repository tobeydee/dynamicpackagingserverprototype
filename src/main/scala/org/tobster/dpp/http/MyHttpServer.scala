package org.tobster.dpp.http

import akka.io.IO
import akka.routing.RoundRobinRouter
import akka.actor.{Actor, ActorIdentity, ActorLogging, ActorRef, Address, Identify, Props, ActorSelection}
import spray.can.Http

import org.tobster.dpp.first.MultiTalent

case class AddNodeMsg(address: Address)

case class SearchParamsMsg(params: List[(String, String)], timestamp: Long)

object MyHttpServer {
  def props(host: String, port: Int, master: Boolean, maxSlaves: Int, totalPackages: Int, maxResultPackages: Int): Props = 
              Props(new MyHttpServer(host, port, master, maxSlaves, totalPackages, maxResultPackages))
}


class MyHttpServer(host: String, port: Int, master: Boolean, maxSlaves: Int,
                   totalPackages: Int, maxResultPackages: Int) extends Actor with ActorLogging {

  import context.system

  IO(Http) ! Http.Bind(self, host, port)
  
  private var httpServers: List[ActorRef] = Nil
  
  private var router: ActorRef = null

  override def receive: Receive = {
    
    case Http.Connected(remote, local) if (master) => {
        
        log.info("Remote address {} connected", remote)
      
        sender ! Http.Register(context.actorOf(MyConnectionHandler.props(remote, sender, router)))
      }
      
    case SearchRequestMsg(params, connectionHandler, timestamp) if (!master) => {
        
        val multiTalent = 
          context.actorOf(
            Props(
              classOf[MultiTalent], totalPackages, maxResultPackages, connectionHandler, context.children.size+1
            ))
        
        multiTalent ! SearchParamsMsg(params, timestamp)
      }
      
    case AddNodeMsg(addr) if (master) => {
        
        val ip: String = addr.host match {
          case Some(s) => s
          case _ => ""
        }
        
        log.info("HttpServer got message from " + ip)
        
        if (ip != this.host) {
          
          val selection: ActorSelection = context.actorSelection("akka.tcp://dpp@" + ip + ":12555/user/httpServer")
        
          selection ! Identify
        }
 
      }
      
    case Identify => {
        sender ! ActorIdentity(null, Some(self))
      }
    
    case ActorIdentity(id, refOption) => {
        
        val ref: ActorRef = refOption match {
          case Some(actorRef) => actorRef
          case _ => null
        }
        
        log.info("Answer from: " + ref.path)
        
        httpServers = ref :: httpServers
        
        if (httpServers.length == maxSlaves) {
          router = system.actorOf(
            Props.empty.withRouter(
              RoundRobinRouter(routees = httpServers))
          )
          log.info(s"Router init done! ($maxSlaves slaves)")
        }
      }
    
  }
}

