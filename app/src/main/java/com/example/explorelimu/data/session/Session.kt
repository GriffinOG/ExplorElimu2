package com.example.explorelimu.data.session

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.jxmpp.jid.BareJid

@Parcelize
data class Session(val className: String, val modelId: Int, val modelName: String, val modelFile: String, val educator: String, val attendees: List<BareJid>? = null): Parcelable
