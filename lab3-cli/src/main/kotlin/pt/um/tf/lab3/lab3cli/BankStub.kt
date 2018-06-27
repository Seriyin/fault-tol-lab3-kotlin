package pt.um.tf.lab3.lab3cli

import io.atomix.catalyst.concurrent.ThreadContext
import io.atomix.catalyst.transport.Address
import io.atomix.catalyst.transport.Connection
import io.atomix.catalyst.transport.Transport
import mu.KLogging
import pt.um.tf.lab3.lab3mes.Bank
import pt.um.tf.lab3.lab3mes.Message
import pt.um.tf.lab3.lab3mes.Reply
import java.util.concurrent.CompletableFuture

class BankStub(private val me: Address,
               private val t: Transport,
               private val tc: ThreadContext) : Bank
{
    companion object : KLogging()

    private var comFMov : CompletableFuture<Boolean> = CompletableFuture()
    private var comFBal : CompletableFuture<Long> = CompletableFuture()
    private var conn : Connection? = null
    private var comFConn : CompletableFuture<Connection> = CompletableFuture()


    init {
        tryRestart()
    }

    private fun tryRestart() {
        tc.execute(this@BankStub::connect).exceptionally {
            logger.error("Try Restart", it)
            tryRestart()
            return@exceptionally null
        }.get()
        conn = comFConn.get()
    }

    private fun connect() {
        comFConn = t.client().connect(me)
    }


    override fun movement(mov: Long): Boolean {
        tc.execute {
            val m = Message(1, mov, me.host())
            comFMov = conn!!.sendAndReceive<Message, Reply>(m)
                            .thenApply(Reply::denied)
        }.get()
        return comFMov.exceptionally {
            logger.error("Failed on Moving $mov", it)
            tryRestart()
            return@exceptionally movement(mov)
        }.get()
    }

    override fun balance(): Long {
        tc.execute {
            val m = Message(0, 0, me.host())
            comFBal = conn!!.sendAndReceive<Message, Reply>(m)
                            .thenApply(Reply::balance)
        }.get()
        return comFBal.exceptionally {
            logger.error("Failed on Balance", it)
            tryRestart()
            return@exceptionally balance()
        }.get()
    }

}
