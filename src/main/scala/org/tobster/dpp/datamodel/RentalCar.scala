package org.tobster.dpp.datamodel

class RentalCar(startDate: String, 
                endDate: String, 
                val price: Double) extends TourismProduct {

  def toXml = {
    <product type="rental_car">
      <price>{price}</price>
    </product>
  }
  
}
