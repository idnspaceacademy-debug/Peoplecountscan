package com.example.utils

import com.example.data.model.MockAppBar
import com.example.data.model.MockElementType
import com.example.data.model.MockSectionType
import com.example.data.model.MockUiElement
import com.example.data.model.MockUiLayout
import com.example.data.model.MockUiSection
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.json.JSONArray
import org.json.JSONObject

/**
 * Utility to parse Gemini layout descriptions (both JSON and textual markdown)
 * and generate a structured MockUiLayout model for rendering mock UI representations.
 */
object SketchLayoutParser {

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    /**
     * Parses the JSON or textual layout description returned by the Gemini API
     * and produces a structured [MockUiLayout] representing mock UI elements.
     */
    fun parseSketchLayoutDescription(rawResponse: String): MockUiLayout {
        val trimmed = rawResponse.trim()
        if (trimmed.isEmpty()) {
            return defaultEmptyLayout("Empty Response")
        }

        // 1. Try parsing JSON directly or from markdown ```json ``` blocks
        val extractedJson = extractJsonContent(trimmed)
        if (extractedJson != null) {
            val jsonLayout = parseJsonLayout(extractedJson, trimmed)
            if (jsonLayout != null && (jsonLayout.sections.isNotEmpty() || jsonLayout.appBar != null)) {
                return jsonLayout
            }
        }

        // 2. Parse structured textual / markdown description
        return parseTextualLayout(trimmed)
    }

    /**
     * Extracts JSON substring from raw text or markdown code fence.
     */
    private fun extractJsonContent(text: String): String? {
        val jsonCodeFenceRegex = Regex("```(?:json)?\\s*([\\s\\S]*?)\\s*```", RegexOption.IGNORE_CASE)
        val match = jsonCodeFenceRegex.find(text)
        if (match != null) {
            val candidate = match.groupValues[1].trim()
            if (candidate.startsWith("{") && candidate.endsWith("}")) {
                return candidate
            }
        }

        val firstBrace = text.indexOf('{')
        val lastBrace = text.lastIndexOf('}')
        if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
            return text.substring(firstBrace, lastBrace + 1)
        }

        return null
    }

    /**
     * Parses a JSON string into [MockUiLayout] using Moshi or manual JSON deserialization fallback.
     */
    private fun parseJsonLayout(jsonStr: String, rawDescription: String): MockUiLayout? {
        return try {
            val adapter = moshi.adapter(MockUiLayout::class.java)
            val parsed = adapter.fromJson(jsonStr)
            if (parsed != null && parsed.sections.isNotEmpty()) {
                parsed.copy(rawDescription = rawDescription)
            } else {
                parseDynamicJsonObject(JSONObject(jsonStr), rawDescription)
            }
        } catch (e: Exception) {
            try {
                parseDynamicJsonObject(JSONObject(jsonStr), rawDescription)
            } catch (e2: Exception) {
                null
            }
        }
    }

    private fun parseDynamicJsonObject(obj: JSONObject, rawDescription: String): MockUiLayout {
        val title = obj.optString("screenTitle", obj.optString("title", "App Mock Screen"))
        val screenType = obj.optString("screenType", obj.optString("type", "Dashboard"))
        val summary = obj.optString("summary", obj.optString("description", ""))

        // App Bar
        var mockAppBar: MockAppBar? = null
        if (obj.has("appBar") || obj.has("topBar") || obj.has("navigation")) {
            val barObj = obj.optJSONObject("appBar") ?: obj.optJSONObject("topBar") ?: obj.optJSONObject("navigation")
            if (barObj != null) {
                val barTitle = barObj.optString("title", title)
                val hasBack = barObj.optBoolean("hasBackAction", barObj.optBoolean("hasBack", false))
                val actionsList = mutableListOf<String>()
                val actionsArr = barObj.optJSONArray("actions")
                if (actionsArr != null) {
                    for (i in 0 until actionsArr.length()) {
                        actionsList.add(actionsArr.optString(i))
                    }
                }
                mockAppBar = MockAppBar(title = barTitle, hasBackAction = hasBack, actions = actionsList)
            }
        }

        // Sections
        val sectionsList = mutableListOf<MockUiSection>()
        val sectionsArr = obj.optJSONArray("sections") ?: obj.optJSONArray("containers") ?: obj.optJSONArray("layout")
        if (sectionsArr != null) {
            for (i in 0 until sectionsArr.length()) {
                val secObj = sectionsArr.optJSONObject(i) ?: continue
                val secTitle = secObj.optString("title", null)
                val secTypeStr = secObj.optString("sectionType", secObj.optString("type", "CARD"))
                val secType = mapSectionType(secTypeStr)

                val elementsList = mutableListOf<MockUiElement>()
                val elementsArr = secObj.optJSONArray("elements") ?: secObj.optJSONArray("items") ?: secObj.optJSONArray("controls")
                if (elementsArr != null) {
                    for (j in 0 until elementsArr.length()) {
                        val elemObj = elementsArr.optJSONObject(j)
                        if (elemObj != null) {
                            val elemTypeStr = elemObj.optString("elementType", elemObj.optString("type", "BODY_TEXT"))
                            val label = elemObj.optString("label", elemObj.optString("text", "Element"))
                            val value = elemObj.optString("value", null)
                            val placeholder = elemObj.optString("placeholder", null)
                            val subText = elemObj.optString("subText", null)
                            val icon = elemObj.optString("icon", null)
                            val isPrimary = elemObj.optBoolean("isPrimary", false)

                            elementsList.add(
                                MockUiElement(
                                    elementType = mapElementType(elemTypeStr),
                                    label = label,
                                    value = value,
                                    placeholder = placeholder,
                                    subText = subText,
                                    icon = icon,
                                    isPrimary = isPrimary
                                )
                            )
                        } else {
                            val strVal = elementsArr.optString(j)
                            if (strVal.isNotBlank()) {
                                elementsList.add(createSmartElementFromText(strVal))
                            }
                        }
                    }
                }

                sectionsList.add(MockUiSection(title = secTitle, sectionType = secType, elements = elementsList))
            }
        }

        return MockUiLayout(
            screenTitle = title,
            screenType = screenType,
            summary = summary,
            appBar = mockAppBar ?: MockAppBar(title = title, hasBackAction = false, actions = listOf("Search", "More")),
            sections = if (sectionsList.isNotEmpty()) sectionsList else defaultSampleSections(),
            rawDescription = rawDescription
        )
    }

    /**
     * Parses standard textual layout descriptions with markdown sections, bullet points,
     * headers and keywords.
     */
    private fun parseTextualLayout(text: String): MockUiLayout {
        val lines = text.lines().map { it.trim() }.filter { it.isNotEmpty() }
        var screenTitle = "UI Mock Screen"
        var screenType = "Application Screen"
        var appBar: MockAppBar? = null
        val sections = mutableListOf<MockUiSection>()

        var currentSectionTitle = "Overview"
        var currentSectionType = MockSectionType.CARD
        val currentElements = mutableListOf<MockUiElement>()

        for (line in lines) {
            val cleanLine = line.removePrefix("#").removePrefix("#").removePrefix("#").trim()

            // Detect Screen Type / Objective
            if (line.contains("Screen Type", ignoreCase = true) || line.contains("Objective", ignoreCase = true) || line.contains("Purpose", ignoreCase = true)) {
                val value = extractColonValue(line)
                if (value.isNotEmpty()) {
                    screenType = value
                    screenTitle = value.split("-", ",", ":").firstOrNull()?.trim() ?: value
                }
                continue
            }

            // Detect Top Navigation / App Bar
            if (line.contains("Top Navigation", ignoreCase = true) || line.contains("App Bar", ignoreCase = true) || line.contains("Header", ignoreCase = true)) {
                val barTitle = extractColonValue(line).ifEmpty { screenTitle }
                val actions = mutableListOf("Search", "Settings")
                if (line.contains("profile", ignoreCase = true)) actions.add("Profile")
                if (line.contains("menu", ignoreCase = true)) actions.add("Menu")
                appBar = MockAppBar(title = barTitle, hasBackAction = true, actions = actions.take(3))
                continue
            }

            // Detect New Section Header
            val isNumberedHeader = line.matches(Regex("^[0-9]+[.)]\\s*.*"))
            val isMarkdownHeader = line.startsWith("#") || line.startsWith("**") && line.endsWith("**")
            val isLayoutContainerHeader = line.contains("Layout Container", ignoreCase = true) ||
                    line.contains("Section", ignoreCase = true) ||
                    line.contains("Card", ignoreCase = true) ||
                    line.contains("Interactive Element", ignoreCase = true) ||
                    line.contains("Compose Layout Hierarchy", ignoreCase = true)

            if ((isNumberedHeader || isMarkdownHeader || isLayoutContainerHeader) && !line.startsWith("-") && !line.startsWith("*")) {
                if (currentElements.isNotEmpty()) {
                    sections.add(MockUiSection(title = currentSectionTitle, sectionType = currentSectionType, elements = currentElements.toList()))
                    currentElements.clear()
                }

                currentSectionTitle = cleanLine.replace(Regex("^[0-9]+[.)]\\s*"), "").replace("*", "").trim()
                currentSectionType = when {
                    currentSectionTitle.contains("Hero", ignoreCase = true) || currentSectionTitle.contains("Metric", ignoreCase = true) || currentSectionTitle.contains("Stat", ignoreCase = true) -> MockSectionType.HERO_METRIC
                    currentSectionTitle.contains("Form", ignoreCase = true) || currentSectionTitle.contains("Input", ignoreCase = true) -> MockSectionType.FORM
                    currentSectionTitle.contains("List", ignoreCase = true) -> MockSectionType.LIST
                    currentSectionTitle.contains("Grid", ignoreCase = true) -> MockSectionType.GRID
                    currentSectionTitle.contains("Action", ignoreCase = true) || currentSectionTitle.contains("Button", ignoreCase = true) -> MockSectionType.ACTION_BUTTONS
                    else -> MockSectionType.CARD
                }
                continue
            }

            // Bullet items or details
            if (line.startsWith("-") || line.startsWith("*") || line.startsWith("•") || isNumberedHeader) {
                val bulletContent = line.replace(Regex("^[-*•0-9.)]+\\s*"), "").replace("**", "").trim()
                if (bulletContent.isNotBlank()) {
                    currentElements.add(createSmartElementFromText(bulletContent))
                }
            } else if (cleanLine.isNotBlank() && cleanLine.length < 80) {
                currentElements.add(createSmartElementFromText(cleanLine))
            }
        }

        if (currentElements.isNotEmpty()) {
            sections.add(MockUiSection(title = currentSectionTitle, sectionType = currentSectionType, elements = currentElements.toList()))
        }

        if (appBar == null) {
            appBar = MockAppBar(title = screenTitle, hasBackAction = false, actions = listOf("Search", "Filter", "More"))
        }

        val finalSections = if (sections.isNotEmpty()) sections else defaultSampleSections()

        return MockUiLayout(
            screenTitle = screenTitle,
            screenType = screenType,
            summary = "Layout parsed from Gemini sketch wireframe analysis.",
            appBar = appBar,
            sections = finalSections,
            rawDescription = text
        )
    }

    private fun extractColonValue(line: String): String {
        val parts = line.split(":", limit = 2)
        return if (parts.size > 1) {
            parts[1].replace("*", "").trim()
        } else {
            ""
        }
    }

    /**
     * Smartly infers MockElementType and attributes from descriptive text tokens.
     */
    private fun createSmartElementFromText(text: String): MockUiElement {
        val lower = text.lowercase()
        return when {
            lower.contains("button") || lower.contains("btn") || lower.contains("submit") || lower.contains("save") || lower.contains("cta") -> {
                val label = text.replace(Regex("(?i)button|btn|cta|primary|action"), "").replace(":", "").trim().ifEmpty { "Action Button" }
                MockUiElement(
                    elementType = MockElementType.BUTTON,
                    label = label,
                    isPrimary = lower.contains("primary") || lower.contains("submit") || lower.contains("save") || lower.contains("main")
                )
            }
            lower.contains("text field") || lower.contains("input") || lower.contains("search bar") || lower.contains("form field") -> {
                val label = text.replace(Regex("(?i)text field|input field|input|search bar|field"), "").replace(":", "").trim().ifEmpty { "Input Field" }
                MockUiElement(
                    elementType = MockElementType.TEXT_FIELD,
                    label = label,
                    placeholder = "Enter $label..."
                )
            }
            lower.contains("toggle") || lower.contains("switch") || lower.contains("checkbox") -> {
                MockUiElement(
                    elementType = MockElementType.TOGGLE,
                    label = text.replace(Regex("(?i)toggle|switch|checkbox"), "").replace(":", "").trim().ifEmpty { "Active Status" },
                    value = "true"
                )
            }
            lower.contains("chip") || lower.contains("tag") || lower.contains("badge") || lower.contains("filter") -> {
                MockUiElement(
                    elementType = MockElementType.CHIP,
                    label = text.replace(Regex("(?i)chip|tag|badge|filter"), "").replace(":", "").trim().ifEmpty { "Filter Tag" }
                )
            }
            lower.contains("metric") || lower.contains("stat") || lower.contains("count") || lower.contains("kpi") || lower.contains("%") -> {
                MockUiElement(
                    elementType = MockElementType.STAT_CARD,
                    label = text.substringBefore(":").trim().ifEmpty { "KPI Metric" },
                    value = text.substringAfter(":", "98.5%").trim(),
                    subText = "+12% vs last period"
                )
            }
            lower.contains("list item") || lower.contains("row") || lower.contains("entry") -> {
                MockUiElement(
                    elementType = MockElementType.LIST_ITEM,
                    label = text.replace(Regex("(?i)list item|row item|entry"), "").replace(":", "").trim().ifEmpty { "List Item Entry" },
                    subText = "Detailed item description"
                )
            }
            lower.contains("progress") || lower.contains("gauge") || lower.contains("bar") -> {
                MockUiElement(
                    elementType = MockElementType.PROGRESS_INDICATOR,
                    label = text.trim(),
                    value = "75%"
                )
            }
            lower.contains("heading") || lower.contains("title") || text.length < 25 -> {
                MockUiElement(
                    elementType = MockElementType.HEADING,
                    label = text.trim()
                )
            }
            else -> {
                MockUiElement(
                    elementType = MockElementType.BODY_TEXT,
                    label = text.trim()
                )
            }
        }
    }

    private fun mapSectionType(typeStr: String): MockSectionType {
        return try {
            MockSectionType.valueOf(typeStr.uppercase())
        } catch (e: Exception) {
            when {
                typeStr.contains("hero", ignoreCase = true) || typeStr.contains("metric", ignoreCase = true) -> MockSectionType.HERO_METRIC
                typeStr.contains("form", ignoreCase = true) || typeStr.contains("input", ignoreCase = true) -> MockSectionType.FORM
                typeStr.contains("list", ignoreCase = true) -> MockSectionType.LIST
                typeStr.contains("grid", ignoreCase = true) -> MockSectionType.GRID
                typeStr.contains("action", ignoreCase = true) -> MockSectionType.ACTION_BUTTONS
                typeStr.contains("header", ignoreCase = true) -> MockSectionType.HEADER
                else -> MockSectionType.CARD
            }
        }
    }

    private fun mapElementType(typeStr: String): MockElementType {
        return try {
            MockElementType.valueOf(typeStr.uppercase())
        } catch (e: Exception) {
            when {
                typeStr.contains("button", ignoreCase = true) -> MockElementType.BUTTON
                typeStr.contains("input", ignoreCase = true) || typeStr.contains("field", ignoreCase = true) -> MockElementType.TEXT_FIELD
                typeStr.contains("stat", ignoreCase = true) || typeStr.contains("metric", ignoreCase = true) -> MockElementType.STAT_CARD
                typeStr.contains("chip", ignoreCase = true) || typeStr.contains("tag", ignoreCase = true) -> MockElementType.CHIP
                typeStr.contains("toggle", ignoreCase = true) || typeStr.contains("switch", ignoreCase = true) -> MockElementType.TOGGLE
                typeStr.contains("list", ignoreCase = true) -> MockElementType.LIST_ITEM
                typeStr.contains("heading", ignoreCase = true) -> MockElementType.HEADING
                else -> MockElementType.BODY_TEXT
            }
        }
    }

    private fun defaultSampleSections(): List<MockUiSection> {
        return listOf(
            MockUiSection(
                title = "Overview & Key Metrics",
                sectionType = MockSectionType.HERO_METRIC,
                elements = listOf(
                    MockUiElement(MockElementType.STAT_CARD, label = "Active Capacity", value = "84%", subText = "Optimal state", isPrimary = true),
                    MockUiElement(MockElementType.STAT_CARD, label = "Total Visitors", value = "1,420", subText = "+8.4% today")
                )
            ),
            MockUiSection(
                title = "Interactive Actions & Controls",
                sectionType = MockSectionType.FORM,
                elements = listOf(
                    MockUiElement(MockElementType.TEXT_FIELD, label = "Search Records", placeholder = "Type to search..."),
                    MockUiElement(MockElementType.TOGGLE, label = "AI Detection Real-time", value = "true"),
                    MockUiElement(MockElementType.BUTTON, label = "Generate Report", isPrimary = true),
                    MockUiElement(MockElementType.BUTTON, label = "Export Data")
                )
            ),
            MockUiSection(
                title = "Recent Events List",
                sectionType = MockSectionType.LIST,
                elements = listOf(
                    MockUiElement(MockElementType.LIST_ITEM, label = "Zone A • Gate 1 Traffic", subText = "12 entrants recorded"),
                    MockUiElement(MockElementType.LIST_ITEM, label = "Zone B • Hallway Monitoring", subText = "Peak movement detected")
                )
            )
        )
    }

    /**
     * Serializes a [MockUiLayout] object into a JSON string.
     */
    fun layoutToJson(layout: MockUiLayout): String {
        return try {
            val adapter = moshi.adapter(MockUiLayout::class.java)
            adapter.toJson(layout)
        } catch (e: Exception) {
            // Fallback manual JSON generator
            val sectionsArray = JSONArray()
            for (section in layout.sections) {
                val secObj = JSONObject()
                secObj.put("title", section.title ?: "")
                secObj.put("sectionType", section.sectionType.name)
                val elemArray = JSONArray()
                for (elem in section.elements) {
                    val elObj = JSONObject()
                    elObj.put("elementType", elem.elementType.name)
                    elObj.put("label", elem.label)
                    elem.value?.let { elObj.put("value", it) }
                    elem.placeholder?.let { elObj.put("placeholder", it) }
                    elem.subText?.let { elObj.put("subText", it) }
                    elObj.put("isPrimary", elem.isPrimary)
                    elemArray.put(elObj)
                }
                secObj.put("elements", elemArray)
                sectionsArray.put(secObj)
            }

            val root = JSONObject()
            root.put("screenTitle", layout.screenTitle)
            root.put("screenType", layout.screenType)
            root.put("summary", layout.summary)
            root.put("rawDescription", layout.rawDescription)
            root.put("sections", sectionsArray)
            root.toString(2)
        }
    }

    /**
     * Reconstructs a [MockUiLayout] from a JSON string or falls back to parsing.
     */
    fun jsonToLayout(json: String): MockUiLayout {
        return parseSketchLayoutDescription(json)
    }

    fun countElements(layout: MockUiLayout): Int {
        return layout.sections.sumOf { it.elements.size }
    }

    private fun defaultEmptyLayout(reason: String): MockUiLayout {
        return MockUiLayout(
            screenTitle = "Mock Wireframe Screen",
            screenType = "Dashboard",
            summary = reason,
            appBar = MockAppBar("Mock Preview", hasBackAction = true, actions = listOf("More")),
            sections = defaultSampleSections(),
            rawDescription = reason
        )
    }
}
