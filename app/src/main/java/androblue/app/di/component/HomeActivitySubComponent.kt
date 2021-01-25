package androblue.app.di.component

import androblue.app.HomeActivity
import androblue.common.dagger.ScopeActivity
import dagger.Subcomponent
import dagger.android.AndroidInjector

@Subcomponent
@ScopeActivity
interface HomeActivitySubComponent : AndroidInjector<HomeActivity> {

    @Subcomponent.Factory
    interface Factory : AndroidInjector.Factory<HomeActivity>
}