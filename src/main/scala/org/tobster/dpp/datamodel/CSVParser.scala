package org.tobster.dpp.datamodel

import scala.collection.immutable.HashMap
import scala.io.Source
import com.github.nscala_time.time.Imports._

object CSVParser {
  
  private val FIELDS_IDX =
    Map(
      "ORIG" -> 1,
      "DEST" -> 2,
      "DATE" -> 3,
      "PRICE" -> 13,
      "SEATS" -> 15
    )

  private def extract(line: String): Flight = {
    
    val data = line.split(';')
    
    def get(name: String) = {
      data(FIELDS_IDX(name))
    }
    
    val dayMonthYear = get("DATE").split('.')map(_.toInt)
    
    val date = new DateTime(dayMonthYear(2), dayMonthYear(1), dayMonthYear(0), 0, 0, 0, 0)
    
    val price = get("PRICE").replace(",", ".").trim.toDouble
    
    val seats: Int = get("SEATS").trim match {
      case star: String if (star == "*") => 999
      case number: String => number.toInt
    }
    
    new Flight(get("ORIG"), get("DEST"), date, price, seats)
  }
  
  def parse(filename: String): List[Flight] = {
    Source.fromFile(filename).getLines.toList.map(extract).filter(_.price != 0.0)
  }
  
}
