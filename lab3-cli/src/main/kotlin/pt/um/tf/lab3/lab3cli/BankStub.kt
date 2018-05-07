package pt.um.tf.lab3.lab3cli

import io.atomix.catalyst.concurrent.ThreadContext
import io.atomix.catalyst.serializer.Serializer
import pt.haslab.ekit.Spread
import pt.um.tf.lab3.lab3mes.Bank
import pt.um.tf.lab3.lab3mes.Message
import pt.um.tf.lab3.lab3mes.Reply
import spread.SpreadMessage
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.logging.Level
import java.util.logging.Logger

class BankStub(val me: UUID,
               val sp: Spread,
               val sr: Serializer,
               val tc: ThreadContext) : Bank
{
    companion object {
        val LOGGER : Logger = Logger.getLogger("BankStub")
    }

    private var comFMov : CompletableFuture<Boolean> = CompletableFuture()
    private var comFBal : CompletableFuture<Long> = CompletableFuture()
    private var i : Int = 0

    init {
        sp.open()
        sp.handler(Reply::class.java, {
            sm: SpreadMessage,
            m: Reply -> handle(m)
        })
    }

    override fun movement(mov: Long): Boolean {
        comFMov = CompletableFuture()
        tc.execute {
            sp.multicast(SpreadMessage(), Message(i,1, mov, me))
        }
        return comFMov.get()
    }

    override fun balance(): Long {
        comFBal = CompletableFuture()
        tc.execute {
            sp.multicast(SpreadMessage(), Message(i,0, 0, me))
        }
        return comFBal.get()
    }


    private fun handle(m: Reply) {
        when (m.op) {
            1 -> when {
                m.seq < i -> LOGGER.log(Level.INFO,"Repeat message")
                m.seq == i -> {
                    comFMov.complete(m.denied)
                    i++
                }
                else -> LOGGER.log(Level.SEVERE, "Sequence is ahead of messaging")
            }
            0 -> comFBal.complete(m.balance)
        }
    }

}
