package com.example.explorelimu.data.member.learner

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
import org.jivesoftware.smack.roster.Roster
import org.jxmpp.jid.BareJid

class LearnersAdapter(private val context: Context): RecyclerView.Adapter<LearnersAdapter.LearnerViewHolder>() {
    private var learnerList = emptyList<Learner>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LearnerViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return LearnerViewHolder(layoutInflater.inflate(R.layout.member_item, parent, false))
    }

    override fun onBindViewHolder(holder: LearnerViewHolder, position: Int) {
        val learner = learnerList[position]
        holder.bindLearner(learner)
        holder.dropButton.setOnClickListener { dropUserFromRoster(context, learner.jid) }
    }

    override fun getItemCount(): Int {
        return learnerList.size
    }

    fun setData(learnerList: List<Learner>){
        this.learnerList = learnerList
        notifyDataSetChanged()
    }

    inner class LearnerViewHolder(learnerView: View): RecyclerView.ViewHolder(learnerView){
        private val learnerNameTextView: TextView = learnerView.findViewById(R.id.member_name_tv)
        private val institutionTextView: TextView = learnerView.findViewById(R.id.school_name_tv)
        val dropButton: Button = learnerView.findViewById(R.id.action_btn)

        fun bindLearner(learner: Learner){
            learnerNameTextView.text = learner.name
            institutionTextView.text = learner.institution
            dropButton.text = context.getString(R.string.drop)
        }
    }

    private fun dropUserFromRoster(context: Context, user: BareJid){
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

        val entry = roster.getEntry(user)
        if (entry != null){
            try {
                roster.removeEntry(entry)
            } catch (e: XMPPException){
                Log.e(javaClass.name + "dropUserFromRoster ", e.message!!)
            }
        }
    }
}