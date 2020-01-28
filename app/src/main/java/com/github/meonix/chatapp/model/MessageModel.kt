package com.github.meonix.chatapp.model

class MessageModel {
    var date: String? = null
    var message: String? = null
    var name: String? = null
    var time: String? = null
    var uid: String? = null

    constructor(date: String, message: String, name: String, time: String, uid: String) {
        this.date = date
        this.message = message
        this.name = name
        this.time = time
        this.uid = uid

    }

    constructor() {}
}
