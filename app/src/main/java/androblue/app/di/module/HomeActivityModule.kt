package androblue.app.di.module

import androblue.app.HomeActivity
import androblue.app.di.component.HomeActivitySubComponent
import dagger.Binds
import dagger.Module
import dagger.android.AndroidInjector
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap

@Suppress("unused")
@Module(subcomponents = [HomeActivitySubComponent::class])
abstract class HomeActivityModule {

    @Binds
    @IntoMap
    @ClassKey(HomeActivity::class)
    abstract fun bind(builder: HomeActivitySubComponent.Factory): AndroidInjector.Factory<*>
}