package androblue.app.di.component

import androblue.app.AndroBlueApplication
import androblue.app.di.module.AdvancedActivityModule
import androblue.app.di.module.AndroBlueApplicationBindingModule
import androblue.app.di.module.AndroBlueApplicationProvideModule
import androblue.app.di.module.ContributesAndroidInjectorModule
import androblue.app.di.module.HomeActivityModule
import androblue.app.di.module.LoginActivityModule
import androblue.app.os_service.AndroBlueServiceHelper
import androblue.app.widget.AndroBlueWidget
import androblue.app.work.RefreshWorker
import androblue.common.dagger.ScopeApplication
import androblue.common.log.LoggerInjectionHolder
import android.app.Application
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule

@ScopeApplication
@Component(
        modules = [
            AndroidSupportInjectionModule::class,
            ContributesAndroidInjectorModule::class,
            AndroBlueApplicationBindingModule::class,
            AndroBlueApplicationProvideModule::class,
            HomeActivityModule::class,
            LoginActivityModule::class,
            AdvancedActivityModule::class
        ]
)
interface AndroBlueApplicationComponent : AndroidInjector<AndroBlueApplication> {

    @Component.Builder
    interface Builder {

        @BindsInstance fun application(application: Application): Builder

        fun build(): AndroBlueApplicationComponent
    }

    fun inject(target: LoggerInjectionHolder)
    fun inject(androBlueServiceHelper: AndroBlueServiceHelper)
    fun inject(androBlueWidget: AndroBlueWidget)
    fun inject(refreshWorker: RefreshWorker)
}
