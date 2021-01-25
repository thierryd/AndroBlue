package androblue.app.di.module

import android.app.Application
import android.content.Context
import dagger.Binds
import dagger.Module

@Suppress("unused")
@Module
internal interface AndroBlueApplicationBindingModule {

    @Binds
    fun context(application: Application): Context
}

