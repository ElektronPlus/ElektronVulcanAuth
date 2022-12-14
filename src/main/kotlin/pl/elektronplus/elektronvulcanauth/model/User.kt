package pl.elektronplus.elektronvulcanauth.model

data class User (
    var id: String,
    var username: String,
    var discriminator: String
)