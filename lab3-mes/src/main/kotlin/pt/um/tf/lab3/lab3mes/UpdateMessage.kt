package pt.um.tf.lab3.lab3mes

import io.atomix.catalyst.buffer.BufferInput
import io.atomix.catalyst.buffer.BufferOutput
import io.atomix.catalyst.serializer.CatalystSerializable
import io.atomix.catalyst.serializer.Serializer

data class UpdateMessage(var accbalance : Long) : CatalystSerializable {
    override fun writeObject(buffer: BufferOutput<*>?, serializer: Serializer?) {
        buffer?.writeLong(accbalance)
    }

    override fun readObject(buffer: BufferInput<*>?, serializer: Serializer?) {
        accbalance = buffer!!.readLong()
    }

}
