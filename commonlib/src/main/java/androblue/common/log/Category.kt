package androblue.common.log

enum class Category constructor(val logcatTag: String) {
    NONE("NONE"),
    LIFECYCLE("LIFECYCLE"),
    NETWORK("NETWORK"),
    NOTIFICATION("NOTIFICATION"),
    CLEANUP("CLEANUP")
}