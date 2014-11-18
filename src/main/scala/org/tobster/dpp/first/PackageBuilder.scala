/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.tobster.dpp.first

import akka.actor.{Actor, ActorLogging, ActorRef}
import org.tobster.dpp.datamodel.{Flight, TourismPackage}

case class ResultPackages(xs: List[TourismPackage], requestor: ActorRef,
                          inputHandler: ActorRef)

class PackageBuilder extends Actor with ActorLogging {

  private def buildPackages(results: List[Tuple2[Flight,Flight]]) = {
    results.map(pair => new TourismPackage(List(pair._1, pair._2))).sortWith((a,b) => a.price < b.price)
  }
  
  def receive = {
    
    case ComputedResult(results, connection, requestor, inputHandler) => {
        connection ! ResultPackages(buildPackages(results), requestor, inputHandler)
    }
    
  }
  
}
