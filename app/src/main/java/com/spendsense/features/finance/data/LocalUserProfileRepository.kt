package com.spendsense.features.finance.data

import android.content.Context
import com.spendsense.features.finance.domain.UserProfile
import com.spendsense.features.finance.domain.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class LocalUserProfileRepository(
    context: Context,
) : UserProfileRepository {
    private val preferences = context.applicationContext.getSharedPreferences("spendsense_profile", Context.MODE_PRIVATE)
    private val profile = MutableStateFlow(readProfile())

    override fun observeProfile(): Flow<UserProfile> = profile.asStateFlow()

    override suspend fun update(profile: UserProfile) {
        preferences.edit()
            .putString(KEY_NAME, profile.name)
            .putLong(KEY_SALARY, profile.monthlySalaryMinor)
            .putString(KEY_CURRENCY, profile.currency)
            .putString(KEY_PHOTO_PATH, profile.profilePhotoPath)
            .apply()
        this.profile.value = profile
    }

    private fun readProfile(): UserProfile {
        return UserProfile(
            name = preferences.getString(KEY_NAME, null)?.takeIf { it.isNotBlank() } ?: "Darshan",
            monthlySalaryMinor = preferences.getLong(KEY_SALARY, 0L),
            currency = preferences.getString(KEY_CURRENCY, null)?.takeIf { it.isNotBlank() } ?: "INR",
            profilePhotoPath = preferences.getString(KEY_PHOTO_PATH, null),
        )
    }

    private companion object {
        const val KEY_NAME = "name"
        const val KEY_SALARY = "monthly_salary_minor"
        const val KEY_CURRENCY = "currency"
        const val KEY_PHOTO_PATH = "profile_photo_path"
    }
}
