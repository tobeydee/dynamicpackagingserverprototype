package org.tobster.dpp.camel

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.camel._
import org.apache.camel.builder.RouteBuilder

class MyCamelRoute(sys: ActorSystem, fw: ActorRef) extends RouteBuilder {
  
  def configure = {
    from("file:/path/to/data?fileName=flights.csv&autoCreate=false&noop=true")
    .split(body.tokenize("\n")).streaming to fw
  }
}

object MyCamelApp extends App {
  
  val system = ActorSystem("my-system")
  
  val camel = CamelExtension(system)
  
  val a1 = system.actorOf(Props[CamelMessageTranslator], name="translator")
  
  camel.context.addRoutes(new MyCamelRoute(system, a1))
}
