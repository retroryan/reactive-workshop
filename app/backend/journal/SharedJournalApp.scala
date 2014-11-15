/*
* Copyright © 2014 Typesafe, Inc. All rights reserved.
*/

package backend.journal

import akka.actor._
import akka.cluster.{Cluster, Member}
import akka.cluster.ClusterEvent.InitialStateAsEvents
import akka.persistence.journal.leveldb.{SharedLeveldbJournal, SharedLeveldbStore}
import scala.concurrent.duration.DurationInt
import akka.actor.RootActorPath
import akka.cluster.ClusterEvent.MemberUp
import akka.actor.ActorIdentity
import scala.Some
import akka.actor.Identify
import backend.BaseApp
import actors.Settings

object SharedJournal {

    val name: String =
        "shared-journal"

    def pathFor(address: Address): ActorPath =
        RootActorPath(address) / "user" / name
}

/**
 * The shared journal is a single point of failure and must not be used in production.
 * This app must be running in order for persistence and cluster sharding to work.
 */
object SharedJournalApp extends BaseApp {

    override protected def initialize(system: ActorSystem, settings: Settings): Unit = {
        println(s"### SharedJournalApp initialize")
        val sharedJournal = system.actorOf(Props(new SharedLeveldbStore), SharedJournal.name)
        SharedLeveldbJournal.setStore(sharedJournal, system)
    }

}

object SharedJournalSetter {

    val name: String =
        "shared-journal-setter"

    def props: Props =
        Props(new SharedJournalSetter)
}

/**
 * This actor must be started and registered as a cluster event listener by all actor systems
 * that need to use the shared journal, e.g. in order to use persistence or cluster sharding.
 */
class SharedJournalSetter extends Actor with ActorLogging {

    override def preStart(): Unit =
        Cluster(context.system).subscribe(self, InitialStateAsEvents, classOf[MemberUp])

    override def receive: Receive =
        waiting

    private def waiting: Receive = {
        case MemberUp(member) if member hasRole SharedJournal.name => onSharedJournalMemberUp(member)
    }

    private def becomeIdentifying(): Unit = {
        context.setReceiveTimeout(10 seconds)
        context become identifying
    }

    private def identifying: Receive = {
        case ActorIdentity(_, Some(sharedJournal)) =>
            SharedLeveldbJournal.setStore(sharedJournal, context.system)
            log.info("### Successfully set shared journal {}", sharedJournal)
            context.stop(self)
        case ActorIdentity(_, None) =>
            log.error("### Can't identify shared journal!")
            context.stop(self)
        case ReceiveTimeout =>
            log.error("### Timeout identifying shared journal!")
            context.stop(self)
    }

    private def onSharedJournalMemberUp(member: Member): Unit = {
        val sharedJournal = context actorSelection SharedJournal.pathFor(member.address)
        log.info(s"### Identify of member address ${member.address}")
        sharedJournal ! Identify(None)
        becomeIdentifying()
    }
}
