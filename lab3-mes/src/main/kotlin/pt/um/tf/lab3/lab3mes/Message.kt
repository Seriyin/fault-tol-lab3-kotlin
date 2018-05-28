package pt.um.tf.lab3.lab3mes

import io.atomix.catalyst.buffer.BufferInput
import io.atomix.catalyst.buffer.BufferOutput
import io.atomix.catalyst.serializer.CatalystSerializable
import io.atomix.catalyst.serializer.Serializer
import java.util.*

/**
 * Message with operation.
 */
data class Message(var op : Int = 0,
                   var mov : Long = 0,
                   var origin : String) : CatalystSerializable {
    override fun writeObject(buffer: BufferOutput<*>?, serializer: Serializer?) {
        buffer?.writeInt(op)
              ?.writeLong(mov)
        serializer?.writeObject(origin, buffer)
    }

    override fun readObject(buffer: BufferInput<*>?, serializer: Serializer?) {
        op = buffer!!.readInt()
        mov = buffer.readLong()
        origin = serializer!!.readObject(buffer)
    }
}
