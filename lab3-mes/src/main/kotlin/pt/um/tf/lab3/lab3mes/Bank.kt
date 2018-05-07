package pt.um.tf.lab3.lab3mes

interface Bank {
    fun movement(mov : Long) : Boolean
    fun balance() : Long
}
