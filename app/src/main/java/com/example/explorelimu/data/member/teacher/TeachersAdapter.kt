package com.example.explorelimu.data.member.teacher

import android.content.ContentValues
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.explorelimu.MainActivity
import com.example.explorelimu.R
import org.jivesoftware.smack.SmackException
import org.jivesoftware.smack.XMPPException
import org.jivesoftware.smack.packet.Presence
import org.jivesoftware.smack.roster.Roster
import org.jxmpp.jid.BareJid
import org.jxmpp.jid.impl.JidCreate

class TeachersAdapter(private val context: Context): RecyclerView.Adapter<TeachersAdapter.TeacherViewHolder>() {
    private var teacherList = emptyList<Teacher>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeacherViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return TeacherViewHolder(layoutInflater.inflate(R.layout.member_item, parent, false))
    }

    override fun onBindViewHolder(holder: TeacherViewHolder, position: Int) {
        val teacher = teacherList[position]
        holder.bindTeacher(teacher)
        holder.subscribeButton.setOnClickListener {
            addUserToRoster(context, teacher)
            notifyItemChanged(position)
        }
    }

    override fun getItemCount(): Int {
        return teacherList.size
    }

    fun setData(teacherList: List<Teacher>){
        this.teacherList = teacherList
        notifyDataSetChanged()
    }

    inner class TeacherViewHolder(teacherView: View): RecyclerView.ViewHolder(teacherView){
        private val teacherNameTextView: TextView = teacherView.findViewById(R.id.member_name_tv)
        private val institutionTextView: TextView = teacherView.findViewById(R.id.school_name_tv)
        val subscribeButton: Button = teacherView.findViewById(R.id.action_btn)
        val subscribedTextView: TextView = teacherView.findViewById(R.id.subscribed_tv)

        fun bindTeacher(teacher: Teacher){
            teacherNameTextView.text = teacher.name
            institutionTextView.text = teacher.institution
            if (teacher.requestStatus == Teacher.RequestStatus.SENT){
                subscribeButton.visibility = View.INVISIBLE
                subscribedTextView.visibility = View.VISIBLE
            }
        }
    }

    private fun addUserToRoster(context: Context, teacher: Teacher)
    {

        val roster = Roster.getInstanceFor((context as MainActivity).mConnection)

        if (!roster.isLoaded) try {
            roster.reloadAndWait()
        } catch (e: SmackException.NotLoggedInException) {
            Log.i(ContentValues.TAG, "NotLoggedInException")
            e.printStackTrace()
        } catch (e: SmackException.NotConnectedException) {
            Log.i(ContentValues.TAG, "NotConnectedException")
            e.printStackTrace()
        }

        Log.d("addUserToRoster", "Adding user to roster: ${teacher.jid}")

        val entry = roster.getEntry(teacher.jid)
        if (entry == null) {
            try {
                Log.d(javaClass.name + " formed jid", JidCreate.from("${teacher.jid}@${context.resources.getString(R.string.service_domain_name)}").toString())

                roster.createEntry(teacher.jid,teacher.name, null)
                val subscribe = Presence(Presence.Type.subscribe)
                subscribe.to = JidCreate.from("${teacher.jid}@${context.resources.getString(R.string.service_domain_name)}")
                context.mConnection.sendStanza(subscribe)
                teacher.requestStatus = Teacher.RequestStatus.SENT
            } catch (e: XMPPException) {
                Log.e(javaClass.name + "addUserToRoster", e.message!!)
            }
        }
    }
}