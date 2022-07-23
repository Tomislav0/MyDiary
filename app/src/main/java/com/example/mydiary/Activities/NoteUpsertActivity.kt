package com.example.mydiary.Activities

import android.Manifest
import android.app.Dialog
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import com.example.mydiary.R
import com.example.mydiary.constants.Constants.*
import com.example.mydiary.helpers.DateHelper
import com.example.mydiary.models.NoteBM
import com.example.mydiary.models.NoteDto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.util.*
import javax.inject.Inject
import kotlin.properties.Delegates
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Environment
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.mydiary.helpers.FileHelper
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import java.lang.Exception
import kotlin.collections.ArrayList


@AndroidEntryPoint
class NoteUpsertActivity : AppCompatActivity(), View.OnClickListener, View.OnLongClickListener {
    @Inject
    lateinit var dateHelper : DateHelper
    @Inject
    lateinit var fileHelper: FileHelper

    private lateinit var auth: FirebaseAuth
    private lateinit var database : FirebaseDatabase

    lateinit var backBtn : ImageButton
    lateinit var saveBtn : Button
    lateinit var dateBtn : ImageButton
    lateinit var dateTV : TextView
    lateinit var titleET : EditText
    lateinit var contentET : EditText
    lateinit var progressBar: ProgressBar
    lateinit var imagePickBtn : ImageButton
    lateinit var imageSmallIV : ImageView
    lateinit var imageBigIV : ImageView
    lateinit var imageUri: Uri
    lateinit var voiceRecordBtn : ImageButton
    lateinit var playBtn : ImageButton

    lateinit var mediaRecorder : MediaRecorder
    lateinit var mediaPlayer : MediaPlayer

    lateinit var upsertNote : NoteDto
    var isUpsert by Delegates.notNull<Boolean>()
    var hasImage by Delegates.notNull<Boolean>()
    var hasVoiceRecording by Delegates.notNull<Boolean>()

    lateinit var calendar: Calendar
    var year by Delegates.notNull<Int>()
    var month by Delegates.notNull<Int>()
    var day by Delegates.notNull<Int>()
    var dateId by Delegates.notNull<String>()
    var isRecording by Delegates.notNull<Boolean>()
    var isPlaying by Delegates.notNull<Boolean>()
    var isMoodRated = false;
    var moodRate :Int = 0

    lateinit var disabledDays: ArrayList<Calendar>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_upsert)

        auth = Firebase.auth
        database = FirebaseDatabase.getInstance(BASE_URL)

        backBtn = findViewById(R.id.backNoteUpsertBtn)
        saveBtn = findViewById(R.id.saveNoteBtn)
        progressBar = findViewById(R.id.progressNoteUpsertPB)
        dateTV = findViewById(R.id.dateTitleNoteUpsertTV)
        titleET = findViewById(R.id.titleNoteUpsertET)
        contentET = findViewById(R.id.noteContentTIET)
        dateBtn = findViewById(R.id.datePickerNoteUpsertBtn)
        imagePickBtn = findViewById(R.id.imagePickBtn)
        imageSmallIV = findViewById(R.id.imageSmallIV)
        imageBigIV = findViewById(R.id.imageBigIV)
        voiceRecordBtn = findViewById(R.id.voiceMessageBtn)
        playBtn = findViewById(R.id.playBtn)

        imageUri = Uri.EMPTY
        hasImage = false
        hasVoiceRecording = false
        isRecording = false
        isPlaying = false

        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(this)
        }else{
            MediaRecorder()
        }
        mediaPlayer = MediaPlayer()

        calendar = Calendar.getInstance()


        fetchDataFromIntent()
        setInitialData()
        if(isMicrophonePresent()){
            getMicrophonePersmission()
        }


        dateBtn.setOnClickListener(this)
        backBtn.setOnClickListener(this)
        saveBtn.setOnClickListener(this)
        imagePickBtn.setOnClickListener(this)
        imageSmallIV.setOnClickListener(this)
        imageSmallIV.setOnLongClickListener(this)
        imageBigIV.setOnClickListener(this)
        voiceRecordBtn.setOnClickListener(this)
        playBtn.setOnClickListener(this)
        playBtn.setOnLongClickListener(this)

        mediaPlayer.setOnCompletionListener { it->if(!it.isPlaying){
            this.isPlaying =false
            mediaPlayer.stop()
            mediaPlayer.reset()
            playBtn.setImageResource(R.drawable.ic_baseline_play_arrow_24)
        } }

        imageBigIV.visibility = View.GONE
        imageSmallIV.clipToOutline = true

    }
    override fun onClick(view: View?) {
        when(view?.id){
            R.id.backNoteUpsertBtn ->{
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
            }
            R.id.saveNoteBtn ->{
                saveNote()
            }
            R.id.datePickerNoteUpsertBtn ->{
                val datePickerDialog = DatePickerDialog.newInstance({ view, myear, mmonth, mdayOfMonth ->
                    var fixedmonth = mmonth + 1
                    dateTV.setText(mdayOfMonth.toString() + "." + fixedmonth.toString() + "." + myear.toString() + ".")
                    year = myear
                    month = mmonth
                    day = mdayOfMonth
                    calendar.set(myear, mmonth, mdayOfMonth)
                },year, month,day)
                datePickerDialog.setCancelColor(Color.BLACK)
                datePickerDialog.setOkColor(Color.BLACK)
                datePickerDialog.disabledDays = disabledDays.toTypedArray()
                datePickerDialog.show(supportFragmentManager,"DatePicker")
            }
            R.id.imagePickBtn ->{
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if(checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_DENIED){
                        val permissions = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                        requestPermissions(permissions, PERMISSION_CODE)
                    }
                    else{
                        pickImageFromGallery()
                    }
                }
                else{

                }
            }
            R.id.imageSmallIV->{
               imageBigIV.visibility = View.VISIBLE
            }
            R.id.imageBigIV->{
                imageBigIV.visibility = View.GONE
            }
            R.id.voiceMessageBtn -> {
                if(isPlaying){
                    Toast.makeText(this,"Please stop player to record.",Toast.LENGTH_SHORT)
                    return
                }
                isRecording = !isRecording
                if(isRecording){
                    voiceRecordBtn.setImageResource(R.drawable.ic_baseline_stop_24)
                    hasVoiceRecording=true
                   startRecording()
                }else{
                    voiceRecordBtn.setImageResource(R.drawable.ic_baseline_keyboard_voice_24)
                    playBtn.visibility=View.VISIBLE
                    stopRecording()
                }
            }
            R.id.playBtn ->{
                isPlaying = !isPlaying
                if(isPlaying){
                    try {
                        mediaPlayer.setDataSource(getRecordingFilePath())
                        mediaPlayer.prepare()
                        mediaPlayer.start()
                        playBtn.setImageResource(R.drawable.ic_baseline_stop_24)
                    }catch (e:Exception)
                    {
                        e.printStackTrace()
                    }
                }else{
                    mediaPlayer.stop()
                    mediaPlayer.reset()
                    playBtn.setImageResource(R.drawable.ic_baseline_play_arrow_24)
                }
            }
        }
    }

    private fun stopRecording() {
        mediaRecorder.stop()
        mediaRecorder.reset()

        Toast.makeText(this,"Recording stopped",Toast.LENGTH_SHORT).show()
    }

    private fun startRecording() {
        try {
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            mediaRecorder.setOutputFile(getRecordingFilePath())
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            mediaRecorder.prepare()
            mediaRecorder.start()

            Toast.makeText(this,"Recording started",Toast.LENGTH_SHORT).show()

        }catch(e:Exception){
            e.printStackTrace()
        }
    }

    private fun getRecordingFilePath():String{
        val contextWrapper = ContextWrapper(applicationContext)
        val musicFile = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        val file = File(musicFile,dateId+".mp3")
        return file.path
    }
    override fun onLongClick(v: View?): Boolean {
        when(v?.id){
            R.id.imageSmallIV ->{
                openDeleteImageDialog()
            }
            R.id.playBtn ->{
                openDeleteVoiceMessageDialog()
            }
        }

        return true
    }

    private fun openDeleteVoiceMessageDialog() {
        val voiceMessageDialog =  Dialog(this)
        voiceMessageDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        voiceMessageDialog.setCancelable(false)
        voiceMessageDialog.setContentView(R.layout.dialog_delete_template)
        val messageTVDialog : TextView = voiceMessageDialog.findViewById(R.id.messageTVDialog)
        val deleteDialogBtn :Button = voiceMessageDialog.findViewById(R.id.deleteBtnDialog)
        val cancelDialogBtn :Button = voiceMessageDialog.findViewById(R.id.cancelBtnDialog)

        messageTVDialog.text = R.string.deleteVoiceMessage.toString()

        deleteDialogBtn.setOnClickListener{
            hasVoiceRecording=false
            playBtn.visibility = View.GONE
            fileHelper.deleteVoiceRecordingFile(upsertNote.id)
            voiceMessageDialog.dismiss()
        }
        cancelDialogBtn.setOnClickListener{
            voiceMessageDialog.dismiss()
        }

        voiceMessageDialog.show()
    }

    private fun openDeleteImageDialog() {
        val imageDialog =  Dialog(this)
        imageDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        imageDialog.setCancelable(false)
        imageDialog.setContentView(R.layout.dialog_delete_template)
        val messageTVDialog : TextView = imageDialog.findViewById(R.id.messageTVDialog)
        val deleteDialogBtn :Button = imageDialog.findViewById(R.id.deleteBtnDialog)
        val cancelDialogBtn :Button = imageDialog.findViewById(R.id.cancelBtnDialog)

        messageTVDialog.text = R.string.deleteImage.toString()

        deleteDialogBtn.setOnClickListener{
            hasImage=false
            imageSmallIV.visibility = View.INVISIBLE
            imageUri = Uri.EMPTY
            if(upsertNote.hasImage) {
                fileHelper.deleteImageFile(upsertNote.id)
            }
            imageDialog.dismiss()
        }
        cancelDialogBtn.setOnClickListener{
            imageDialog.dismiss()
        }

        imageDialog.show()
    }


    private fun setInitialData() {
        if (isUpsert) {
            dateId = upsertNote.id
            if(upsertNote.hasImage) {

                val imgFile = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),upsertNote.id+".jpg")
                if (imgFile.exists()) {
                    val myBitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                    imageSmallIV.setImageBitmap(myBitmap)
                    imageBigIV.setImageBitmap(myBitmap)
                }
            }else{
                imageSmallIV.visibility = View.GONE
            }
            hasImage=upsertNote.hasImage
            hasVoiceRecording = upsertNote.hasVoiceRecording

            val upsertMonth = dateHelper.getDateMonth(upsertNote.date)
            day= dateHelper.getDateDay(upsertNote.date)
            month = upsertMonth-1
            year = dateHelper.getDateYear(upsertNote.date)
            dateTV.setText(day.toString() + "." + upsertMonth.toString() + "." + year.toString() + ".")
            titleET.setText(upsertNote.title)
            contentET.setText(upsertNote.content)
            calendar.set(year,month,day)
            moodRate=upsertNote.moodRate
        } else {
            hasVoiceRecording = false;
            hasImage=false
            playBtn.visibility=View.GONE
            dateId = Date().time.toString()
            year = calendar.get(Calendar.YEAR)
            month = calendar.get(Calendar.MONTH)
            day = calendar.get(Calendar.DAY_OF_MONTH)
            var fixedMonth = month+1
            dateTV.setText(day.toString() + "." + fixedMonth.toString() + "." + year.toString() + ".")
        }

        playBtn.visibility  = if(hasVoiceRecording) View.VISIBLE else View.GONE
        imageSmallIV.visibility = if(hasImage) View.VISIBLE else View.GONE
    }

    private fun fetchDataFromIntent() {
        val data = intent.getParcelableExtra<NoteDto>("upsertNote")
        val disabledDaysString : ArrayList<String>? = intent.getStringArrayListExtra("disabledDays")
        if(disabledDaysString!=null){
        Log.i("jel je", disabledDaysString.toString())}

        disabledDays = arrayListOf()
        disabledDaysString?.forEach { it->
            val date = dateHelper.convertToDateFormat(it)
            val day = dateHelper.getDateDay(date)
            val month = dateHelper.getDateMonth(date)-1
            val year = dateHelper.getDateYear(date)
            val cal = Calendar.getInstance()
            cal.set(year,month,day)
            disabledDays?.add(cal)}

        if (data != null) {
            isUpsert = true
            upsertNote = data;
        } else {
            isUpsert = false
            upsertNote = NoteDto("", Date(), "", "", false, false,0)
        }
        Log.i("log_isUpsert",isUpsert.toString())
    }


    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    private fun saveNote() {
        var date = dateHelper.convertToStringFormat(calendar.time)
        val title =  titleET.text.toString()
        val content = contentET.text.toString()

        if(title.isEmpty()&& content.isEmpty()){
            titleET.requestFocus()
            contentET.requestFocus()
            Toast.makeText(this,"Please provide some data to save note.", Toast.LENGTH_LONG).show()
            return
        }

        if(!isUpsert && !isMoodRated){
            openMoodRatingDialog()
            return
        }

        progressBar.visibility=View.VISIBLE
        val note = NoteBM(dateId,date,title,content, hasImage, hasVoiceRecording,moodRate)

        if(imageUri != Uri.EMPTY){
            val iStream = contentResolver.openInputStream(imageUri)
            val inputData :ByteArray? = fileHelper.getBytes(iStream)

            fileHelper.writeImageToFile(note.id,inputData)
        }

        database.getReference("Notes").
                child(auth.currentUser!!.uid).
                child(dateId).
                setValue(note).addOnCompleteListener{
                    if(it.isSuccessful){
                        Toast.makeText(this,noteSavedMessage, Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, HomeActivity::class.java)
                        startActivity(intent)
                    }
                    else{

                        Toast.makeText(this,somethingWentWrong, Toast.LENGTH_SHORT).show()
                    }
                    progressBar.visibility=View.GONE
        }

    }

    private fun openMoodRatingDialog() {
        val mooddialog =  Dialog(this)
        mooddialog.setOnCancelListener(null)
        mooddialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        mooddialog.setCancelable(false)
        mooddialog.setContentView(R.layout.dialog_mood_rating)
        val titleTV : TextView = mooddialog.findViewById(R.id.titleTVDialog)
        val spinner:Spinner = mooddialog.findViewById(R.id.moodSpinner)
        val saveDialogBtn :Button = mooddialog.findViewById(R.id.saveNoteBtnDialog)
        val cancelDialogBtn :Button = mooddialog.findViewById(R.id.cancelNoteBtnDialog)
        ArrayAdapter.createFromResource(
            this,
            R.array.moodRate,
            android.R.layout.simple_spinner_dropdown_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }
        saveDialogBtn.setOnClickListener{
            if(spinner.selectedItem.toString() =="Select mood"){
                spinner.requestFocus()
            }else{
                isMoodRated = true
                moodRate = spinner.selectedItem.toString().toInt()
                mooddialog.dismiss()
                saveNote()
            }
        }
        cancelDialogBtn.setOnClickListener{
            isMoodRated = false
            mooddialog.dismiss()
        }
        mooddialog.window?.setGravity(1)

        mooddialog.show()

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            PERMISSION_CODE->{
                if(grantResults.isNotEmpty() && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    pickImageFromGallery()
                }
                else{
                    Toast.makeText(this,"Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode== RESULT_OK && requestCode == IMAGE_PICK_CODE){
            imageSmallIV.visibility=View.VISIBLE
            imageSmallIV.setImageURI(data?.data)
            imageBigIV.setImageURI(data?.data)
            imageUri = data?.data!!
            hasImage = true
        }
    }

    private fun isMicrophonePresent() :Boolean{
        return this.packageManager.hasSystemFeature(PackageManager.FEATURE_MICROPHONE)
    }

    private fun getMicrophonePersmission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), MICROPHONE_PERMISSION_CODE)
        }
    }
}

