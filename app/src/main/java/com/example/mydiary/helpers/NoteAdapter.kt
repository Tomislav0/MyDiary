package com.example.mydiary.helpers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.mydiary.R
import com.example.mydiary.models.NoteDto
import java.io.File
import java.text.SimpleDateFormat
import kotlin.coroutines.coroutineContext


class NoteAdapter(
    var notes: MutableList<NoteDto>,
    var selectedNotes:MutableList<NoteDto>,
    val listener : OnItemClickListener,
    val context : Context,
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {
    var isSelected = false

    inner class NoteViewHolder(itemView:View) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener{
            val date : TextView = itemView.findViewById(R.id.dateItemNoteTV)
        val title : TextView = itemView.findViewById(R.id.titleItemNoteTV)
        val content : TextView = itemView.findViewById(R.id.contentItemNoteTV)
        val selectedView : View = itemView.findViewById(R.id.isSelectedView)
        val noteImg : ImageView = itemView.findViewById(R.id.noteImg)
        val voiceRecordingIV : ImageView = itemView.findViewById(R.id.voiceRecordingIV)

        init{
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }


        override fun onClick(v: View?) {
            val position = adapterPosition
            if(position != RecyclerView.NO_POSITION){
                if(isSelected){
                    onLongClick(v)
                }
                else{
                listener.onItemClick(position)
                }
            }
        }

        override fun onLongClick(v: View?): Boolean {
            val position = adapterPosition
            if(position != RecyclerView.NO_POSITION){
                if(!selectedNotes.contains(notes[position])){

                    selectedNotes.add((notes[position]))
                }else{
                    selectedNotes.remove((notes[position]))
                }
                notifyItemChanged(position)
                isSelected = selectedNotes.size!=0
                listener.onItemLongClick(position)
            }
            return true;
        }
    }

    interface  OnItemClickListener {
        fun onItemClick(position: Int)
        fun onItemLongClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return notes.size
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {

        if (!selectedNotes.contains(notes[position])) {
            holder.selectedView.visibility = View.GONE
        } else {
            holder.selectedView.visibility = View.VISIBLE
        }

        val sdf = SimpleDateFormat("dd.MM.yyyy.")
        var title = notes[position].title
        var content = notes[position].content
        if (title.isNotEmpty() && title.length > 13) {
            title = title.slice(IntRange(0, 10)) + "..."
        }

        if (content.isNotEmpty() && content.length > 87) {
            content = content.slice(IntRange(0, 84)) + "..."
        }

        holder.date.text = sdf.format(notes[position].date)
        holder.title.text = title
        holder.content.text = content
        holder.voiceRecordingIV.visibility = if(notes[position].hasVoiceRecording) View.VISIBLE else View.GONE

        if (notes[position].hasImage) {
            val imgFile = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), notes[position].id +".jpg")
            if (imgFile.exists()) {
                Log.i("uso","vadi")
                var myBitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                holder.noteImg.visibility = View.VISIBLE
                holder.noteImg.setImageBitmap(myBitmap)
            }
        }else{
            holder.noteImg.visibility = View.GONE
            holder.noteImg.setImageBitmap(null)
        }


    }


}