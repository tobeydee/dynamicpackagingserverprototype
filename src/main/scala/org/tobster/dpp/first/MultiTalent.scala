package org.tobster.dpp.first

import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.xml.NodeSeq

import org.joda.time.DateTime

import akka.actor.{Actor, ActorLogging, ActorRef}

import org.tobster.dpp.datamodel.{TourismPackage, FlightLookup, TourismProduct}
import org.tobster.dpp.http.SearchParamsMsg


case class SearchResultsMsg(products: List[(TourismProduct, TourismProduct)], timestamp: Long)


case class ResultXmlPackages(packages: NodeSeq,
                             timestamp: Long,
                             totalPackages: Int,
                             computationTime: Long,
                             childrenCount: Int)


class MultiTalent(totalPackages: Int,
                  maxResultPackages: Int,
                  connectionHandler: ActorRef,
                  currentChildren: Int) extends Actor with ActorLogging {

  // ExecutionContext needed for Futures

  import context.dispatcher

  /*
   * InputHandler
   */
  private def parseSearchParams(params: List[(String, String)]) = {
    val orig = params(0)._2
    val dest = params(1)._2
    val pax = params(2)._2.toInt
    val d1 = params(3)._2.split('.').map(_.toInt)
    val d2 = params(4)._2.split('.').map(_.toInt)
    val startDate = new DateTime(d1(2), d1(1), d1(0), 0, 0, 0, 0)
    val endDate = new DateTime(d2(2), d2(1), d2(0), 0, 0, 0, 0)
    Tuple5(orig, dest, pax, startDate, endDate)
  }

  private def maxItems[T <: Any](xs: List[T], limit: Int): Int = {
    xs.length > limit match {
      case true => limit
      case _ => xs.length
    }
  }

  /*
   * ResultCollector
   */
  private def collectResults(orig: String,
                             dest: String,
                             pax: Int,
                             start: DateTime,
                             end: DateTime,
                             accuracy: Int,
                             timestamp: Long) = {

    val f1 = Future {
      val xs = FlightLookup.searchFlight(orig, dest, pax, start, accuracy)
      xs.slice(0, maxItems(xs, maxResultPackages))
    }

    val f2 = Future {
      val xs = FlightLookup.searchFlight(dest, orig, pax, start, accuracy)
      xs.slice(0, maxItems(xs, maxResultPackages))
    }

    /*
    val f3 = Future {
      val xs = FlightLookup.searchFlight(orig, dest, 1, start, 14)
      xs.slice(0, maxItems(xs, maxResultPackages))
    }
    
    val f4 = Future {
      val xs = FlightLookup.searchFlight(dest, orig, 1, start, 14)
      xs.slice(0, maxItems(xs, maxResultPackages))
    }
    */

    val combinations1 = f1 flatMap { a =>
      f2 map { b =>
        computeCombinations(a, b)
      }
    }

    /*
    val combinations2 = f3 flatMap { c =>
      f4 map { d =>
        computeCombinations(c, d)
      }
    }
    
    val results = combinations1 flatMap { c1 =>
      combinations2 map { c2 =>
        val xs = computeCombinations2(c1, c2)
        xs.slice(0, maxItems(xs, totalPackages))
      }
    }
    */

    combinations1.onComplete {
      case Success(xs) => {
        self ! SearchResultsMsg(xs, timestamp)
      }
      case Failure(f) => {
        log.info("Failed to compute combinations: " + f.getMessage)
      }
    }

  }

  /*
   * CombinationComputer
   */
  private def computeCombinations(a: List[TourismProduct], b: List[TourismProduct]): List[(TourismProduct, TourismProduct)] = {
    a.par.flatMap(x => b.map(y => Tuple2(x, y))).toList
  }

  private def computeCombinations2(a: List[(TourismProduct, TourismProduct)], b: List[(TourismProduct, TourismProduct)]) = {
    a.par.flatMap(x => b.map(y => Tuple4(x._1, x._2, y._1, y._2))).toList
  }

  /*
   * PackageBuilder
   */
  private def buildPackages(results: List[(TourismProduct, TourismProduct)]) = {
    results.map(pair => new TourismPackage(
      List(pair._1, pair._2))
    ).sortWith((a, b) => a.price < b.price)
  }

  /*
   * Serialize to XML
   */
  private def serializePackages(packages: List[TourismPackage]): NodeSeq = {
    <packages>
      {packages.map(_.toXml)}
    </packages>
  }

  private val ACCURACY = 10

  // Start of computation
  private var t1 = 0L

  def receive = {

    case SearchParamsMsg(urlParams, timestamp) => {

      // Stop the time, when the request comes in.
      t1 = System.currentTimeMillis

      val params = parseSearchParams(urlParams)

      collectResults(params._1,
        params._2,
        params._3,
        params._4,
        params._5,
        ACCURACY,
        timestamp)
    }

    case SearchResultsMsg(combinations, timestamp) => {

      val packages = buildPackages(combinations)

      val topPackages = packages.slice(0, maxItems(packages, totalPackages))

      // Compute difference between start and end of computation.
      val computationTime: Long = System.currentTimeMillis - t1

      /*
       * Send result back to ConnectionHandler of master-node
       */
      connectionHandler ! ResultXmlPackages(serializePackages(topPackages),
        timestamp,
        packages.size,
        computationTime,
        currentChildren)

      context.stop(self)

    }

  }

}