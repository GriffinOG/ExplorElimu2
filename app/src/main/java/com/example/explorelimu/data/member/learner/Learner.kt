package com.example.explorelimu.data.member.learner

import org.jxmpp.jid.BareJid

data class Learner(val jid: BareJid, val name: String, val institution: String, val subscriptionStatus: SubscriptionStatus = SubscriptionStatus.ACCEPTED){
    enum class SubscriptionStatus {
        ACCEPTED, DROPPED
    }
}