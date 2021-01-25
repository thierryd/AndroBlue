package androblue.app.di.component

import androblue.app.advanced.AdvancedActivity
import androblue.common.dagger.ScopeActivity
import dagger.Subcomponent
import dagger.android.AndroidInjector

@Subcomponent
@ScopeActivity
interface AdvancedActivitySubComponent : AndroidInjector<AdvancedActivity> {

    @Subcomponent.Factory
    interface Factory : AndroidInjector.Factory<AdvancedActivity>
}