package io.greenstep.data.health

import android.content.Context
import android.os.Build
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant

class HealthConnectManager(private val context: Context) {

    private val permissions = setOf(HealthPermission.getReadPermission(StepsRecord::class))

    fun isAvailable(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return false
        return try {
            HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE
        } catch (_: Exception) {
            false
        }
    }

    suspend fun hasPermissions(): Boolean {
        if (!isAvailable()) return false
        return try {
            val client = HealthConnectClient.getOrCreate(context)
            client.permissionController.getGrantedPermissions().containsAll(permissions)
        } catch (_: Exception) {
            false
        }
    }

    suspend fun requestPermissions(): Boolean {
        if (!isAvailable()) return false
        return hasPermissions()
    }

    fun getPermissions(): Set<String> = permissions

    suspend fun readSteps(start: Instant, end: Instant): Long {
        if (!isAvailable()) return 0L
        return try {
            val client = HealthConnectClient.getOrCreate(context)
            val request = ReadRecordsRequest(
                recordType = StepsRecord::class,
                timeRangeFilter = TimeRangeFilter.between(start, end),
            )
            val response = client.readRecords(request)
            response.records.sumOf { it.count }
        } catch (_: Exception) {
            0L
        }
    }
}
