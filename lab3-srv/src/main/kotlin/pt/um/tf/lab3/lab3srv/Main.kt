package pt.um.tf.lab3.lab3srv

import io.atomix.catalyst.concurrent.SingleThreadContext
import io.atomix.catalyst.serializer.Serializer
import io.atomix.catalyst.transport.Address
import io.atomix.catalyst.transport.Transport
import io.atomix.catalyst.transport.netty.NettyTransport
import pt.haslab.ekit.Spread
import pt.um.tf.lab3.lab3mes.Message
import pt.um.tf.lab3.lab3mes.Reply
import pt.um.tf.lab3.lab3mes.UpdateMessage
import pt.um.tf.lab3.lab3mes.getSHA256
import spread.MembershipInfo
import spread.SpreadGroup
import spread.SpreadMessage
import java.net.ServerSocket
import java.security.SecureRandom
import java.util.concurrent.CompletableFuture
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

    val tlr = SecureRandom()
    val b = ByteArray(512)
    val random = "${tlr.nextBytes(b)}"
    val me = getSHA256(random)
    val t : Transport = NettyTransport()
    val sr = Serializer()
    val tc = SingleThreadContext("srv-%d",sr)
    val acc = Account()
    val spread = Spread("srv-$me", true)
    var quality: Quality = if (s) Quality.first() else Quality.initFollow()
    val known = mutableSetOf<SpreadGroup>()

    lateinit var spreadGroup : SpreadGroup
    lateinit var leaderGroup : SpreadGroup

    fun run() {
        sr.register(Message::class.java)
        sr.register(Reply::class.java)
        sr.register(UpdateMessage::class.java)
        if(quality == Quality.LEADER) {
            leaderGroup = spread.privateGroup
            tc.execute(this::startServer)
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
        known += spread.privateGroup
        spread.open()
        spreadGroup = spread.join("banks")
    }

    private fun startServer() {
        var again = false
        while(again) {
            var c : CompletableFuture<Void> = CompletableFuture()
            tc.execute({
                c = t.server().listen(Address("127.0.0.1", 22556), {
                    it.handler(Message::class.java, this::handlerMessage)
                })
            })
            try {
                c.get()
                again = true
            }
            catch(e : Exception) {
                //Infinite cycle of attempting to server here.
            }
        }
    }

    private fun handlers() {
        LOGGER.log(Level.INFO,"Handling connection")
        spread.handler(spread.MembershipInfo::class.java, {
            _ : SpreadMessage,
            m : spread.MembershipInfo -> handler(m)
        }).handler(UpdateMessage::class.java, {
            sm : SpreadMessage,
            m : UpdateMessage -> handler(sm, m)
        })
    }

    private fun handler(m: MembershipInfo) {
        when (quality) {
            Quality.LEADER -> {
                if (m.isCausedByJoin) {
                    val spm = SpreadMessage()
                    spm.addGroup(m.joined)
                    spm.setSafe()
                    spread.multicast(spm, UpdateMessage(acc.balance()))
                }
            }
            Quality.FOLLOWER -> {
                if (m.isCausedByLeave) {
                    //Assumption that left group is always size 1.
                    if (leaderGroup == m.left) {
                        val smallest : SpreadGroup =
                                known.minBy { it.toString() } ?: spread.privateGroup
                        if(spread.privateGroup.toString() <= smallest.toString()) {
                            quality.rise()
                            //I am the leader
                            //I start the server on the downed port
                            //Might blow up if port is not reusable.
                            startServer()
                            //Leaders can only fail crash
                            known.clear()
                        }
                        else {
                            known.remove(smallest)
                            leaderGroup = smallest
                        }
                    }
                }
                else if (m.isCausedByJoin) {
                    known += m.joined
                }
            }
        }

    }

    private fun handler(sm: SpreadMessage,
                        m: UpdateMessage) {
        when (quality) {
            Quality.LEADER -> {
                if (sm.sender == leaderGroup) {
                    LOGGER.log(Level.INFO, "Leader got update")
                }
                else {
                    LOGGER.log(Level.SEVERE, "Leader got update from follower!!")
                }
            }
            Quality.FOLLOWER -> {
                leaderGroup = sm.sender
                acc.updateBalance(m.accbalance)
            }
        }
    }

    private fun handlerMessage(m : Message) : CompletableFuture<Reply> {
        var r : CompletableFuture<Reply> = CompletableFuture()
        when (m.op) {
            1 -> {
                r = CompletableFuture.completedFuture(
                        Reply(1,
                              acc.movement(m.mov),
                              acc.balance()))

            }
            0 -> {
                r = CompletableFuture.completedFuture(
                        Reply(0,
                              false,
                              acc.balance()))
            }
        }
        val spm = SpreadMessage()
        spm.addGroup(spreadGroup)
        spm.setSafe()
        spread.multicast(spm, UpdateMessage(acc.balance()))
        return r
    }

}



