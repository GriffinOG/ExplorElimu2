package com.example.explorelimu.data.session

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.explorelimu.MainActivity
import com.example.explorelimu.R
import com.example.explorelimu.data.model.Model
import org.jivesoftware.smack.roster.Roster
import org.jivesoftware.smackx.muc.MultiUserChatManager
import org.jxmpp.jid.EntityBareJid
import org.jxmpp.jid.impl.JidCreate
import org.jxmpp.jid.parts.Resourcepart
import java.lang.Exception

class SessionsRepository(private val context: Context) {
    private var sessionsRepository: SessionsRepository? = null

    @Synchronized
    fun getInstance(): SessionsRepository? {
        if (sessionsRepository == null) sessionsRepository = SessionsRepository(context)
        return sessionsRepository
    }

    val _classesList: MutableLiveData<List<Session>> = MutableLiveData()
    val classesList: LiveData<List<Session>>
        get() = _classesList

    fun getClasses() {
        val sessions = ArrayList<Session>()
        val mConnection = (context as MainActivity).mConnection
        val multiUserChatManager = MultiUserChatManager.getInstanceFor(mConnection)

        val joinedRooms = multiUserChatManager.joinedRooms
        for (joinedRoom in joinedRooms){
            val muc = multiUserChatManager.getMultiUserChat(joinedRoom)

            val session = Session(muc.subject.substringAfterLast("@"),
                    muc.subject.substringBeforeLast("/").toInt(),
                    muc.subject.substringAfterLast("/").substringBeforeLast("&"),
                    muc.subject.substringAfterLast("&").substringBeforeLast("@"),
                    muc.owners[0].jid.asUnescapedString().substringBeforeLast("@"))
            sessions.add(session)
        }
        _classesList.postValue(sessions)
    }

    fun createSession(roomName: String, model: Model, ownersList: ArrayList<EntityBareJid>) {
        val mConnection = (context as MainActivity).mConnection

        try {
            val multiUserChatManager = MultiUserChatManager.getInstanceFor(mConnection)

            // Create a MultiUserChat using an XMPPConnection for a room
            val multiUserChat =
                multiUserChatManager.getMultiUserChat(JidCreate.entityBareFrom(
                    "$roomName@" + context.resources.getString(
                    R.string.muc_service)))

            // Create the nickname.
            val nickname = Resourcepart.from(roomName)

            // Create the room
            multiUserChat.create(nickname)
                .configFormManager
                .submitConfigurationForm()

            val subject = "${model.id}/${model.name}&${model.fileName}@$roomName"
            multiUserChat.changeSubject(subject)

            for (user in ownersList) multiUserChat.invite(user, subject)

            val session = Session(roomName,
                    model.id,
                    model.name,
                    model.fileName,
                    multiUserChat.moderators[0].jid.asUnescapedString().substringBeforeLast("@"))

            _classesList.postValue(listOf(session))
        }catch (e: Exception){
            Log.e(javaClass.name + " createSession", e.message.toString())
            Toast.makeText(context, "Error creating room. Try again later", Toast.LENGTH_LONG).show()
        }

    }

    fun getRosterEntries(): ArrayList<EntityBareJid> {
        val mConnection = (context as MainActivity).mConnection
        val rosterEntries: ArrayList<EntityBareJid> = arrayListOf()

        val roster = Roster.getInstanceFor(mConnection)

        if (!roster.isLoaded){
            roster.reloadAndWait()
        }

        try {
            for (entry in roster.entries){
                rosterEntries.add(JidCreate.entityBareFrom(entry.jid))
            }
        }catch (e: java.lang.Exception){
            Log.e(javaClass.name, e.message.toString())
        }

        return rosterEntries
    }

}