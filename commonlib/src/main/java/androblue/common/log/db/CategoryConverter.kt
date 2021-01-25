package androblue.common.log.db

import androblue.common.log.Category
import androidx.room.TypeConverter

class CategoryConverter {

    @TypeConverter
    fun toCategory(name: String): Category {
        return Category.valueOf(name)
    }

    @TypeConverter
    fun toName(category: Category): String {
        return category.name
    }
}