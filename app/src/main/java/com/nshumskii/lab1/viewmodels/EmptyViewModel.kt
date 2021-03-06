package com.nshumskii.lab1.viewmodels

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.nshumskii.lab1.data.AppDatabase
import com.nshumskii.lab1.data.PersonRepository
import com.nshumskii.lab1.models.Event
import com.nshumskii.lab1.models.Person
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EmptyViewModel(application: Application) : AndroidViewModel(application) {

    private var personRepository =
        PersonRepository(AppDatabase.getInstance(application).personDataDao())

    var fileEvent: MutableLiveData<Event<String>> = MutableLiveData()

    companion object {
        private val TAG = EmptyViewModel::class.java.simpleName
        const val REQUEST_TO_IMPORT_FILE = 10
        const val ACTION_FINISH_IMPORT_FILE = "finish_import_file"
        const val ACTION_START_IMPORT_FILE = "start_import_file"
        const val ACTION_ERROR_IMPORT_FILE = "error_import_file"
    }

    fun fileToImport(resultCode: Int, data: Intent?) {
        if (Activity.RESULT_OK == resultCode) {
            data?.data?.let { uri ->
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        getApplication<Application>().contentResolver.openInputStream(uri)
                            .use { inputStream ->
                                withContext(Main) { fileEvent.value = Event(ACTION_START_IMPORT_FILE) }
                                JsonReader(inputStream?.reader()).use { jsonReader ->
                                    val personsType = object : TypeToken<List<Person>>() {}.type
                                    val persons: List<Person> =
                                        GsonBuilder().create().fromJson(jsonReader, personsType)

                                    personRepository.insertAll(persons = persons)
                                    withContext(Main) { fileEvent.value = Event(ACTION_FINISH_IMPORT_FILE) }
                                }
                            }
                    } catch (ex: Exception) {
                        Log.e(TAG, "Error seeding database", ex)
                        withContext(Main) { fileEvent.value = Event(ACTION_ERROR_IMPORT_FILE) }
                    }
                }
            }
        }
    }

}