package androblue.app.di.module

import androblue.app.repository.db.VehicleDao
import androblue.app.repository.db.VehicleDb
import androblue.common.dagger.ScopeApplication
import androblue.common.log.db.LoggerDao
import androblue.common.log.db.LoggerDb
import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides

@Suppress("unused")
@Module
object AndroBlueApplicationProvideModule {

    @Provides
    @ScopeApplication
    internal fun provideLoggerDao(application: Application): LoggerDao {
        return Room
                .databaseBuilder(application, LoggerDb::class.java, "log_db")
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build()
                .loggerDao()
    }

    @Provides
    @ScopeApplication
    internal fun provideVehicleDao(application: Application): VehicleDao {
        return Room
                .databaseBuilder(application, VehicleDb::class.java, "vehicle_db")
                .build()
                .vehicleDao()
    }
}