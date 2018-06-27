package pt.um.tf.lab3.lab3cli

import mu.KLogging
import java.util.concurrent.BlockingQueue

class Spammer(private val i: Int, private val bf: BankFactory, private val q: BlockingQueue<Long>) {
    companion object : KLogging()
    fun execute() {
        logger.info("New spammer $i")
        val b = bf.newBank()
        val rand = (20000..80000).random()
        var balance : Long = 0
        logger.info("$i Spammer will do $rand iterations")
        (0..rand).forEach {
            val mov = (-200..200).random().toLong()
            if (!b.movement(mov)) {
                balance += mov
            }
            else {
                logger.info("Rejected $mov")
            }
        }
        logger.info("$i Spammer finished with $balance")
        q.put(balance)
    }
}
