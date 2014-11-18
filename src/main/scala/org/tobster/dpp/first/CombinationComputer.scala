package org.tobster.dpp.first

import akka.actor.{Actor, ActorLogging, Props, ActorRef}
import org.tobster.dpp.datamodel.Flight

case class ComputedResult(data: List[(Flight, Flight)], connection: ActorRef, requestor: ActorRef, inputHandler: ActorRef)

class CombinationComputer extends Actor with ActorLogging {
  
  private var packageBuilderRef: ActorRef = null
  
  private def compute(l1: List[Flight], l2: List[Flight]) = {
    
    val xs = new Array[(Flight, Flight)](l1.size * l2.size)
    
    var i: Int = 0
    
    for (flight1 <- l1) {
      for (flight2 <- l2) {
          xs(i) = Tuple2(flight1, flight2)
          i += 1
      }
    }
    
    xs.toList
  }
  
  def receive = {
    
    case CollectedResults(l1, l2, connection, requestor, inputHandler) => {
        packageBuilderRef = context.actorOf(Props[PackageBuilder])
        packageBuilderRef ! ComputedResult(compute(l1, l2), connection, requestor, inputHandler)
    }
  }
  
  override def postStop {
    context.stop(packageBuilderRef)
  }

}
