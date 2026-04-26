package com.zovdeneg.app.data.repository

import com.zovdeneg.app.data.remote.ZovBearerAuthInvalidator
import com.zovdeneg.app.data.remote.ZovSessionTokens
import com.zovdeneg.app.domain.auth.ClearRemoteSessionUseCase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class ClearRemoteSessionUseCaseImpl @Inject constructor(
    private val sessionTokens: ZovSessionTokens,
    private val bearerInvalidator: ZovBearerAuthInvalidator,
) : ClearRemoteSessionUseCase {
    override fun invoke() {
        sessionTokens.clear()
        bearerInvalidator.invalidateAll()
    }
}
