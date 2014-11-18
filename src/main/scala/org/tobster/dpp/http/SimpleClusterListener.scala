package org.tobster.dpp.http

import akka.actor.{Actor, ActorLogging, ActorRef}

import akka.cluster.ClusterEvent.{ClusterDomainEvent, CurrentClusterState,
                                  MemberRemoved, MemberUp, UnreachableMember}

class SimpleClusterListener(httpServer: ActorRef) extends Actor with ActorLogging {
  
  def receive = {
    
    case state: CurrentClusterState =>
      log.info("Current members: {}", state.members.mkString(", "))
      
    case MemberUp(member) => {
        log.info("Member is Up: {}", member.address)
        httpServer ! AddNodeMsg(member.address)
      }
      
    case UnreachableMember(member) =>
      log.info("Member detected as unreachable: {}", member)
      
    case MemberRemoved(member, previousStatus) =>
      log.info("Member is Removed: {} after {}",
               member.address, previousStatus)
      
    case _: ClusterDomainEvent => // ignore
  }
}

