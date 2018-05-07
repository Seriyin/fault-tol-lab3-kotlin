package pt.um.tf.lab3.lab3srv


/**
 * Describes typical server flow.
 * At first the state of the group is unknown.
 * If we're solo we must be the first server.
 * This is a very strong assumption but necessary in this simplified model.
 * We could not validate whether we're solo reliably.
 *
 * We find others in the group because we're lagging behind.
 * After we know the group is informed of our existence we queue everything.
 *
 * Wait until join receipt and run all queued requests.
 * Only then are we up-to-date.
 */
enum class Phase {
    UNK {
        override fun change(): Phase = WAIT
    },
    WAIT {
        override fun change(): Phase = UP
    },
    UP {
        override fun change(): Phase = UP
    };

    abstract fun change(): Phase

    companion object {
        fun initPhase() : Phase {
            return Phase.UNK
        }
        fun first() : Phase {
            return Phase.UP
        }
    }
}