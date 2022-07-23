package com.example.mydiary.helpers

import android.content.Context
import android.content.ContextWrapper
import android.os.Environment
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.inject.Inject

class FileHelper @Inject constructor(@ApplicationContext val context: Context) {
     private fun deleteFileFromExternalStorage(filename: String,extension: String,environment: String):Boolean{
        return try {
            val file = File(context.getExternalFilesDir(environment),filename+extension)
            file.delete()
        }catch (e:Exception){
            e.printStackTrace()
            false
        }
    }
    private fun writeToFile(fileName : String,data:ByteArray?, extension:String, environment: String){
        val contextWrapper = ContextWrapper(context)
        val path = contextWrapper.getExternalFilesDir(environment)
        try {
            val file = File(path,fileName+extension)
            val writer = FileOutputStream(file)
            writer.write(data)
            writer.close()
        }catch (e:Exception){
            e.printStackTrace();
        }
    }

    fun getBytes(inputStream: InputStream?): ByteArray? {
        val byteBuffer = ByteArrayOutputStream()
        val bufferSize = 1024
        val buffer = ByteArray(bufferSize)
        var len = 0
        while (inputStream!!.read(buffer).also { len = it } != -1) {
            byteBuffer.write(buffer, 0, len)
        }
        return byteBuffer.toByteArray()
    }

    fun writeImageToFile(fileName : String,data:ByteArray?){
        writeToFile(fileName,data, ".jpg",Environment.DIRECTORY_PICTURES)
    }
    fun writeVoiceRecordingToFile(fileName : String,data:ByteArray?){
        writeToFile(fileName,data, ".mp3",Environment.DIRECTORY_MUSIC)
    }

    fun deleteImageFile(filename: String):Boolean{
        return deleteFileFromExternalStorage(filename,".jpg",Environment.DIRECTORY_PICTURES)
    }
    fun deleteVoiceRecordingFile(filename: String):Boolean{
        return deleteFileFromExternalStorage(filename,".mp3",Environment.DIRECTORY_MUSIC)
    }
}