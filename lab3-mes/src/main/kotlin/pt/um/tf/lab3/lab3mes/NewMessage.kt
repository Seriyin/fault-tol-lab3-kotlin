package pt.um.tf.lab3.lab3mes

import io.atomix.catalyst.buffer.BufferInput
import io.atomix.catalyst.buffer.BufferOutput
import io.atomix.catalyst.serializer.CatalystSerializable
import io.atomix.catalyst.serializer.Serializer
import java.util.*

/**
 * NewMessage comes from source wishing to join the Clique.
 */
data class NewMessage(var origin : UUID = UUID(0,0)) : CatalystSerializable {
    override fun writeObject(buffer: BufferOutput<*>?,
                             serializer: Serializer?) {
        serializer?.writeObject(origin)
    }

    override fun readObject(buffer: BufferInput<*>?, serializer: Serializer?) {
        origin = serializer!!.readObject<UUID>(buffer)
    }
}
