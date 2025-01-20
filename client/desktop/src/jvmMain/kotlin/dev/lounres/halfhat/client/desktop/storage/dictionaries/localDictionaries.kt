package dev.lounres.halfhat.client.desktop.storage.dictionaries

import dev.lounres.halfhat.client.localStorage.sql.AppDatabase
import dev.lounres.kone.collections.interop.toKoneReifiedSet
import dev.lounres.kone.collections.iterables.KoneIterable
import dev.lounres.kone.collections.iterables.next
import dev.lounres.kone.collections.set.KoneReifiedSet
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class LocalDictionary(
    val name: String,
    private val appDatabase: AppDatabase,
) {
    val allWords: KoneReifiedSet<String>
        get() = appDatabase.localDictionariesQueries.getDictionaryWordsByName(name = name).executeAsList().toKoneReifiedSet()
}

class LocalDictionariesRegistry(val appDatabase: AppDatabase): SynchronizedObject() {
    val dictionaryNames: StateFlow<KoneReifiedSet<String>> =
        appDatabase
            .localDictionariesQueries
            .getDictionaryNames()
            .let {
                val flow = MutableStateFlow(it.executeAsList().toKoneReifiedSet())
                it.addListener { flow.value = it.executeAsList().toKoneReifiedSet() }
                flow
            }
    
    fun getDictionaryByName(name: String): LocalDictionary =
        appDatabase
            .localDictionariesQueries
            .getDictionaryDescriptionByName(name = name)
            .executeAsOne()
            .let {
                LocalDictionary(
                    name = it,
                    appDatabase = appDatabase,
                )
            }
    
    fun createDictionary(name: String, allWords: KoneIterable<String>) {
        appDatabase.transaction {
            appDatabase.localDictionariesQueries.createDictionaryDescription(name = name)
            for (word in allWords)
                appDatabase.localDictionariesQueries.insertWordIntoDictionary(name = name, word = word)
        }
    }
}