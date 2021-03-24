package com.example.explorelimu.data.member.teacher

import org.jxmpp.jid.BareJid

data class Teacher(val jid: BareJid, val name: String, val institution: String, var requestStatus: RequestStatus = RequestStatus.NULL){
    enum class RequestStatus {
        SENT, ACCEPTED, NULL
    }
}
