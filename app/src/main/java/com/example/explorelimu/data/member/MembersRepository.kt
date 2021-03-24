package com.example.explorelimu.data.member

import android.content.ContentValues
import android.content.Context
import android.provider.ContactsContract
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.explorelimu.MainActivity
import com.example.explorelimu.data.member.learner.Learner
import com.example.explorelimu.data.member.teacher.Teacher
import org.jivesoftware.smack.SmackException
import org.jivesoftware.smack.XMPPException
import org.jivesoftware.smack.roster.Roster
import org.jivesoftware.smackx.search.ReportedData
import org.jivesoftware.smackx.search.UserSearchManager
import org.jivesoftware.smackx.vcardtemp.VCardManager
import org.jxmpp.jid.BareJid
import org.jxmpp.jid.impl.JidCreate

class MembersRepository(private val context: Context) {
    private var membersRepository: MembersRepository? = null

    @Synchronized
    fun getInstance(): MembersRepository? {
        if (membersRepository == null) membersRepository = MembersRepository(context)
        return membersRepository
    }

    val _teachersList: MutableLiveData<List<Teacher>> = MutableLiveData()
    val teachersList: LiveData<List<Teacher>>
        get() = _teachersList

    val _learnersList: MutableLiveData<List<Learner>> = MutableLiveData()
    val learnersList: LiveData<List<Learner>>
        get() = _learnersList

    fun getTeachers() {
        var teachers = ArrayList<Teacher>()

        val cr = context.contentResolver
        val cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null)

        if ((cur?.count ?: 0) > 0) {
            while (cur != null && cur.moveToNext()) {
                val id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID)
                )
                val name = cur.getString(
                        cur.getColumnIndex(
                                ContactsContract.Contacts.DISPLAY_NAME
                        )
                )

                val pCur = cr.query(
                        ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                        arrayOf<String>(id), null
                )

                while (pCur!!.moveToNext()) {
                    val email = pCur.getString(
                            pCur.getColumnIndex(
                                    ContactsContract.CommonDataKinds.Email.ADDRESS
                            )
                    )

                    checkUser(context, email, teachers)
                    Log.i(javaClass.name, "Name: $name")
                    Log.i(javaClass.name, "Email: $email")

                }
                pCur.close()
            }
        }
        cur?.close()
        _teachersList.postValue(teachers)
        Log.d(javaClass.name + " teacherList size", _teachersList.value?.size.toString())
    }

    fun checkUser(context: Context, email: String, teachers: ArrayList<Teacher>)
    {
        Log.d(javaClass.name, "Checking user")

        if (context is MainActivity){
            val mConnection = context.mConnection

            val search = UserSearchManager(mConnection)

            val searchForm = search
                .getSearchForm(JidCreate.domainBareFrom("search." + mConnection.xmppServiceDomain))
            Log.d(javaClass.name, "Search form created")

            val answerForm = searchForm.createAnswerForm()
            answerForm.setAnswer("Email", true)
            answerForm.setAnswer("search", email)

            var data: ReportedData? = null

            try {
                data = search.getSearchResults(
                        answerForm,
                        JidCreate.domainBareFrom("search." + mConnection.xmppServiceDomain)
                )
                Log.i(javaClass.name, "Searching...")
            }catch (e: Exception){
                Log.e(javaClass.name, "Can't search")
                when(e){
                    is SmackException.NoResponseException, is XMPPException.XMPPErrorException, is SmackException.NotConnectedException, is InterruptedException -> {
                        context.runOnUiThread {
                            Toast.makeText(
                                    context,
                                    "An error occurred while loading your contact list",
                                    Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }

//            Log.d("ContactsActivity", newPhone)

            if (data?.rows != null) {
                val roster = Roster.getInstanceFor(mConnection)
                if (!roster.isLoaded) try {
                    roster.reloadAndWait()
                } catch (e: SmackException.NotLoggedInException) {
                    Log.i(ContentValues.TAG, "NotLoggedInException")
                    e.printStackTrace()
                } catch (e: SmackException.NotConnectedException) {
                    Log.i(ContentValues.TAG, "NotConnectedException")
                    e.printStackTrace()
                }

                for (row in data.rows) {
                    var userJid: BareJid? = null
                    var isInRoster = false
                    for (jid in row.getValues("jid")) userJid = JidCreate.bareFrom(jid)
                    if (roster.getEntry(userJid) != null){
                        isInRoster = true
                    }

                    for (name in row.getValues("name")) {
                        Log.i("Iterator values......", " $name")

                        if ((name as String).substringAfterLast("101").substringBeforeLast("404") == "educator"){
                            Log.d(javaClass.name + " teacher jid", JidCreate.bareFrom(name).toString())
                            val teacher = Teacher(userJid!!,
                                    name.substringBeforeLast("101"),
                                    name.substringAfterLast("404").replace("909", " "),
                                    requestStatus = if (isInRoster) Teacher.RequestStatus.SENT else Teacher.RequestStatus.NULL
                            )
                            teachers.add(teacher)
                        }
//                        val user = JidCreate.bareFrom(value)
//                        addUserToRoster(mConnection, user, name)
                    }
                }
            }
        }
    }

    fun getLearners() {
        Log.d(javaClass.name, "getLearners called")
        var learners = ArrayList<Learner>()

        val mConnection = (context as MainActivity).mConnection
        val vCardManager = VCardManager.getInstanceFor(mConnection)

        val roster = Roster.getInstanceFor(mConnection)

        if (!roster.isLoaded) try {
            roster.reloadAndWait()
        } catch (e: SmackException.NotLoggedInException) {
            Log.e(javaClass.name, "NotLoggedInException")
        } catch (e: SmackException.NotConnectedException) {
            Log.e(javaClass.name, "NotConnectedException")
        }

        Log.d(javaClass.name + " roster size", roster.entries.size.toString())

        try {
            for (entry in roster.entries){
                val vCard = vCardManager.loadVCard(JidCreate.entityBareFrom(entry.jid))

                val learner = Learner(entry.jid, vCard.nickName, vCard.organization)
                learners.add(learner)

//                if (name.substringAfterLast("101").substringBeforeLast("404") == "student"){
//                    val learner = Learner(entry.jid, name.toString().substringBeforeLast("101"),
//                        name.substringAfterLast("404").replace("909", " "))
//                    learners.add(learner)
//                }
            }
        }catch (e: java.lang.Exception){
            Log.e(javaClass.name, e.message.toString())
        }


//        _learnersList.postValue(learners)
        Log.d(javaClass.name, "learners posted")

    }
}

//    fun addUserToRoster(xmpptcpConnection: XMPPTCPConnection, user: BareJid, userName: String)
//    {
//
//        val roster = Roster.getInstanceFor(xmpptcpConnection)
//
//        if (!roster.isLoaded) try {
//            roster.reloadAndWait()
//        } catch (e: SmackException.NotLoggedInException) {
//            Log.i(ContentValues.TAG, "NotLoggedInException")
//            e.printStackTrace()
//        } catch (e: SmackException.NotConnectedException) {
//            Log.i(ContentValues.TAG, "NotConnectedException")
//            e.printStackTrace()
//        }
//        Log.d("addUserToRoster", "Adding user to roster: $user")
//
//        val entry = roster.getEntry(user)
//        if (entry == null) {
//            try {
//                roster.createEntry(user,userName, null)
//                val subscribe = Presence(Presence.Type.subscribe)
//                subscribe.to = JidCreate.from("$user@${context.resources.getString(R.string.service_domain_name)}")
//                (context as ContactsActivity).mConnection.sendStanza(subscribe)
//            } catch (e: XMPPException) {
//                Log.e("addUserToRoster", e.message)
//            }
//        }
//
//    }