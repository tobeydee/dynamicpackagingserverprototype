package org.tobster.dpp.datamodel

import org.joda.time.{DateTime, Days}

class Flight(val orig: String, val dest: String, val date: DateTime, val price: Double, val seats: Int) extends TourismProduct {
  
  private def canEqual(other: Any): Boolean = other.isInstanceOf[Flight]
  
  override def hashCode = toString.hashCode
  
  /*
   * Recipe was taken from: 
   * http://booksites.artima.com/programming_in_scala_2ed/examples/html/ch30.html
   */
  override def equals(other: Any): Boolean = other match {
    case that: Flight => (this.canEqual(that)) && 
                         (orig.equals(that.orig)) &&
                         (dest.equals(that.dest)) &&
                         (date.equals(that.date)) &&
                         (price.equals(that.price)) &&
                         (seats.equals(that.seats))
    case _ => false
  }
  
  override def toString = {
    "from " + orig + " to " + dest + " at " + date + " for " + price + " available " + seats
  }
  
  def compareDate(other: DateTime, maxDays: Int): Boolean = {
    
    val diff = Days.daysBetween(date, other).getDays.abs
    
    diff <= maxDays match {
      case true => true
      case _ => false
    }
  }
  
  def toXml = {
    <product type="flight">
      <origin>{orig}</origin>
      <destination>{dest}</destination>
      <date>{date}</date>
      <price>{price}</price>
      <seats>{seats}</seats>
    </product>
  }
  
}
 