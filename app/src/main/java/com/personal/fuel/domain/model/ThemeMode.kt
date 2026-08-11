package com.personal.fuel.domain.model

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK;

    companion object {
        fun fromStorage(value: String?): ThemeMode =
            entries.firstOrNull { it.name == value } ?: DARK
    }
}

/** User preferences that affect how the app is drawn. */
data class AppearanceSettings(
    val themeMode: ThemeMode = ThemeMode.DARK,
    val dynamicColor: Boolean = false,
)
