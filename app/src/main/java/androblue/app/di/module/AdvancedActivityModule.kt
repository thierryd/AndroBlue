package androblue.app.di.module

import androblue.app.advanced.AdvancedActivity
import androblue.app.di.component.AdvancedActivitySubComponent
import dagger.Binds
import dagger.Module
import dagger.android.AndroidInjector
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap

@Suppress("unused")
@Module(subcomponents = [AdvancedActivitySubComponent::class])
abstract class AdvancedActivityModule {

    @Binds
    @IntoMap
    @ClassKey(AdvancedActivity::class)
    abstract fun bind(builder: AdvancedActivitySubComponent.Factory): AndroidInjector.Factory<*>
}