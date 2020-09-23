package io.getstream.chat.android.livedata.utils

import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import io.getstream.chat.android.client.utils.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

interface Call2<T> {

    @WorkerThread
    fun execute(): Result<T>

    @UiThread
    fun enqueue(callback: (Result<T>) -> Unit = {})

    fun cancel()
    suspend fun invoke(): Result<T>
}

class CallImpl2<T>(var runnable: suspend () -> Result<T>, var scope: CoroutineScope = GlobalScope) :
    Call2<T> {
    var canceled: Boolean = false

    override fun cancel() {
        canceled = true
    }

    override fun execute(): Result<T> {
        val result = runBlocking(scope.coroutineContext) { runnable() }
        return result
    }

    override suspend fun invoke(): Result<T> = withContext(scope.coroutineContext) {
        runnable()
    }

    override fun enqueue(callback: (Result<T>) -> Unit) {
        scope.launch(scope.coroutineContext) {
            if (!canceled) {
                val result = runnable()
                callback(result)
            }
        }
    }
}
