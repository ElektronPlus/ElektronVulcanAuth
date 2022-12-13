package pl.elektronplus.elektronvulcanauth.model

data class UserRequest (
 var serverID: String,
 var accessToken: String,
 var className: String,
 var nick: String
)