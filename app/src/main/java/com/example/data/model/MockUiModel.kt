package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * High-level layout representation parsed from Gemini's sketch analysis.
 */
@JsonClass(generateAdapter = true)
data class MockUiLayout(
    val screenTitle: String = "Mock Screen",
    val screenType: String = "Dashboard",
    val summary: String = "",
    val appBar: MockAppBar? = null,
    val sections: List<MockUiSection> = emptyList(),
    val rawDescription: String = ""
)

@JsonClass(generateAdapter = true)
data class MockAppBar(
    val title: String = "App Screen",
    val hasBackAction: Boolean = false,
    val actions: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class MockUiSection(
    val title: String? = null,
    val sectionType: MockSectionType = MockSectionType.CARD,
    val elements: List<MockUiElement> = emptyList()
)

enum class MockSectionType {
    HEADER,
    HERO_METRIC,
    CARD,
    FORM,
    LIST,
    GRID,
    ACTION_BUTTONS,
    FOOTER
}

@JsonClass(generateAdapter = true)
data class MockUiElement(
    val elementType: MockElementType = MockElementType.BODY_TEXT,
    val label: String = "",
    val value: String? = null,
    val placeholder: String? = null,
    val subText: String? = null,
    val icon: String? = null,
    val isPrimary: Boolean = false
)

enum class MockElementType {
    HEADING,
    SUBHEADING,
    BODY_TEXT,
    BUTTON,
    TEXT_FIELD,
    STAT_CARD,
    CHIP,
    TOGGLE,
    LIST_ITEM,
    ICON_BUTTON,
    IMAGE_PLACEHOLDER,
    DIVIDER,
    PROGRESS_INDICATOR
}
