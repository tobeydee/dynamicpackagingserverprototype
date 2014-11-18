package org.tobster.dpp.datamodel

import scala.concurrent.Future
import scala.collection.mutable.Stack
import scala.util.{Failure, Success}

import akka.actor.{Actor, ActorLogging}

private case class QueryOrder(params: List[String])

private case class SingleResultReady(result: List[TourismProduct])

private case class CombinationResultReady(result: List[ProductCombination])

private case class ResultFailed(result: Throwable)

private class ProductCombination(val products: List[TourismProduct])

class MultiProductBuilder(products: Int) extends Actor with ActorLogging {
  
  // ExecutionContext needed for Futures
  import context.dispatcher
  
  private val singleResults = new Stack[List[TourismProduct]]
  
  private var combinationResults: List[List[ProductCombination]] = Nil
  
  private def query(param: String): List[TourismProduct] = Nil
  
  private def combinations(a: List[TourismProduct], b: List[TourismProduct]): List[ProductCombination] = {
    a.par.flatMap(x => b.map(y => new ProductCombination(List(x, y)))).toList
  }
  
  private def combinationsMultiple(a: List[ProductCombination], b: List[ProductCombination]): List[ProductCombination] = {
    a.par.flatMap(x => b.map(y => new ProductCombination(x.products ::: y.products) )).toList
  }

  def receive = {
    
    case QueryOrder(params) => {
      
        params.foreach { param =>
          
          val f1 = Future { query(param) }
          
          f1.onComplete {
            case Success(xs) => self ! SingleResultReady(xs)
            case Failure(f) => self ! ResultFailed(f)
          }
        }
      }
      
    case SingleResultReady(rs) => {
        
        singleResults.push(rs)
        
        if ( (singleResults.size % 2).equals(0)) {
          
          val combination = Future { combinations(rs, singleResults.pop) }
          
          combination.onComplete {
            case Success(xs) => self ! CombinationResultReady(xs)
            case Failure(f) => self ! ResultFailed(f)
          }
        }
      }
      
    case CombinationResultReady(rs) => {
        
        combinationResults = rs :: combinationResults
        
        if (combinationResults.size == products-1) {
          combinationResults.reduce( (a,b) => combinationsMultiple(a,b))
        }
      }
    
    case ResultFailed(e) => {
        log.info("ResultFailed: " + e.getMessage)
      }
    
  }
  
}
