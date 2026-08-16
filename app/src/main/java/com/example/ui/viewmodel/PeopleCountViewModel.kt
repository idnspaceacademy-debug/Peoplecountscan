package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.PeopleCountDatabase
import com.example.data.model.AlertLevel
import com.example.data.model.AlertNotification
import com.example.data.model.AppSettings
import com.example.data.model.DailySummary
import com.example.data.model.EventType
import com.example.data.model.HourlyRecord
import com.example.data.model.PeakHourItem
import com.example.data.model.PdfExportConfig
import com.example.data.model.TrackingEvent
import com.example.data.repository.PeopleCountRepository
import com.example.utils.PdfReportGenerator
import com.example.utils.SampleDataProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import kotlin.random.Random

data class LiveUiState(
    val currentCount: Int = 72,
    val todayMasuk: Int = 358,
    val todayKeluar: Int = 286,
    val capacityLimit: Int = 100,
    val alertLevel: AlertLevel = AlertLevel.SAFE,
    val isSimulating: Boolean = true,
    val isCameraActive: Boolean = false,
    val selectedSubTab: Int = 0, // 0: Ringkasan, 1: Tren Harian, 2: Peak Hour
    val selectedDate: String = SampleDataProvider.DEFAULT_DATE,
    val displayDate: String = SampleDataProvider.DEFAULT_DISPLAY_DATE,
    val showNotificationsDialog: Boolean = false,
    val showExportPdfDialog: Boolean = false,
    val showSketchGalleryDialog: Boolean = false,
    val selectedGallerySketch: com.example.data.model.ProcessedSketchEntity? = null,
    val exportedPdfFile: File? = null
)

class PeopleCountViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PeopleCountRepository
    private var simulationJob: Job? = null

    private val _uiState = MutableStateFlow(LiveUiState())
    val uiState: StateFlow<LiveUiState> = _uiState.asStateFlow()

    init {
        val db = PeopleCountDatabase.getDatabase(application, viewModelScope)
        repository = PeopleCountRepository(db.peopleCountDao(), db.processedSketchDao())

        // Initial setup
        viewModelScope.launch {
            repository.resetToSampleData()
        }

        startLiveSimulation()
    }

    val alerts: StateFlow<List<AlertNotification>> = repository.allAlerts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SampleDataProvider.getInitialAlerts())

    val unreadAlertCount: StateFlow<Int> = repository.unreadAlertCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 2)

    val dailySummary: StateFlow<DailySummary> = repository.getDailySummary(SampleDataProvider.DEFAULT_DATE)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SampleDataProvider.getInitialDailySummary())

    val hourlyRecords: StateFlow<List<HourlyRecord>> = repository.getHourlyRecords(SampleDataProvider.DEFAULT_DATE)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SampleDataProvider.getInitialHourlyRecords())

    val recentEvents: StateFlow<List<TrackingEvent>> = repository.recentEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SampleDataProvider.getInitialEvents())

    val appSettings: StateFlow<AppSettings> = repository.appSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SampleDataProvider.getDefaultSettings())

    val processedSketches: StateFlow<List<com.example.data.model.ProcessedSketchEntity>> = repository.allSketches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SampleDataProvider.getInitialProcessedSketches())

    fun toggleSketchGallery(show: Boolean) {
        _uiState.update { it.copy(showSketchGalleryDialog = show) }
    }

    fun selectGallerySketch(sketch: com.example.data.model.ProcessedSketchEntity?) {
        _uiState.update { it.copy(selectedGallerySketch = sketch) }
    }

    fun saveProcessedSketch(
        layout: com.example.data.model.MockUiLayout,
        rawDescription: String = layout.rawDescription,
        photoFile: File? = null,
        customTitle: String? = null,
        tags: String = "Mobile, AI",
        onSaved: ((Long) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val id = repository.saveProcessedSketch(
                layout = layout,
                rawDescription = rawDescription,
                photoFile = photoFile,
                customTitle = customTitle,
                tags = tags
            )
            onSaved?.invoke(id)
        }
    }

    fun toggleSketchFavorite(id: Long, isFavorite: Boolean) {
        viewModelScope.launch {
            repository.toggleSketchFavorite(id, isFavorite)
        }
    }

    fun updateSketchMetadata(id: Long, title: String, tags: String, notes: String) {
        viewModelScope.launch {
            repository.updateSketchMetadata(id, title, tags, notes)
        }
    }

    fun deleteSketch(id: Long) {
        viewModelScope.launch {
            repository.deleteProcessedSketch(id)
            if (_uiState.value.selectedGallerySketch?.id == id) {
                _uiState.update { it.copy(selectedGallerySketch = null) }
            }
        }
    }

    fun duplicateSketch(id: Long) {
        viewModelScope.launch {
            repository.duplicateSketch(id)
        }
    }

    fun selectAnalyticsSubTab(tabIndex: Int) {
        _uiState.update { it.copy(selectedSubTab = tabIndex) }
    }

    fun toggleNotificationsDialog(show: Boolean) {
        _uiState.update { it.copy(showNotificationsDialog = show) }
        if (show) {
            viewModelScope.launch {
                repository.markAllAlertsAsRead()
            }
        }
    }

    fun toggleExportPdfDialog(show: Boolean) {
        _uiState.update { it.copy(showExportPdfDialog = show) }
    }

    fun toggleSimulation() {
        val currentlySimulating = _uiState.value.isSimulating
        if (currentlySimulating) {
            simulationJob?.cancel()
            _uiState.update { it.copy(isSimulating = false) }
        } else {
            _uiState.update { it.copy(isSimulating = true) }
            startLiveSimulation()
        }
    }

    fun toggleCameraMode(active: Boolean) {
        _uiState.update { it.copy(isCameraActive = active) }
    }

    fun manualPersonIn() {
        processCountChange(delta = 1, isEntry = true, source = "Manual Input (+1)")
    }

    fun manualPersonOut() {
        if (_uiState.value.currentCount > 0) {
            processCountChange(delta = -1, isEntry = false, source = "Manual Input (-1)")
        }
    }

    fun resetLiveCount() {
        _uiState.update {
            it.copy(
                currentCount = 0,
                alertLevel = AlertLevel.SAFE
            )
        }
        viewModelScope.launch {
            repository.recordTrackingEvent(
                type = EventType.RESET,
                countChange = 0,
                newTotal = 0,
                source = "Operator Reset"
            )
        }
    }

    fun updateMaxCapacity(newCapacity: Int) {
        if (newCapacity < 10) return
        _uiState.update {
            val level = computeAlertLevel(it.currentCount, newCapacity)
            it.copy(capacityLimit = newCapacity, alertLevel = level)
        }
        viewModelScope.launch {
            val current = appSettings.value
            repository.updateSettings(current.copy(maxCapacity = newCapacity))
        }
    }

    private fun processCountChange(delta: Int, isEntry: Boolean, source: String) {
        val limit = _uiState.value.capacityLimit
        val newCount = (_uiState.value.currentCount + delta).coerceAtLeast(0)
        val newMasuk = if (isEntry) _uiState.value.todayMasuk + delta else _uiState.value.todayMasuk
        val newKeluar = if (!isEntry) _uiState.value.todayKeluar + (-delta) else _uiState.value.todayKeluar
        val newLevel = computeAlertLevel(newCount, limit)

        val previousLevel = _uiState.value.alertLevel
        _uiState.update {
            it.copy(
                currentCount = newCount,
                todayMasuk = newMasuk,
                todayKeluar = newKeluar,
                alertLevel = newLevel
            )
        }

        viewModelScope.launch {
            repository.recordTrackingEvent(
                type = if (isEntry) EventType.IN else EventType.OUT,
                countChange = delta,
                newTotal = newCount,
                source = source
            )

            // Trigger notification alert if level changed or breached
            if (newLevel != previousLevel) {
                when (newLevel) {
                    AlertLevel.DANGER -> {
                        repository.createAlertNotification(
                            level = AlertLevel.DANGER,
                            title = "KAPASITAS MELEBIHI BATAS!",
                            count = newCount,
                            capacityLimit = limit
                        )
                    }
                    AlertLevel.WARNING -> {
                        repository.createAlertNotification(
                            level = AlertLevel.WARNING,
                            title = "Kapasitas Hampir Penuh",
                            count = newCount,
                            capacityLimit = limit
                        )
                    }
                    AlertLevel.SAFE -> {
                        if (previousLevel != AlertLevel.SAFE) {
                            repository.createAlertNotification(
                                level = AlertLevel.SAFE,
                                title = "Kapasitas Kembali Aman",
                                count = newCount,
                                capacityLimit = limit
                            )
                        }
                    }
                }
            }
        }
    }

    private fun computeAlertLevel(count: Int, limit: Int): AlertLevel {
        val pct = (count.toFloat() / limit.coerceAtLeast(1).toFloat()) * 100f
        return when {
            pct >= 100f -> AlertLevel.DANGER
            pct >= 80f -> AlertLevel.WARNING
            else -> AlertLevel.SAFE
        }
    }

    private fun startLiveSimulation() {
        simulationJob?.cancel()
        simulationJob = viewModelScope.launch {
            while (isActive) {
                delay(3500L)
                if (!_uiState.value.isSimulating) continue

                // Smart probabilistic fluctuation keeping count lively around realistic values (65 - 85)
                val current = _uiState.value.currentCount
                val isEntry = if (current < 50) {
                    Random.nextFloat() < 0.85f
                } else if (current > 92) {
                    Random.nextFloat() < 0.25f
                } else {
                    Random.nextBoolean()
                }

                val delta = if (isEntry) 1 else -1
                if (current + delta >= 0) {
                    processCountChange(
                        delta = delta,
                        isEntry = isEntry,
                        source = if (isEntry) "AI Camera 01 (Entrance)" else "AI Camera 02 (Exit)"
                    )
                }
            }
        }
    }

    fun exportPdf(context: Context, config: PdfExportConfig) {
        viewModelScope.launch {
            val summary = dailySummary.value
            val records = hourlyRecords.value
            val top5 = SampleDataProvider.getTop5PeakHours()

            val file = PdfReportGenerator.generateAndSharePdf(
                context = context,
                config = config,
                summary = summary,
                hourlyRecords = records,
                topPeakHours = top5
            )

            _uiState.update { it.copy(exportedPdfFile = file, showExportPdfDialog = false) }
            if (file != null) {
                Toast.makeText(context, "Laporan PDF berhasil dibuat!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Gagal membuat PDF", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun markAllAlertsAsRead() {
        viewModelScope.launch {
            repository.markAllAlertsAsRead()
        }
    }

    fun clearAllAlerts() {
        viewModelScope.launch {
            repository.clearAllAlerts()
        }
    }

    fun resetToDemoData(context: Context) {
        viewModelScope.launch {
            repository.resetToSampleData()
            _uiState.update {
                it.copy(
                    currentCount = 72,
                    todayMasuk = 358,
                    todayKeluar = 286,
                    capacityLimit = 100,
                    alertLevel = AlertLevel.SAFE
                )
            }
            Toast.makeText(context, "Data demo berhasil dimuat ulang", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCleared() {
        super.onCleared()
        simulationJob?.cancel()
    }
}
