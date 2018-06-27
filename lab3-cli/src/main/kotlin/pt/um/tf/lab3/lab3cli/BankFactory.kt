package pt.um.tf.lab3.lab3cli

import io.atomix.catalyst.concurrent.SingleThreadContext
import io.atomix.catalyst.concurrent.ThreadContext
import io.atomix.catalyst.serializer.Serializer
import io.atomix.catalyst.transport.Address
import io.atomix.catalyst.transport.netty.NettyTransport
import pt.haslab.ekit.Spread
import pt.um.tf.lab3.lab3mes.Bank
import pt.um.tf.lab3.lab3mes.Message
import pt.um.tf.lab3.lab3mes.Reply
import java.util.*
import java.util.concurrent.ThreadLocalRandom

class BankFactory {
    private val me = Address("127.0.0.1", 22556)
    private val l = mutableListOf<ThreadContext>()
    private val t = NettyTransport()

    fun newBank() : Bank {
        val sr = Serializer()
        sr.register(Message::class.java)
        sr.register(Reply::class.java)
        val tc : ThreadContext = SingleThreadContext("cli-%d", sr)
        l.add(tc)
        return BankStub(me, t, tc)
    }

    fun closeBanks() {
        t.close()
        l.forEach(ThreadContext::close)
        l.clear()
    }

}
