package com.example.explorelimu.data.session

import org.jxmpp.jid.BareJid

data class Session(val className: String, val modelId: Int, val modelName: String, val modelFile: String, val educator: String, val attendees: List<BareJid>? = null)
