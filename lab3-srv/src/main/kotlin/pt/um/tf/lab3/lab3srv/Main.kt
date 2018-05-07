package pt.um.tf.lab3.lab3srv

import io.atomix.catalyst.concurrent.SingleThreadContext
import io.atomix.catalyst.serializer.Serializer
import pt.haslab.ekit.Spread
import pt.um.tf.lab3.lab3mes.Message
import pt.um.tf.lab3.lab3mes.Reply
import pt.um.tf.lab3.lab3mes.UpdateMessage
import spread.MembershipInfo
import spread.SpreadGroup
import spread.SpreadMessage
import java.util.concurrent.ThreadLocalRandom
import java.util.logging.Level
import java.util.logging.Logger


fun main(args : Array<String>) {
    val main = Main(if (args.size>=0) args[0].toBoolean() else false)
    main.run()
}

private class Main(s: Boolean) {
    companion object {
        val LOGGER : Logger = Logger.getLogger("Main")
    }

    val tlr = ThreadLocalRandom.current()
    val me = "${tlr.nextLong()}${tlr.nextLong()}${tlr.nextLong()}${tlr.nextLong()}"
    val sr = Serializer()
    val tc = SingleThreadContext("srv-%d",sr)
    val acc = Account()
    val spread = Spread("srv-$me", true)
    var quality: Quality = if (s) Quality.first() else Quality.initFollow()
    val known = mutableSetOf<String>()

    lateinit var spreadGroup : SpreadGroup
    lateinit var leaderGroup : SpreadGroup

    fun run() {
        sr.register(Message::class.java)
        sr.register(Reply::class.java)
        sr.register(UpdateMessage::class.java)
        if(quality == Quality.LEADER) {
            leaderGroup = spread.privateGroup
        }
        tc.execute(this::handlers)
          .thenAccept(this::openAndJoin)
        while(readLine() == null);
        spread.leave(spreadGroup)
        spread.close()
        tc.close()
        LOGGER.log(Level.INFO,"I'm here")
    }

    private fun openAndJoin(void : Void) {
        spread.open()
        spreadGroup = spread.join("banks")
    }

    private fun handlers() {
        LOGGER.log(Level.INFO,"Handling connection")
        spread.handler(Message::class.java, {
            sm : SpreadMessage,
            m : Message -> handler(sm, m)
        }).handler(Reply::class.java, {
            sm : SpreadMessage,
            m : Reply -> handler(m)
        }).handler(spread.MembershipInfo::class.java, {
            sm : SpreadMessage,
            m : spread.MembershipInfo -> handler(sm, m)
        }).handler(UpdateMessage::class.java, {
            sm : SpreadMessage,
            m : UpdateMessage -> handler(sm, m)
        })
    }

    private fun handler(m: Reply) {
        LOGGER.log(Level.SEVERE, "Reply : $m")
    }

    private fun handler(sm: SpreadMessage,
                        m: MembershipInfo) {
        when (quality) {
            Quality.LEADER -> {
                if (m.isCausedByJoin()) {
                    val spm = SpreadMessage()
                    spm.addGroup(m.joined)
                    spm.setSafe()
                    spread.multicast(spm, UpdateMessage(acc.balance()))
                }
            }
            Quality.FOLLOWER -> {
                if (m.isCausedByLeave()) {
                    if (leaderGroup == m.left) {
                        val smallest : String = known.min() ?: me
                        if(me <= smallest) {
                            quality.rise()
                            //Leaders can only fail crash
                            known.clear()
                        }
                        else {
                            known.remove(smallest)
                        }
                    }
                }
                else if (m.isCausedByJoin()) {
                    known.add(m.joined.toString())
                }
            }
        }

    }

    private fun handler(sm: SpreadMessage,
                        m: UpdateMessage) {
        when (quality) {
            Quality.LEADER -> {
                LOGGER.log(Level.SEVERE, "Leader got update")
            }
            Quality.FOLLOWER -> {
                leaderGroup = sm.sender
                acc.updateBalance(m.accbalance)
            }
        }
    }

    private fun handler(sm : SpreadMessage,
                        m : Message) {
        when (quality) {
            Quality.LEADER -> {
                val spm = SpreadMessage()
                spm.addGroup(sm.sender)
                spm.setSafe()
                when (m.op) {
                    1 -> {
                        spread.multicast(spm, Reply(m.seq,1, acc.movement(m.mov), acc.balance()))
                    }
                    0 -> {
                        spread.multicast(spm, Reply(m.seq,0, false, acc.balance()))
                    }
                }
            }
            Quality.FOLLOWER -> {
                LOGGER.log(Level.WARNING, "Follower got message")
                //Redirect message to leader
                if (leaderGroup !in sm.groups) {
                    val spm = SpreadMessage()
                    spm.addGroup(leaderGroup)
                    spm.setSafe()
                    spread.multicast(spm, m)
                }
            }
        }
    }

}



