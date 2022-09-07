package pl.elektronplus.elektronvulcanauth.model

data class DiscordOAuthResponse (
    var access_token: String,
    var expires_in: Int,
    var refresh_token: String,
    var scope: String,
    var token_type: String
)