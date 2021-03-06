package com.nshumskii.lab1.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
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


class ListViewModel(application: Application) : AndroidViewModel(application) {

    private var personRepository =
        PersonRepository(AppDatabase.getInstance(application).personDataDao())

    var persons: MutableLiveData<List<PersonData>> = MutableLiveData()

    var navEvent: MutableLiveData<Event<Int>> = MutableLiveData()

    var personsList: List<PersonData>? = null

    private val personsObserver: Observer<List<PersonData>> = Observer {
        if (it.isNullOrEmpty()) {
            navEvent.value = Event(R.id.action_listFragment_to_emptyFragment)
        } else {
            persons.value = it
            personsList = it
        }
    }

    init {
        personRepository.getContacts().observeForever(personsObserver)
    }

    fun search(target: String) {
        CoroutineScope(IO).launch {
            val search = personsList?.filter {
                (it.firstname + " " + it.lastname).toLowerCase().contains(target.toLowerCase())
            }
            withContext(Main) {
                persons.value = search
            }
        }
    }

    fun sort() {
        CoroutineScope(IO).launch {
            val sort = personsList?.sortedWith(compareBy(PersonData::lastname))
            withContext(Main) { persons.value = sort }
        }
    }

    fun deleteAll() = CoroutineScope(IO).launch { personRepository.deleteAll() }

    fun insertNew() {
        navEvent.value = Event(R.id.action_listFragment_to_editFragment)
    }

    override fun onCleared() {
        super.onCleared()
        personRepository.getContacts().removeObserver(personsObserver)
    }

}
