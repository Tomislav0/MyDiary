package com.example.mydiary.Fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Icon
import android.media.Image
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mydiary.Activities.NoteUpsertActivity
import com.example.mydiary.R
import com.example.mydiary.constants.Constants
import com.example.mydiary.constants.Constants.BASE_URL
import com.example.mydiary.helpers.DateHelper
import com.example.mydiary.helpers.FileHelper
import com.example.mydiary.helpers.NoteAdapter
import com.example.mydiary.models.NoteBM
import com.example.mydiary.models.NoteDto
import com.google.android.material.internal.ViewUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_diary.*
import kotlinx.coroutines.selects.select
import java.io.File
import java.security.acl.Group
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.properties.Delegates

@AndroidEntryPoint
class DiaryFragment : Fragment(R.layout.fragment_diary), View.OnClickListener, NoteAdapter.OnItemClickListener{
    @Inject
    lateinit var dateHelper: DateHelper
    @Inject
    lateinit var fileHelper: FileHelper

    lateinit var auth : FirebaseAuth
    lateinit var database : FirebaseDatabase
    lateinit var gson:Gson

    lateinit var newNoteBtn : ImageButton
    lateinit var notesRV :RecyclerView
    lateinit var searchBtn : ImageButton
    lateinit var searchET:EditText
    lateinit var titleTV:TextView
    lateinit var progressBar: ProgressBar
    lateinit var cancelSearchBtn : ImageButton
    lateinit var selectAllCB:CheckBox
    lateinit var deleteSelectedBtn : ImageButton
    lateinit var selectionCounterTV : TextView
    lateinit var selectionGroup :androidx.constraintlayout.widget.Group
    lateinit var adapter:NoteAdapter
    private lateinit var fetchedNotes : MutableList<NoteDto>
    private lateinit var notesToSearch : MutableList<NoteDto>
    private var isSearchClicked by Delegates.notNull<Boolean>()
    lateinit var selectedNotes : MutableList<NoteDto>
    lateinit var disabledDays : ArrayList<String>
    lateinit var allDays : ArrayList<String>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth
        database = FirebaseDatabase.getInstance(BASE_URL)

        gson = Gson()

        newNoteBtn = view.findViewById(R.id.addNoteBtn)
        notesRV = view.findViewById(R.id.diaryRV)
        searchBtn = view.findViewById(R.id.searchDiaryImgBtn)
        searchET = view.findViewById(R.id.searchDiaryET)
        titleTV = view.findViewById(R.id.titleDiaryTV)
        progressBar = view.findViewById(R.id.progressDiaryPB)
        cancelSearchBtn = view.findViewById(R.id.cancelSelectionBtn)
        selectAllCB = view.findViewById(R.id.selectAllCB)
        deleteSelectedBtn = view.findViewById(R.id.deleteSelectionBtn)
        selectionGroup = view.findViewById(R.id.selectionGroup)
        selectionCounterTV = view.findViewById(R.id.selectedCounterTV)

        selectionGroup.visibility = View.GONE

        newNoteBtn.setOnClickListener(this)
        searchBtn.setOnClickListener(this)
        cancelSearchBtn.setOnClickListener(this)
        selectAllCB.setOnClickListener(this)
        deleteSelectedBtn.setOnClickListener(this)

        isSearchClicked = false
        searchET.visibility=View.GONE
        searchET.setOnFocusChangeListener{view,
            it-> if(!searchET.isFocused){
            searchET.visibility=View.GONE
            searchBtn.setImageResource(R.drawable.ic_search)
            }
        }
        allDays = arrayListOf()
        fetchNotes()

        notesToSearch = fetchedNotes

        selectedNotes = mutableListOf()
        adapter = NoteAdapter(notesToSearch,selectedNotes,this, requireContext())
        notesRV.adapter = adapter
        notesRV.layoutManager = LinearLayoutManager(context)

        val sdf = SimpleDateFormat("dd.MM.yyyy.")
        searchET.addTextChangedListener{
            var search = searchET.text.toString().lowercase()
            notesToSearch = fetchedNotes.filter { it ->
                        it.content.lowercase().contains(search) ||
                        it.title.lowercase().contains(search) ||
                        sdf.format(it.date).contains(search) } as MutableList<NoteDto>
            notesToSearch.sortByDescending { i->i.date }
            adapter.notes=notesToSearch
            adapter.notifyDataSetChanged()
        }

    }

    override fun onStart() {
        super.onStart()
        notesToSearch = this.fetchedNotes
        adapter.notes=notesToSearch
        adapter.notifyDataSetChanged()
    }

    override fun onClick(view: View?) {
        when(view?.id){
            R.id.addNoteBtn ->{
                navigateToNewNoteActivity()
            }
            R.id.searchDiaryImgBtn ->{
                isSearchClicked = !isSearchClicked
                if(isSearchClicked){
                    searchET.visibility=View.VISIBLE
                    searchBtn.setImageResource(R.drawable.ic_baseline_close_24)
                    searchET.requestFocus()

                }
                else{
                    searchET.visibility=View.GONE
                    searchET.setText(null)
                    searchBtn.setImageResource(R.drawable.ic_search)
                }
            }
            R.id.cancelSelectionBtn->{
                adapter.selectedNotes.clear()
                adapter.isSelected =false
                selectionGroup.visibility=View.GONE
                searchBtn.visibility = View.VISIBLE
                searchET.visibility = View.GONE
                isSearchClicked=false
                searchET.text.clear()
                adapter.notifyDataSetChanged()
            }
            R.id.selectAllCB->{
                if(selectAllCB.isChecked){
                    adapter.selectedNotes.clear()
                    adapter.selectedNotes.addAll(adapter.notes)
                    adapter.notifyDataSetChanged()
                }
                else{
                    adapter.selectedNotes.clear()
                    adapter.notifyDataSetChanged()
                }

                selectedCounterTV.setText(adapter.selectedNotes.size.toString())
            }
            R.id.deleteSelectionBtn->{
                for (selectedNote in adapter.selectedNotes) {
                    database.getReference("Notes").child(auth.currentUser!!.uid)
                        .child(selectedNote.id).removeValue().addOnCompleteListener {
                        if(it.isSuccessful){
                            val toDelete = adapter.notes.filter { i -> i.id == selectedNote.id }
                            if(toDelete.first().hasImage){
                                try {
                                    fileHelper.deleteImageFile(toDelete.first().id )
                                }catch (e:Exception){
                                    e.printStackTrace()}
                            }
                            adapter.notes.remove(toDelete.first())
                            adapter.selectedNotes.clear()
                            cancelSearchBtn.callOnClick()

                    }else {
                            Toast.makeText(context, "Removing failed!", Toast.LENGTH_SHORT).show()
                    }

                }
                    fetchNotes()
            }

            }
        }
    }

    private fun fetchNotes(){
        this.fetchedNotes = mutableListOf()
        this.disabledDays = arrayListOf()
        progressBar.visibility = View.VISIBLE
        database.getReference("Notes").child(auth.currentUser!!.uid).get().addOnCompleteListener {
            if(it.isSuccessful){
                it.result.children.forEach { data ->
                    val toJson = gson.toJson(data.value)
                    val note = gson.fromJson(toJson, NoteBM::class.java)
                    val parsedDate = dateHelper.convertToDateFormat(note.date)
                    this.fetchedNotes.add(NoteDto(note.id,parsedDate,note.title,note.content,note.hasImage, note.hasVoiceRecording, note.moodRate))
                    allDays.add(dateHelper.getDateChartFormat(parsedDate))
                    disabledDays.add(note.date)
                }

                this.fetchedNotes.sortByDescending { it.date }
                notesRV.adapter!!.notifyDataSetChanged()

                progressBar.visibility = View.GONE
                Log.i("notesi",fetchedNotes.toString())
            }else{
                progressBar.visibility = View.GONE
            }
        }


    }

    private fun navigateToNewNoteActivity() {
        var intent = Intent(activity, NoteUpsertActivity::class.java)
        intent.putStringArrayListExtra("disabledDays",disabledDays)
        var day = Calendar.getInstance()
        day.time = Date()
        var freeDay = dateHelper.getDateChartFormat(day.time)
        while(allDays.contains(freeDay)){
            Log.i("uso","stvaro")
            day.set(Calendar.DAY_OF_MONTH,day.get(Calendar.DAY_OF_MONTH)-1)
            freeDay = dateHelper.getDateChartFormat(day.time)
        }
        intent.putExtra("freeDay",freeDay)
        startActivity(intent)
    }

    override fun onItemClick(position: Int) {
        val intent = Intent(activity, NoteUpsertActivity::class.java)
        val note = fetchedNotes[position]
        var noteToSend = NoteDto(note.id,note.date,note.title,note.content,note.hasImage,note.hasVoiceRecording,note.moodRate)
        intent.putExtra("upsertNote",noteToSend)
        intent.putStringArrayListExtra("disabledDays",disabledDays)
        startActivity(intent)

    }

    override fun onItemLongClick(position: Int) {
        selectedCounterTV.setText(adapter.selectedNotes.size.toString())
        if(adapter.isSelected){
            selectionGroup.visibility=View.VISIBLE
            searchBtn.visibility = View.GONE
            searchET.visibility = View.GONE
            selectAllCB.isChecked = false
        }
        else{
            selectionGroup.visibility=View.GONE
            searchBtn.visibility = View.VISIBLE
            searchET.visibility = View.GONE
            searchET.text.clear()
            isSearchClicked=false
        }
    }
}