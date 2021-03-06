package com.nshumskii.lab1.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.nshumskii.lab1.R
import com.nshumskii.lab1.data.AppDatabase
import com.nshumskii.lab1.data.PersonData
import com.nshumskii.lab1.data.PersonRepository
import com.nshumskii.lab1.models.Event
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ContactViewModel(application: Application) : AndroidViewModel(application) {

    private var personRepository =
        PersonRepository(AppDatabase.getInstance(application).personDataDao())

    var person: MutableLiveData<PersonData> = MutableLiveData()

    var navEvent: MutableLiveData<Event<Int>> = MutableLiveData()

    var msgEvent: MutableLiveData<Event<String>> = MutableLiveData()

    fun fetchPerson(id: Long) {
        CoroutineScope(IO).launch {
            var selectedPerson = personRepository.getPersonById(id)
            if (selectedPerson == null) {
                selectedPerson = personRepository.selectLast()
            }
            withContext(Main) {
                person.value = selectedPerson
            }
        }
    }

    fun delete() {
        CoroutineScope(IO).launch {
            person.value?.let { personRepository.delete(it) }
            withContext(Main) {
                msgEvent.value =
                    Event(getApplication<Application>().getString(R.string.contact_removed))
                navEvent.value = Event(R.id.action_contactFragment_to_listFragment)
            }
        }
    }

    fun update() {
        navEvent.value = Event(R.id.action_contactFragment_to_editFragment)
    }

    fun home() {
        navEvent.value = Event(R.id.action_contactFragment_to_listFragment)
    }

}
