package pt.um.tf.lab3.lab3srv


/**
 * Describes typical server flow.
 * If we're solo we must be the leader.
 * This is a very strong assumption but necessary in this simplified model.
 *
 * Election is automatic and elects smallest id.
 *
 * We find others in the group as followers to be notified.
 */
enum class Quality {
    LEADER {
        override fun follow(): Quality = LEADER
        override fun rise(): Quality = LEADER
    },
    FOLLOWER {
        override fun follow(): Quality = FOLLOWER
        override fun rise(): Quality = LEADER
    };

    abstract fun rise() : Quality
    abstract fun follow() : Quality

    companion object {
        fun initFollow() : Quality {
            return Quality.FOLLOWER
        }
        fun first() : Quality {
            return Quality.LEADER
        }
    }
}