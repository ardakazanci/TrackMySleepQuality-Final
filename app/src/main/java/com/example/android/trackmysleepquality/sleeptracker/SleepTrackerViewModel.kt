/*
 * Copyright 2019, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.trackmysleepquality.sleeptracker

import android.app.Application
import android.provider.SyncStateContract.Helpers.update
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.database.SleepNight
import com.example.android.trackmysleepquality.formatNights
import kotlinx.coroutines.*

/**
 * ViewModel for SleepTrackerFragment.
 *
 * @param application : Uygulama bağlamı.
 * @param database  : Veritabanı üzerinde işlemler yapabilmemizi sağlayan Dao.
 * @extend AndroidViewModel :  Application ı bir özellik olarak kullanabilmek için.
 * @see : Önemli nokta Bir adet tonight MutableLiveData değişkeni oluşturarak veritbanına ekleme
 * çıkarma gibi işlemler sonucunda elde edilecek verileri bu değişken üzerinden yapacağız.
 * Kısaca geçerli geceyi temsil eder.
 */
class SleepTrackerViewModel(
        val database: SleepDatabaseDao,
        application: Application) : AndroidViewModel(application) {



    private var _showSnackbarEvent = MutableLiveData<Boolean>()
    val showSnackBarEvent: LiveData<Boolean>
        get() = _showSnackbarEvent


    private val _navigateToSleepQuality = MutableLiveData<SleepNight>()
    val navigateToSleepQuality: LiveData<SleepNight>
        get() = _navigateToSleepQuality



    // Coroutine lerin çalışması için bir iş başlatılıyor.
    private var viewModelJob = Job()

    // Coroutine lerin çalışacağı iş parçacağı kapsamı belirtiliyor.
    // Bu örnekte Main thread.
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    // Biçimlendirilmiş verileri tutacak değişken.
    private val nights = database.getAllNights()

    // Eklenen nights verilerini fonksiyon aracılığıyla düzenliyor.
    // Util Class'ı içinde belirlenen fonksiyon aracılığıyla, string bir düzenleme sağlanıyor.
    val nightsString = Transformations.map(nights) { nights ->
        formatNights(nights, application.resources)
    }

    // İşlem yapılacak ilgili veri. 'tonight'
    // Start - Stop işlemleri sürecinde alakadar olacak ve gözlemlenecek veri budur.
    // Uygulamamızda ki bir adet SleepNight verisi üzerinde işlem yapacağımız için başta oluşturuyoruz.
    private var tonight = MutableLiveData<SleepNight?>()

    /**
     * İşlem yapılan tonight nesnesi eğer ki boş a eşitse startbutton aktif.
     */
    val startButtonVisible = Transformations.map(tonight) {
        it == null
    }
    /**
     * İşlem yapılan tonight nesnesi eğer ki boş a eşit değilse stopbutton aktif.
     */
    val stopButtonVisible = Transformations.map(tonight) {
        it != null
    }
    /**
     * veritabanında gece verileri yok ise clear button pasif olacak.
     */
    val clearButtonVisible = Transformations.map(nights) {
        it?.isNotEmpty()
    }


    // Oluşturduğumuz gibi yükleme işlemini başlatıyoruz.
    init {
        initializeTonight()
    }


    private fun initializeTonight() {
        uiScope.launch {
            // İlk değerinin atamasını yapıyoruz. veri olmadığı için şimdilik başlangıç değerde olacak.
            tonight.value = getTonightFromDatabase()
        }
    }




    // Askıya alınmış fonksiyon.
    // Veritabanı işlemi yapılacağı için suspend fonksiyon kullanıldı.
    // SleepNight nesnesi dönecek tonight a value olarak aktarılacak.
    // tonight bizim için bir adet sleepnight ' ı ifade etmektedir.
    private suspend fun getTonightFromDatabase(): SleepNight? {

        return withContext(Dispatchers.IO) {

            var night = database.getTonight()
            if (night?.endTimeMilli != night?.startTimeMilli) {
                night = null
            }
            night

        }


    }

    // Start butonuna tıklandığında yapılacak işlemler.
    fun onStartTracking() {

        uiScope.launch {
            // Kullanıcı Start butonuna tıkladığında bir adet SleepNight nesnesi oluşturulacak
            val newNight = SleepNight()
            insert(newNight)
            // İşlem yapılacak tonight değişkenine yeni değeri atıyoruz.
            // ilk başta başlatmamızın sebebi işleme alabilmek.
            tonight.value = getTonightFromDatabase()

        }

    }

    private suspend fun insert(night: SleepNight) {

        withContext(Dispatchers.IO) {
            database.insert(night)
        }

    }


    // Stop butonuna tıklandığında yapılacak işlemler.
    fun onStopTracking() {
        uiScope.launch {
            val oldNight = tonight.value ?: return@launch
            oldNight.endTimeMilli = System.currentTimeMillis() // bitiş zamanı sistem saati.
            update(oldNight)
            // İlgili gece verisi üzerinde işlem yapıyoruz.
            _navigateToSleepQuality.value = oldNight

        }
    }


    private suspend fun update(night: SleepNight) {
        withContext(Dispatchers.IO) {
            database.update(night)
        }
    }

    // Temizleme işlemi
    fun onClear() {
        uiScope.launch {
            clear()
            tonight.value = null
        }
    }

    suspend fun clear() {
        withContext(Dispatchers.IO) {
            database.clear()
        }
    }

    // ViewModel sonlandırıldığında Coroutine lerde öldürülür.
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
        _showSnackbarEvent.value = true
    }



    fun doneNavigating() {
        _navigateToSleepQuality.value = null
    }

    fun doneShowingSnackbar() {
        _showSnackbarEvent.value = false
    }


}

