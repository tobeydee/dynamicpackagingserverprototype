package org.tobster.dpp

import akka.camel.CamelMessage
import akka.actor.{Actor, ActorLogging}

class CamelMsgHandler extends Actor with ActorLogging {
  
  def receive = {
    
    case msg: CamelMessage =>
      
      val params = msg.headers.get("CamelHttpQuery")
      
      println("Received " + params)
      
      sender ! "fooo"

  }
}
