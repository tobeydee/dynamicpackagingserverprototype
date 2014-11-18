package org.tobster.dpp.datamodel

import scala.xml.NodeSeq

class TourismPackage(components: List[TourismProduct]) {
  
  val price: Double = components.map(_.price).foldLeft(0.0)(_ + _)
  
  def toXml: NodeSeq = {
    <package>
      {
        components.map(_.toXml)
      }
    </package>
  }
  
}
