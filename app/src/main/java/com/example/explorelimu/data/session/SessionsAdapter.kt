package com.example.explorelimu.data.session

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.explorelimu.R
import com.example.explorelimu.util.downloadModel

class SessionsAdapter(private val context: Context): RecyclerView.Adapter<SessionsAdapter.SessionViewHolder>() {

    private var sessionsList = emptyList<Session>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return SessionViewHolder(layoutInflater.inflate(R.layout.session_item, parent, false))
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        val session = sessionsList[position]
        holder.bindSession(session)
        holder.joinButton.setOnClickListener { downloadModel(context, session.modelFile, session.modelId) }
    }

    override fun getItemCount(): Int {
        return sessionsList.size
    }

    fun setData(sessionList: List<Session>){
        this.sessionsList = sessionList
        notifyDataSetChanged()
    }

    inner class SessionViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val sessionNameTextView = itemView.findViewById<TextView>(R.id.session_name_tv)
        private val educatorNameTextView = itemView.findViewById<TextView>(R.id.educator_name_tv)
        private val modelNameTextView = itemView.findViewById<TextView>(R.id.description_tv)
        val joinButton = itemView.findViewById<Button>(R.id.action_btn)

        fun bindSession(session: Session) {
            sessionNameTextView.text = session.className
            educatorNameTextView.text = session.educator
            modelNameTextView.text = session.modelName
        }
    }
}