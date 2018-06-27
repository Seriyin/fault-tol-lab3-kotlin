package pt.um.tf.lab3.lab3srv

import io.atomix.catalyst.concurrent.SingleThreadContext
import io.atomix.catalyst.concurrent.ThreadContext
import io.atomix.catalyst.serializer.Serializer
import io.atomix.catalyst.transport.Address
import io.atomix.catalyst.transport.Transport
import io.atomix.catalyst.transport.netty.NettyTransport
import mu.KLogging
import pt.haslab.ekit.Spread
import pt.um.tf.lab3.lab3mes.Message
import pt.um.tf.lab3.lab3mes.Reply
import pt.um.tf.lab3.lab3mes.UpdateMessage
import spread.MembershipInfo
import spread.SpreadGroup
import spread.SpreadMessage
import java.util.*
import java.util.concurrent.CompletableFuture


fun main(args : Array<String>) {
    val main = Main(if (args.isNotEmpty()) args[0].toBoolean() else false)
    main.run()
}

private class Main(s: Boolean) {
    companion object : KLogging()

    val me : UUID = UUID.randomUUID()
    val t : Transport = NettyTransport()
    val sr = Serializer()
    val spreadtc = SingleThreadContext("ssrv-%d",sr)
    var conntc : ThreadContext? = null
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
        spreadtc.execute(this::openAndJoin)
          .thenRun(this::handlers)
          .exceptionally {
            logger.info("", it)
            return@exceptionally null
        }
        if(quality == Quality.LEADER) {
            startServer()
        }
        while(readLine() == null);
        spread.leave(spreadGroup)
        spread.close()
        t.close()
        spreadtc.close()
        if(quality == Quality.LEADER) {
            conntc?.close()
        }
        logger.info("I'm here")
    }

    private fun openAndJoin() {
        spread.open().get()
        spread.join("banks")
    }

    private fun startServer() {
        conntc = SingleThreadContext("csrv-%d", sr)
        var again = true
        while(again) {
            var c : CompletableFuture<Void> = CompletableFuture()
            conntc?.execute {
                c = t.server().listen(Address("127.0.0.1", 22556)) {
                    it.handler(Message::class.java, this::handlerMessage)
                }
                logger.info("Server started at $c")
            }?.join()
            try {
                c.get()
                again = false
            }
            catch(e : Exception) {
                //Infinite cycle of attempting to server here.
            }
        }
    }

    private fun handlers() {
        logger.info("Handling connection")
        spread.handler(spread.MembershipInfo::class.java, this@Main::handler)
              .handler(UpdateMessage::class.java, this@Main::handler)
    }

    private fun handler(sm : SpreadMessage, m: MembershipInfo) {
        when (quality) {
            Quality.LEADER -> {
                if (m.isCausedByJoin) {
                    if(m.joined == spread.privateGroup) {
                        leaderGroup = spread.privateGroup
                        spreadGroup = m.group
                        logger.info("$me joined group : $spreadGroup")
                    }
                    else {
                        val spm = SpreadMessage()
                        spm.addGroup(m.joined)
                        spm.setSafe()
                        spread.multicast(spm, UpdateMessage(acc.balance()))
                        logger.info("${m.group} joined: ${m.joined}")
                    }
                    known += m.joined
                }
                else if (m.isCausedByLeave) {
                    logger.info("${m.group} left: ${m.left}")
                    known -= m.left
                }
            }
            Quality.FOLLOWER -> {
                if (m.isCausedByLeave) {
                    //Assumption that left group is always size 1.
                    if (leaderGroup == m.left) {
                        val smallest = known.minBy { it.toString() } ?: spread.privateGroup
                        if(spread.privateGroup.toString() <= smallest.toString()) {
                            quality.rise()
                            //I am the leader
                            //I start the server on the downed port
                            //Might blow up if port is not reusable.
                            startServer()
                            //Leaders can only fail crash
                        }
                        known -= m.left
                        leaderGroup = smallest
                        logger.info { "New leader : $smallest" }
                    }
                    logger.info("${m.group} left: ${m.left}")
                }
                else if (m.isCausedByJoin) {
                    if(m.joined == spread.privateGroup) {
                        spreadGroup = m.group
                    }
                    known += m.joined
                    logger.info("${m.group} joined: ${m.joined}")
                }
            }
        }
    }

    private fun handler(sm: SpreadMessage,
                        m: UpdateMessage) {
        when (quality) {
            Quality.LEADER -> {
                if (sm.sender == leaderGroup) {
                    logger.info("Leader got update")
                }
                else {
                    logger.error("Leader got update from follower!!")
                }
            }
            Quality.FOLLOWER -> {
                leaderGroup = sm.sender
                acc.updateBalance(m.accbalance)
                logger.info { "Update to ${m.accbalance}" }
            }
        }
    }

    private fun handlerMessage(m : Message) : CompletableFuture<Reply> {
        val r : CompletableFuture<Reply>
        r = when (m.op) {
            1 -> {
                //logger.info("Movement of ${m.mov}")
                val rp = Reply(1, !acc.movement(m.mov), m.mov)
                CompletableFuture.completedFuture(rp)
            }
            0 -> {
                logger.info("Balance of ${acc.balance()}")
                val rp = Reply(0, false, acc.balance())
                CompletableFuture.completedFuture(rp)
            }
            else -> {
                CompletableFuture.failedFuture(InputMismatchException())
            }
        }
        val spm = SpreadMessage()
        spm.addGroup(spreadGroup)
        spm.setSafe()
        spread.multicast(spm, UpdateMessage(acc.balance()))
        return r
    }

}



