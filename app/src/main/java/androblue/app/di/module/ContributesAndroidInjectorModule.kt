package androblue.app.di.module

import androblue.app.SplashActivity
import androblue.app.widget.AndroBlueWidget
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Suppress("unused")
@Module
abstract class ContributesAndroidInjectorModule {

    @ContributesAndroidInjector
    internal abstract fun androBlueWidget(): AndroBlueWidget

    @ContributesAndroidInjector
    internal abstract fun splashActivity(): SplashActivity
}