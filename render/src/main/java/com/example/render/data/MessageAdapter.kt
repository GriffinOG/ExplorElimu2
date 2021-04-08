package com.example.render.data

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.render.R
import org.andresoviedo.android_3d_model_engine.inclass.data.GroupMessage
import java.text.SimpleDateFormat

class MessageAdapter(private val context: Context): RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {
    private var messageList = emptyList<GroupMessage>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return MessageViewHolder(layoutInflater.inflate(R.layout.layout_msg, parent, false))
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messageList[position]
        holder.bindMessage(message)
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    fun setData(messageList: List<GroupMessage>){
        this.messageList = messageList
        notifyDataSetChanged()
    }

    inner class MessageViewHolder(msgView: View): RecyclerView.ViewHolder(msgView) {
        private val senderNameTextView: TextView = msgView.findViewById(R.id.user_id_tv)
        private val timeStampTextView: TextView = msgView.findViewById(R.id.time_tv)
        private val msgTextView: TextView = msgView.findViewById(R.id.msg_tv)

        fun bindMessage(groupMessage: GroupMessage){
            senderNameTextView.text = groupMessage.sender

            val currentTimeString: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                SimpleDateFormat("HH:mm a", context.resources.configuration.locales.get(0)).format(groupMessage.date)
            } else
                SimpleDateFormat("HH:mm a", context.resources.configuration.locale).format(groupMessage.date)
            timeStampTextView.text = currentTimeString
        }
    }
}