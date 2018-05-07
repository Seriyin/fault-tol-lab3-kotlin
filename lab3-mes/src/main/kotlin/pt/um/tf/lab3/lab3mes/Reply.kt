package pt.um.tf.lab3.lab3mes

import io.atomix.catalyst.buffer.BufferInput
import io.atomix.catalyst.buffer.BufferOutput
import io.atomix.catalyst.serializer.CatalystSerializable
import io.atomix.catalyst.serializer.Serializer

/**
 * Reply to account operation.
 */
data class Reply(var seq : Int = 0,
                 var op : Int = 0,
                 var denied : Boolean = false,
                 var balance : Long = 0)
    : CatalystSerializable {

    override fun readObject(buffer: BufferInput<*>?,
                            serializer: Serializer?) {
        seq = buffer!!.readInt()
        op = buffer.readInt()
        denied = buffer.readBoolean()
        balance = buffer.readLong()
    }

    override fun writeObject(buffer: BufferOutput<*>?,
                             serializer: Serializer?) {
        buffer?.writeInt(seq)
              ?.writeInt(op)
              ?.writeBoolean(denied)
              ?.writeLong(balance)
    }
}
