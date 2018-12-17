package info.dvkr.screenstream

import android.app.Application
import android.util.Log
import com.crashlytics.android.Crashlytics
import info.dvkr.screenstream.di.baseKoinModule
import info.dvkr.screenstream.service.AppService
import io.fabric.sdk.android.Fabric
import org.koin.android.ext.android.startKoin
import org.koin.log.Logger
import timber.log.Timber

class ScreenStreamApp : Application() {

    override fun onCreate() {
        super.onCreate()

        Fabric.with(this, Crashlytics())

        Timber.plant(object : Timber.Tree() {
            override fun log(priority: Int, tag: String?, message: String, throwable: Throwable?) {
                if (priority == Log.VERBOSE || priority == Log.DEBUG) return
                Crashlytics.log("${tag ?: ""}:$message")
                throwable?.run { Crashlytics.logException(this) }
            }
        })

        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread: Thread, throwable: Throwable ->
            Timber.e(throwable, "Uncaught throwable in thread ${thread.name}")
            defaultHandler.uncaughtException(thread, throwable)
        }

        startKoin(this,
            listOf(baseKoinModule),
            logger = object : Logger {
                override fun debug(msg: String) = Timber.tag("Koin").d(msg)
                override fun err(msg: String) = Timber.tag("Koin").e(msg)
                override fun info(msg: String) = Timber.tag("Koin").i(msg)
            })

        AppService.startForegroundService(this)
    }
}