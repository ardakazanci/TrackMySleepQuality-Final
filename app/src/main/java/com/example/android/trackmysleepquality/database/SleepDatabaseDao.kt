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
package com.example.android.trackmysleepquality.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

/**
 * ----- 2. BÖLÜM -----
 * Dao aracılığıyla oluşturduğumuz veritabanında ki sütun alanlarını kullanarak SQL Ortak işlemlerini
 * veritabanı üzerinde yapabiliyoruz. @Insert @Delete @Update @Query gibi.
 * @Delete kullanmamızın sebebi belirli spesifik bir değer vermemiz gerektiği biz ise veritabanını temizledik.
 *
 */
@Dao
interface SleepDatabaseDao {

    // Ekleme işlemi. Room aracılığıyla. Dao kullanarak veritabanına ekleme yapar.
    @Insert
    fun insert(night: SleepNight)

    // Güncelleme işlemi. Room aracılığıyla. Dao kullanarak veritanında ki aldıki nesne güncellenir.
    @Update
    fun update(night: SleepNight)

    // Sorgu İşlemi : İlgili id ' ye sahip sleepnight nesnesini döndürür. key ve fun key aynı olmalıdır.
    @Query("SELECT * from daily_sleep_quality_table WHERE nightId = :key")
    fun get(key: Long): SleepNight?

    // Temizleme işlemi : Veritabanını temizler.
    @Query("DELETE FROM daily_sleep_quality_table")
    fun clear()

    // Sorgu işlemi : En son eklenen gece bilgisini döndürür. Azalan sıralama yapılmıştır.
    @Query("SELECT * FROM daily_sleep_quality_table ORDER BY nightId DESC LIMIT 1")
    fun getTonight(): SleepNight?

    // Sorgu işlemi : En güncel tüm sonuçlar livedata olarak alınmıştır.
    // Tüm notlar olabilir. Tüm öğrenci kayıtları olabilir. Tüm maç sonuçları olabilir vs.
    @Query("SELECT * FROM daily_sleep_quality_table ORDER BY nightId DESC")
    fun getAllNights(): LiveData<List<SleepNight>>

}

