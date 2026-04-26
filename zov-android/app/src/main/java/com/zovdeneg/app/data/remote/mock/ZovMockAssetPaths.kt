package com.zovdeneg.app.data.remote.mock

internal object ZovMockAssetPaths {
    private const val P = "mock/"
    const val PORTFOLIO_SUMMARY = "${P}portfolio_summary.json"
    const val PORTFOLIO_HOLDINGS = "${P}portfolio_holdings.json"
    const val SECURITIES_LIST = "${P}securities_list.json"
    const val TRANSACTIONS_LIST = "${P}transactions_list.json"
    const val SECURITY_DETAILS = "${P}security_details.json"
    const val BALANCE = "${P}balance.json"
    const val BALANCE_AFTER_WITHDRAW = "${P}balance_after_withdraw.json"
    const val USER_PROFILE = "${P}user_profile.json"
    const val AUTH_ENVELOPE = "${P}auth_envelope.json"
    const val AUTH_TOKENS_REFRESH = "${P}auth_tokens_refresh.json"
    const val PIN_CHANGE_OK = "${P}pin_change_ok.json"
    const val ORDER_CREATED = "${P}order_created.json"
}
