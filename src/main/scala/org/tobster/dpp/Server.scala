package org.tobster.dpp

import akka.actor.{Actor, ActorSystem, DeadLetter, Props}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import org.tobster.dpp.datamodel.FlightLookup
import org.tobster.dpp.http.{MyHttpServer, SimpleClusterListener}

/*
 * Start server with (for example): <ipaddr>|<port>|<master>|<slaves>|<totalPkgs>|<maxResPkgs>
 *
 * mvn scala:run -DmainClass=org.tobster.dpp.Server -DaddArgs="127.0.0.1|12555|true|2|50|50"                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            * mvn scala:run -DmainClass=org.tobster.dpp.Server -DaddArgs="127.0.0.1|12555|true|2"
 * 
 */
object Server extends App {
  
  assert(
    args.length == 6, 
    "PLEASE SPECIFY: <ipaddr> <port> <master> <slaves> <totalPkgs> <maxResPkgs>"
  )
  
  /*
   * Read command-line arguments
   */
  private val ipAddr = args(0)
  
  // Port for cluster
  System.setProperty("akka.remote.netty.tcp.port", args(1))
  
  // Command-line argument that specifies, if this node is a master or a slave.
  private val isMaster: Boolean = args(2).toBoolean
  
  // Master-Node 'waits' for maxSlaves to join the cluster
  private val maxSlaves: Int = args(3).toInt
  
  private val totalPackages: Int = args(4).toInt
  
  private val maxResultPackages: Int = args(5).toInt

  println("Total flights: " + FlightLookup.totalFlights)
  
  println("Total origins: " + FlightLookup.totalOrigins)
  
  private val system = ActorSystem("dpp")
  
  /*
   * Debug listener
   */
  private val debugListener = system.actorOf(Props(new Actor {
        def receive = {
          case d: DeadLetter => println(d)
        }
      }))
  
  system.eventStream.subscribe(debugListener, classOf[DeadLetter])
  
  /*
   * Init and start HTTP server
   */
  private val port = 19999
  
  private val httpServer = system.actorOf(
    MyHttpServer.props(
      ipAddr, port, isMaster, maxSlaves, totalPackages, maxResultPackages
    ), "httpServer")
  
  /*
   * Cluster listener
   */
  private val clusterListener = system.actorOf(Props(classOf[SimpleClusterListener], httpServer))
  
  Cluster(system).subscribe(clusterListener, classOf[ClusterDomainEvent])
  
}