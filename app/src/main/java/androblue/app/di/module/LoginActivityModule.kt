package androblue.app.di.module

import androblue.app.di.component.LoginActivitySubComponent
import androblue.app.login.LoginActivity
import dagger.Binds
import dagger.Module
import dagger.android.AndroidInjector
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap

@Suppress("unused")
@Module(subcomponents = [LoginActivitySubComponent::class])
abstract class LoginActivityModule {

    @Binds
    @IntoMap
    @ClassKey(LoginActivity::class)
    abstract fun bind(builder: LoginActivitySubComponent.Factory): AndroidInjector.Factory<*>
}