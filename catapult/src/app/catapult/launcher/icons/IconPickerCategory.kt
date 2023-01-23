package app.catapult.launcher.icons

data class IconPickerCategory(
    val title: String,
    val items: List<IconPickerItem>
) {
    fun filter(searchQuery: String): IconPickerCategory {
        return IconPickerCategory(
            title = title,
            items = items.filter {
                it.label.lowercase().contains(searchQuery.lowercase())
            }
        )
    }
}
