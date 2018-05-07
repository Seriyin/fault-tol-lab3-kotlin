package pt.um.tf.lab3.lab3srv

import pt.um.tf.lab3.lab3mes.Bank

class Account : Bank {
    override fun balance(): Long {
        return balance
    }

    private var balance : Long = 0

    override fun movement(mov : Long): Boolean {
        var res = true
        if (mov > 0) {
            balance += mov
        }
        else {
            if (-mov > balance) {
                res = false
            }
            else {
                balance -= -mov
            }
        }
        return res
    }

    internal fun updateBalance(balance : Long) {
        this.balance = balance
    }
}
