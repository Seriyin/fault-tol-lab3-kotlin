package pt.um.tf.lab3.lab3cli

import io.atomix.catalyst.concurrent.ThreadContext
import io.atomix.catalyst.transport.Address
import io.atomix.catalyst.transport.Connection
import io.atomix.catalyst.transport.Transport
import pt.haslab.ekit.Spread
import pt.um.tf.lab3.lab3mes.Bank
import pt.um.tf.lab3.lab3mes.Message
import pt.um.tf.lab3.lab3mes.Reply
import spread.SpreadMessage
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.logging.Level
import java.util.logging.Logger

class BankStub(val me: Address,
               val t: Transport,
               val tc: ThreadContext) : Bank
{
    companion object {
        val LOGGER : Logger = Logger.getLogger("BankStub")
    }

    private var conn : Connection? = null
    private var comFMov : CompletableFuture<Boolean> = CompletableFuture()
    private var comFBal : CompletableFuture<Long> = CompletableFuture()

    init {
        tryRestart()
    }

    private fun tryRestart() {
        while(conn == null) {
            var comFConn : CompletableFuture<Connection> = CompletableFuture()
            tc.execute({
                comFConn = t.client().connect(me)
            })
            conn = try {
                comFConn.get()
            } catch (e : Exception) {
                //Infinite cycle of attempting connection here.
                null
            }
        }
    }

    override fun movement(mov: Long): Boolean {
        var res : Boolean
        comFMov = CompletableFuture()
        tc.execute {
            comFMov = conn!!.sendAndReceive<Message, Reply>(Message(1, mov, me.host()))
                            .handle({
                r, e -> if (e != null) throw e else r.denied
            })
        }
        try {
            res = comFMov.get()
        }
        catch (e : Exception) {
            res = false
            tryRestart()
        }
        return res
    }

    override fun balance(): Long {
        var res : Long
        comFBal = CompletableFuture()
        tc.execute {
            comFBal = conn!!.sendAndReceive<Message, Reply>(
                    Message(0, 0, me.host()))
                            .handle({
                r, e -> if (e != null) throw e else r.balance
            })
        }
        try {
            res = comFBal.get()
        }
        catch (e : Exception) {
            res = -1
            tryRestart()
        }
        return res
    }

}
