package org.tobster.dpp.datamodel

import scala.collection.mutable.HashMap

import org.joda.time.DateTime

object FlightLookup {
  
  print("FlightLookup: ")

  private val FileNames = List(
    "cfi_all_spo.csv",
    "cfi_all_spo_delta.csv"
  )
  
  // Immutable Map: Origin -> List[Flight]
  private val allFlights: Map[String, List[Flight]] = {
    
    println("Initialize available flights...")
    
    val flights = new HashMap[String, List[Flight]] // mutable HashMap!
    
    def insertFlights(unsortedFlights: List[Flight]) = {
    
      def insert(flight: Flight) = {
        if (flights.contains(flight.orig)) {
          flights(flight.orig) = flight :: flights(flight.orig)
        }
        else {
          flights(flight.orig) = List(flight)
        }
      }
    
      unsortedFlights.foreach(insert)
    }
    
    FileNames.foreach(f => insertFlights(CSVParser.parse(f)))
    
    flights.toMap
  }
  
  private def getFlightsByOrigin(orig: String): List[Flight] =
    allFlights.contains(orig) match {
      case true => allFlights(orig)
      case _ => Nil
    }
  
  def searchFlight(orig: String, dest: String, pax: Int, date: DateTime, accuracy: Int) =
    getFlightsByOrigin(orig)
    .filter(_.dest == dest)
    .filter(_.seats >= pax)
    .filter(_.compareDate(date, accuracy))
  
  def totalOrigins = allFlights.keys.size
  
  def totalDestinations = allFlights.values.map(xs => xs.map(_.dest).toSet).reduce(_ ++ _)
  
  def orig2Dest = allFlights.map(t => (t._1, t._2.map(_.dest).toSet))
  
  def totalFlights = allFlights.values.map(_.size).foldLeft(0)(_ + _)
  
  def prettyPrint = allFlights.map(t => (t._1, t._2.size))
  
}
