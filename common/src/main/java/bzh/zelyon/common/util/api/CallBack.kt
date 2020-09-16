package bzh.zelyon.common.util.api

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

interface CallBack<T>: Callback<T> {
    override fun onResponse(call: Call<T>, response: Response<T>) {
        if (response.isSuccessful) {
            response.body()?.let {
                onSuccess(it)
            } ?: onFail(CallError(call, response))
        } else {
            onFail(CallError(call, response))
        }
    }

    override fun onFailure(call: Call<T>, throwable: Throwable) {
        onFail(CallError(call, throwable = throwable))
    }

    fun onSuccess(response: T)

    fun onFail(callError: CallError<T>) {}
}