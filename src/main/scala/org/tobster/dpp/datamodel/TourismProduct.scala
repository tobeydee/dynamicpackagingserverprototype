package org.tobster.dpp.datamodel

import scala.xml.NodeSeq

trait TourismProduct {

  val price: Double
  
  def toXml: NodeSeq
  
}
