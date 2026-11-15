package com.callcatcher.next.screening

import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService
import android.telecom.Connection

/**
 * Handles supported carrier calls only. It does not intercept third-party VoIP calls.
 * Only explicit user blocklist entries are automatically rejected.
 */
class CallScreeningServiceImpl : CallScreeningService() {
    override fun onScreenCall(callDetails: Call.Details) {
        val incoming = callDetails.handle?.schemeSpecificPart
        val blocked = BlockRules.getBlocked(this)
        val verificationFailed =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                callDetails.callerNumberVerificationStatus == Connection.VERIFICATION_STATUS_FAILED

        // Evaluate additional risk signals for future in-app reporting. They do not
        // silently reject callers; only a user's explicit blocklist choice does.
        SpamIntelligence.assess(incoming, blocked, verificationFailed)
        val shouldBlock = callDetails.callDirection == Call.Details.DIRECTION_INCOMING &&
            BlockRules.isBlocked(incoming, blocked)

        val response = CallResponse.Builder()
            .setDisallowCall(shouldBlock)
            .setRejectCall(shouldBlock)
            .setSkipCallLog(false)
            .setSkipNotification(false)
            .build()

        respondToCall(callDetails, response)
    }
}
