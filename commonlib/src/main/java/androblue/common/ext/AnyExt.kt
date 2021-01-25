package androblue.common.ext

val <T : Any> T.className: String
    get() = this::class.java.simpleName