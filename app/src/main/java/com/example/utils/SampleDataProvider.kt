package com.example.utils

import com.example.data.model.AlertLevel
import com.example.data.model.AlertNotification
import com.example.data.model.AppSettings
import com.example.data.model.DailySummary
import com.example.data.model.EventType
import com.example.data.model.HourlyRecord
import com.example.data.model.PeakHourItem
import com.example.data.model.TrackingEvent

object SampleDataProvider {

    const val DEFAULT_DATE = "2024-05-25"
    const val DEFAULT_DISPLAY_DATE = "25 Mei 2024"

    fun getInitialDailySummary(): DailySummary {
        return DailySummary(
            date = DEFAULT_DATE,
            displayDate = DEFAULT_DISPLAY_DATE,
            totalMasuk = 1254,
            totalKeluar = 1112,
            totalSekarang = 142,
            peningkatanPercent = 12.0,
            rataRataPerJam = 104,
            peakHourTime = "14:00",
            peakHourRange = "14:00 - 15:00",
            peakCount = 156
        )
    }

    fun getInitialHourlyRecords(date: String = DEFAULT_DATE): List<HourlyRecord> {
        val raw = listOf(
            Triple(0, 2, 4),
            Triple(1, 1, 2),
            Triple(2, 0, 1),
            Triple(3, 1, 0),
            Triple(4, 5, 2),
            Triple(5, 12, 4),
            Triple(6, 28, 10),
            Triple(7, 45, 20),
            Triple(8, 72, 42),
            Triple(9, 85, 65),
            Triple(10, 98, 80),   // 10:00 -> 105
            Triple(11, 112, 95),  // 11:00 -> 122
            Triple(12, 120, 102), // 12:00 -> 128
            Triple(13, 156, 120), // 13:00 -> 164
            Triple(14, 144, 118), // 14:00 -> 156 (Peak)
            Triple(15, 110, 94),  // 15:00 -> 120
            Triple(16, 92, 82),
            Triple(17, 78, 84),
            Triple(18, 62, 75),
            Triple(19, 48, 56),
            Triple(20, 35, 42),
            Triple(21, 22, 32),
            Triple(22, 14, 22),
            Triple(23, 6, 16)
        )

        var cumulative = 40
        return raw.mapIndexed { index, (hour, masuk, keluar) ->
            cumulative = (cumulative + masuk - keluar).coerceAtLeast(0)
            val timeLabel = String.format("%02d:00", hour)
            HourlyRecord(
                id = (index + 1).toLong(),
                date = date,
                hour = hour,
                timeLabel = timeLabel,
                masuk = masuk,
                keluar = keluar,
                sekarang = if (hour in 10..15) {
                    when (hour) {
                        10 -> 105
                        11 -> 122
                        12 -> 128
                        13 -> 164
                        14 -> 156
                        15 -> 120
                        else -> cumulative
                    }
                } else cumulative,
                isPeak = (hour == 14)
            )
        }
    }

    fun getTop5PeakHours(): List<PeakHourItem> {
        return listOf(
            PeakHourItem(rank = 1, timeRange = "14:00 - 15:00", count = 156, isHighest = true),
            PeakHourItem(rank = 2, timeRange = "13:00 - 14:00", count = 154),
            PeakHourItem(rank = 3, timeRange = "15:00 - 16:00", count = 138),
            PeakHourItem(rank = 4, timeRange = "12:00 - 13:00", count = 128),
            PeakHourItem(rank = 5, timeRange = "16:00 - 17:00", count = 112)
        )
    }

    fun getInitialAlerts(): List<AlertNotification> {
        val now = System.currentTimeMillis()
        return listOf(
            AlertNotification(
                id = 1L,
                timestamp = now - (15 * 60 * 1000),
                formattedTime = "10:35",
                level = AlertLevel.DANGER,
                title = "KAPASITAS MELEBIHI BATAS!",
                count = 124,
                percent = 124,
                capacityLimit = 100,
                isRead = false
            ),
            AlertNotification(
                id = 2L,
                timestamp = now - (30 * 60 * 1000),
                formattedTime = "10:20",
                level = AlertLevel.WARNING,
                title = "Kapasitas Hampir Penuh",
                count = 96,
                percent = 96,
                capacityLimit = 100,
                isRead = false
            ),
            AlertNotification(
                id = 3L,
                timestamp = now - (45 * 60 * 1000),
                formattedTime = "10:10",
                level = AlertLevel.SAFE,
                title = "Kapasitas Aman",
                count = 72,
                percent = 72,
                capacityLimit = 100,
                isRead = true
            ),
            AlertNotification(
                id = 4L,
                timestamp = now - (180 * 60 * 1000),
                formattedTime = "08:00",
                level = AlertLevel.SAFE,
                title = "Sistem Tracking Aktif",
                count = 35,
                percent = 35,
                capacityLimit = 100,
                isRead = true
            )
        )
    }

    fun getInitialEvents(): List<TrackingEvent> {
        val now = System.currentTimeMillis()
        return listOf(
            TrackingEvent(id = 1L, timestamp = now - 12000, type = EventType.IN, countChange = 1, currentTotal = 72, source = "AI Camera 01 (Entrance)"),
            TrackingEvent(id = 2L, timestamp = now - 25000, type = EventType.OUT, countChange = -1, currentTotal = 71, source = "AI Camera 02 (Exit Hallway)"),
            TrackingEvent(id = 3L, timestamp = now - 48000, type = EventType.IN, countChange = 2, currentTotal = 72, source = "AI Camera 01 (Entrance)"),
            TrackingEvent(id = 4L, timestamp = now - 95000, type = EventType.IN, countChange = 1, currentTotal = 70, source = "AI Camera 01 (Entrance)"),
            TrackingEvent(id = 5L, timestamp = now - 140000, type = EventType.OUT, countChange = -1, currentTotal = 69, source = "AI Camera 02 (Exit Hallway)")
        )
    }

    fun getDefaultSettings(): AppSettings {
        return AppSettings(
            id = 1,
            maxCapacity = 100,
            warningThresholdPercent = 80,
            soundEnabled = true,
            vibrationEnabled = true,
            autoSimulationEnabled = true,
            simulationIntervalMs = 2500L,
            activeDetectionMode = "SIMULATION"
        )
    }

    fun getInitialProcessedSketches(): List<com.example.data.model.ProcessedSketchEntity> {
        val now = System.currentTimeMillis()

        val layout1 = com.example.data.model.MockUiLayout(
            screenTitle = "Smart Facility Monitor",
            screenType = "Dashboard",
            summary = "Dasbor monitoring okupansi ruangan real-time dengan metrik kapasitas dan kontrol AI.",
            appBar = com.example.data.model.MockAppBar(
                title = "Smart Facility Monitor",
                hasBackAction = true,
                actions = listOf("Search", "Notifications", "Settings")
            ),
            sections = listOf(
                com.example.data.model.MockUiSection(
                    title = "Metrik Kapasitas Utama",
                    sectionType = com.example.data.model.MockSectionType.HERO_METRIC,
                    elements = listOf(
                        com.example.data.model.MockUiElement(com.example.data.model.MockElementType.STAT_CARD, label = "Kapasitas Aktif", value = "84%", subText = "Batas aman <90%", isPrimary = true),
                        com.example.data.model.MockUiElement(com.example.data.model.MockElementType.STAT_CARD, label = "Total Pengunjung", value = "1,420", subText = "+12% dari kemarin"),
                        com.example.data.model.MockUiElement(com.example.data.model.MockElementType.STAT_CARD, label = "Tingkat Keselamatan", value = "99.2%", subText = "Grade A+")
                    )
                ),
                com.example.data.model.MockUiSection(
                    title = "Zona Fasilitas",
                    sectionType = com.example.data.model.MockSectionType.GRID,
                    elements = listOf(
                        com.example.data.model.MockUiElement(com.example.data.model.MockElementType.CHIP, label = "Zona A • Lobby (42/50)"),
                        com.example.data.model.MockUiElement(com.example.data.model.MockElementType.CHIP, label = "Zona B • Hallway (18/30)"),
                        com.example.data.model.MockUiElement(com.example.data.model.MockElementType.CHIP, label = "Zona C • Auditorium (84/100)")
                    )
                ),
                com.example.data.model.MockUiSection(
                    title = "Aksi & Kontrol Cepat",
                    sectionType = com.example.data.model.MockSectionType.ACTION_BUTTONS,
                    elements = listOf(
                        com.example.data.model.MockUiElement(com.example.data.model.MockElementType.TOGGLE, label = "AI Auto-Tracking Realtime", value = "true"),
                        com.example.data.model.MockUiElement(com.example.data.model.MockElementType.BUTTON, label = "Generate Laporan PDF", isPrimary = true),
                        com.example.data.model.MockUiElement(com.example.data.model.MockElementType.BUTTON, label = "Ekspor CSV Data")
                    )
                )
            ),
            rawDescription = "Sketsa dashboard monitoring kapasitas fasilitas pintar dengan widget metrik dan tombol ekspor."
        )

        val layout2 = com.example.data.model.MockUiLayout(
            screenTitle = "Visitor Pass & Registration",
            screenType = "Form & Registrasi",
            summary = "Formulir pendaftaran dan check-in pengunjung dengan pemindai kartu identitas.",
            appBar = com.example.data.model.MockAppBar(
                title = "Registrasi Pengunjung",
                hasBackAction = true,
                actions = listOf("Scan QR", "Bantuan")
            ),
            sections = listOf(
                com.example.data.model.MockUiSection(
                    title = "Data Pengunjung Baru",
                    sectionType = com.example.data.model.MockSectionType.FORM,
                    elements = listOf(
                        com.example.data.model.MockUiElement(com.example.data.model.MockElementType.TEXT_FIELD, label = "Nama Lengkap", placeholder = "Masukkan nama sesuai KTP"),
                        com.example.data.model.MockUiElement(com.example.data.model.MockElementType.TEXT_FIELD, label = "Instansi / Perusahaan", placeholder = "Nama instansi"),
                        com.example.data.model.MockUiElement(com.example.data.model.MockElementType.TEXT_FIELD, label = "Nomor Telepon", placeholder = "0812-xxxx-xxxx"),
                        com.example.data.model.MockUiElement(com.example.data.model.MockElementType.TOGGLE, label = "Cetak Kartu Akses Tamu", value = "true")
                    )
                ),
                com.example.data.model.MockUiSection(
                    title = "Konfirmasi Check-In",
                    sectionType = com.example.data.model.MockSectionType.ACTION_BUTTONS,
                    elements = listOf(
                        com.example.data.model.MockUiElement(com.example.data.model.MockElementType.BUTTON, label = "Konfirmasi & Masuk", isPrimary = true),
                        com.example.data.model.MockUiElement(com.example.data.model.MockElementType.BUTTON, label = "Batal")
                    )
                )
            ),
            rawDescription = "Sketsa wireframe sistem penerimaan tamu dan registrasi akses masuk."
        )

        val layout3 = com.example.data.model.MockUiLayout(
            screenTitle = "Security Sensor & Camera Hub",
            screenType = "Monitoring Keamanan",
            summary = "Panel kontrol visual kamera pengawas, deteksi anomali, dan status sensor gedung.",
            appBar = com.example.data.model.MockAppBar(
                title = "Pusat Kamera Keamanan",
                hasBackAction = true,
                actions = listOf("Switch View", "Sirene")
            ),
            sections = listOf(
                com.example.data.model.MockUiSection(
                    title = "Status Sensor Real-time",
                    sectionType = com.example.data.model.MockSectionType.HERO_METRIC,
                    elements = listOf(
                        com.example.data.model.MockUiElement(com.example.data.model.MockElementType.STAT_CARD, label = "Kamera Aktif", value = "8 / 8 Online", isPrimary = true),
                        com.example.data.model.MockUiElement(com.example.data.model.MockElementType.STAT_CARD, label = "Deteksi Hari Ini", value = "412 Orang", subText = "Aman")
                    )
                ),
                com.example.data.model.MockUiSection(
                    title = "Daftar Sensor & Feed",
                    sectionType = com.example.data.model.MockSectionType.LIST,
                    elements = listOf(
                        com.example.data.model.MockUiElement(com.example.data.model.MockElementType.LIST_ITEM, label = "Pintu Masuk Utama (CAM-01)", subText = "AI Object Detection: Aktif • 60 FPS"),
                        com.example.data.model.MockUiElement(com.example.data.model.MockElementType.LIST_ITEM, label = "Hallway Barat (CAM-02)", subText = "Arus Orang: 14 per menit"),
                        com.example.data.model.MockUiElement(com.example.data.model.MockElementType.LIST_ITEM, label = "Auditorium Lantai 2 (CAM-03)", subText = "Kapasitas: 84%")
                    )
                ),
                com.example.data.model.MockUiSection(
                    title = "Tindakan Darurat",
                    sectionType = com.example.data.model.MockSectionType.ACTION_BUTTONS,
                    elements = listOf(
                        com.example.data.model.MockUiElement(com.example.data.model.MockElementType.BUTTON, label = "Kunci Pintu Otomatis"),
                        com.example.data.model.MockUiElement(com.example.data.model.MockElementType.BUTTON, label = "Broadcast Notifikasi Evakuasi", isPrimary = true)
                    )
                )
            ),
            rawDescription = "Sketsa pusat kontrol kamera keamanan dan status sensor sensor iot."
        )

        return listOf(
            com.example.data.model.ProcessedSketchEntity(
                id = 1,
                title = "Smart Facility Monitor Dashboard",
                screenType = "Dashboard",
                summary = "Monitoring okupansi real-time dengan metrik kapasitas dan kontrol AI.",
                createdAt = now - (3 * 3600 * 1000),
                displayDate = "Hari Ini, 07:24",
                layoutJson = SketchLayoutParser.layoutToJson(layout1),
                rawDescription = layout1.rawDescription,
                elementCount = SketchLayoutParser.countElements(layout1),
                sectionCount = layout1.sections.size,
                isFavorite = true,
                tags = "Dashboard, AI, Monitoring",
                notes = "Desain sketsa awal untuk implementasi dasbor pemantauan kapasitas gedung."
            ),
            com.example.data.model.ProcessedSketchEntity(
                id = 2,
                title = "Visitor Pass & Registration Gate",
                screenType = "Form & Registrasi",
                summary = "Pendaftaran dan check-in pengunjung dengan kartu identitas.",
                createdAt = now - (26 * 3600 * 1000),
                displayDate = "Kemarin, 09:15",
                layoutJson = SketchLayoutParser.layoutToJson(layout2),
                rawDescription = layout2.rawDescription,
                elementCount = SketchLayoutParser.countElements(layout2),
                sectionCount = layout2.sections.size,
                isFavorite = false,
                tags = "Form, Pengunjung, Gate",
                notes = "Wireframe alur input data tamu di pos security."
            ),
            com.example.data.model.ProcessedSketchEntity(
                id = 3,
                title = "Security Sensor & Camera Hub",
                screenType = "Monitoring Keamanan",
                summary = "Panel kontrol visual kamera pengawas dan status sensor.",
                createdAt = now - (50 * 3600 * 1000),
                displayDate = "2 hari lalu",
                layoutJson = SketchLayoutParser.layoutToJson(layout3),
                rawDescription = layout3.rawDescription,
                elementCount = SketchLayoutParser.countElements(layout3),
                sectionCount = layout3.sections.size,
                isFavorite = true,
                tags = "Kamera, CCTV, Security",
                notes = "Pusat kendali sensor kamera dan deteksi arah pergerakan orang."
            )
        )
    }
}
