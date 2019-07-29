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

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * ----- 3. BÖLÜM -----
 * Bu bölümde Entity ve Dao nun kullanılacağı veritabanı oluşturulacaktır.
 * Singleton tasarım prensini kullanılmıştır. Çünkü bir adet veritabanı örneği üzerinden işlem yapmak
 * performans açısından fayda sağlayacaktır. Genel bir kalıptır. Farklı veritabanları da kullanabilir.
 */

@Database(entities = [SleepNight::class], version = 1, exportSchema = false)
abstract class SleepDatabase : RoomDatabase() {

    // Veritabanının Dao hakkında bilgisi olabilir gerekir.
    // Birden fazla dao olabilir.
    abstract val sleepDatabaseDao: SleepDatabaseDao

    // Veritabanından nesne oluşturmadan direkt olarak sınıf adıyla çağırmak için
    // companion object oluşturuyoruz.

    companion object {
        /**
         * Tüm iş parçacıklarında tek bir VERİTABANI örneğinin kullanılacağının belirtirmidir.
         */
        @Volatile
        private var INSTANCE: SleepDatabase? = null

        /**
         * Uygulama bağlamında VERİTABANI örneğinin döndürülmesini sağlayan metod.
         */
        fun getInstance(context: Context): SleepDatabase {

            // Context aktarılıyor.
            // Birden fazla iş parçacağı aynı anda veritabanı örneği isteyebilir.
            // Bunu geçmek adına yaptığımız işlem synchronized ' tir.
            synchronized(this) {

                var instance = INSTANCE

                if (instance == null) {

                    instance = Room.databaseBuilder(
                            context.applicationContext,
                            SleepDatabase::class.java,
                            "sleep_history_database")
                            .fallbackToDestructiveMigration()
                            .build()

                    INSTANCE = instance

                }

                return instance

            }


        }


    }


}