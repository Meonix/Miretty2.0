package com.github.meonix.chatapp.model

class MessagesChatModel {
    var date: String? = null
    var message: String? = null
    var time: String? = null
    var from_uid: String? = null
    var type: String? = null

    constructor(date: String, from_uid: String, message: String, time: String, type: String) {
        this.date = date
        this.message = message
        this.time = time
        this.from_uid = from_uid
        this.type = type
    }

    constructor() {}
}
