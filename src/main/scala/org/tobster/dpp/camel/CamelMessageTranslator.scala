package org.tobster.dpp.camel

import akka.actor.{Actor, ActorLogging}
import akka.camel.CamelMessage

class CamelMessageTranslator extends Actor with ActorLogging {
  def receive = {
    case msg: CamelMessage =>
      msg.mapBody {
        body: String =>  // DO SOMETHING
      }
  }
}
