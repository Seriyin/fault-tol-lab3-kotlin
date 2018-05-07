package pt.um.tf.lab3.lab3srv

import io.atomix.catalyst.concurrent.SingleThreadContext
import io.atomix.catalyst.serializer.Serializer
import pt.haslab.ekit.Spread
import pt.um.tf.lab3.lab3mes.Message
import pt.um.tf.lab3.lab3mes.NewMessage
import pt.um.tf.lab3.lab3mes.Reply
import pt.um.tf.lab3.lab3mes.UpdateMessage
import spread.SpreadGroup
import spread.SpreadMessage
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import java.util.logging.Level
import java.util.logging.Logger


fun main(args : Array<String>) {
    val main = Main(if (args.size>=0) args[0].toBoolean() else false)
    main.run()
}

class Main(s: Boolean) {
    companion object {
        val LOGGER : Logger = Logger.getLogger("Main")
        val INIT_SPACE : Int = 2048
    }

    val tlr = ThreadLocalRandom.current()
    val me = UUID(tlr.nextLong(), tlr.nextLong())
    val sr = Serializer()
    val tc = SingleThreadContext("srv-%d",sr)
    val acc = Account()
    val spread = Spread("srv-$me", false)
    var phase: Phase = if (s) Phase.first() else Phase.initPhase()
    val waitQueue : Queue<Message> by lazy {
        ArrayDeque<Message>(INIT_SPACE)
    }
    lateinit var spreadGroup : SpreadGroup

    fun run() {
        sr.register(Message::class.java)
        sr.register(Reply::class.java)
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
        }).handler(NewMessage::class.java, {
            sm : SpreadMessage,
            m : NewMessage -> handler(sm, m)
        }).handler(UpdateMessage::class.java, {
            sm : SpreadMessage,
            m : UpdateMessage -> handler(m)
        })
    }

    private fun handler(m: Reply) {
        LOGGER.log(Level.SEVERE, "Reply : $m")
    }

    private fun handler(sm: SpreadMessage,
                        m: NewMessage) {
        when (phase) {
            Phase.UP -> {
                val spm = SpreadMessage()
                spm.addGroup(sm.sender)
                spm.setCausal()
                spread.multicast(spm, UpdateMessage(acc.balance()))
            }
            Phase.UNK -> {
                if (m.origin == me)
                    phase.change()
            }
            Phase.WAIT -> {}
        }

    }

    private fun handler(m: UpdateMessage) {
        when (phase) {
            Phase.WAIT -> {
                acc.movement(m.accbalance)
                for (item in waitQueue) {
                    if(item.op == 1) {
                        acc.movement(item.mov)
                    }
                }
                waitQueue.clear()
                phase.change()
            }
            Phase.UP -> {}
            Phase.UNK -> {}
        }
    }

    private fun handler(sm : SpreadMessage,
                        m : Message) {
        when (phase) {
            Phase.WAIT -> {
                waitQueue.add(m)
            }
            Phase.UP -> {
                val spm = SpreadMessage()
                spm.addGroup(sm.sender)
                when (m.op) {
                    1 -> {
                        spread.multicast(spm, Reply(m.seq,1, acc.movement(m.mov), acc.balance()))
                    }
                    0 -> {
                        spread.multicast(spm, Reply(m.seq,0, false, acc.balance()))
                    }
                }
            }
            Phase.UNK -> {}
        }
    }

}



