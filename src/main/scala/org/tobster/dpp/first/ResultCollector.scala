/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.tobster.dpp.first

import org.joda.time.DateTime

import akka.actor.{Actor, ActorLogging, Props, ActorRef}
import org.tobster.dpp.datamodel.Flight

case class CollectedResults(l1: List[Flight], l2: List[Flight],
                            connection: ActorRef, requestor: ActorRef,
                            inputHandler: ActorRef)

case class SearchFlight(orig: String, dest: String, pax: Int, date: DateTime)

class ResultCollector extends Actor with ActorLogging {
  
  private var resultCounter = 0
  
  private val MAX_RESULTS = 2
  
  private var results: List[List[Flight]] = Nil
  
  private var connector1: ActorRef = null
  private var connector2: ActorRef = null
  private var combinationComputerRef: ActorRef = null
  private var connectionRef: ActorRef = null
  private var requestorRef: ActorRef = null
  private var inputHandlerRef: ActorRef = null
  
  private def aggregateResults(xs: List[Flight]) = {
    results = xs :: results
    resultCounter += 1
    if (resultCounter == MAX_RESULTS) {
      combinationComputerRef = context.actorOf(Props[CombinationComputer])
      combinationComputerRef ! CollectedResults(results(0).toList, results(1).toList,
                                  connectionRef, requestorRef, inputHandlerRef)
    }
  }
  
  private def gatherResults(orig: String, dest: String, pax: Int,
                            start: DateTime, end: DateTime) = {
    
    connector1 = context.actorOf(Props[DatabaseConnector])
    connector1 ! SearchFlight(orig, dest, pax, start)
    
    connector2 = context.actorOf(Props[DatabaseConnector])
    connector2 ! SearchFlight(dest, orig, pax, end)
  }
  
  def receive = {
    
    case SearchParams(orig, dest, pax, start, end, connection, requestor,
                      inputHandler) => {
        connectionRef = connection
        requestorRef = requestor
        inputHandlerRef = inputHandler
        gatherResults(orig, dest, pax, start, end)  
      }
      
      
    case FlightSearchResult(xs) => aggregateResults(xs)
    
  }
  
  override def postStop {
    context.stop(connector1)
    context.stop(connector2)
    context.stop(combinationComputerRef)
  }
  
}
