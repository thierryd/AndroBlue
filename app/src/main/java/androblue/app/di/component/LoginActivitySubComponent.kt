package androblue.app.di.component

import androblue.app.login.LoginActivity
import androblue.common.dagger.ScopeActivity
import dagger.Subcomponent
import dagger.android.AndroidInjector

@Subcomponent
@ScopeActivity
interface LoginActivitySubComponent : AndroidInjector<LoginActivity> {

    @Subcomponent.Factory
    interface Factory : AndroidInjector.Factory<LoginActivity>
}