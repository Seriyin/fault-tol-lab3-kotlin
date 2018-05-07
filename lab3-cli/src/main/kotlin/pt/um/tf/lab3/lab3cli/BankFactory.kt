package pt.um.tf.lab3.lab3cli

import io.atomix.catalyst.concurrent.SingleThreadContext
import io.atomix.catalyst.concurrent.ThreadContext
import io.atomix.catalyst.serializer.Serializer
import pt.haslab.ekit.Spread
import pt.um.tf.lab3.lab3cli.BankStub
import pt.um.tf.lab3.lab3mes.Bank
import pt.um.tf.lab3.lab3mes.Message
import pt.um.tf.lab3.lab3mes.Reply
import java.util.*
import java.util.concurrent.ThreadLocalRandom

class BankFactory {
    private val tlr = ThreadLocalRandom.current()
    private val l = arrayListOf<Pair<ThreadContext,Spread>>()

    fun newBank() : Bank {
        val sr = Serializer()
        val me = UUID(tlr.nextLong(), tlr.nextLong())
        val sp = Spread("cli-$me",false)
        sr.register(Message::class.java)
        sr.register(Reply::class.java)
        val tc : ThreadContext = SingleThreadContext("cli-%d", sr)
        l.add(Pair(tc,sp))
        return BankStub(me, sp, sr, tc)
    }

    fun closeBanks() {
        l.forEach{ (a, b) ->
            a.close()
            b.close()
        }
        l.clear()
    }

}
