package pt.um.tf.lab3.lab3srv

import pt.um.tf.lab3.lab3mes.Bank

class Account(private var balance: Long = 0) : Bank {
    override fun balance(): Long {
        return balance
    }


    override fun movement(mov : Long): Boolean {
        var res = true
        when {
            mov > 0 -> balance += mov
            -mov <= balance -> balance += mov
            else -> res = false
        }
        return res
    }

    fun updateBalance(balance : Long) {
        this.balance = balance
    }
}
