/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.tobster.dpp.first

import akka.actor.{Actor, ActorLogging}

import org.tobster.dpp.datamodel.Flight
import org.tobster.dpp.datamodel.FlightLookup

case class FlightSearchResult(flights: List[Flight])

class DatabaseConnector extends Actor with ActorLogging {
  
  private val ACCURACY = 5
  
  def receive = {
      
    case SearchFlight(orig, dest, pax, date) => {
        sender ! FlightSearchResult(FlightLookup.searchFlight(orig, dest, pax, date, ACCURACY))
    }
  }
  
}
