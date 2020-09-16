package bzh.zelyon.common.util.api

import retrofit2.Call
import retrofit2.Response

class CallError<T>(call: Call<T>, response: Response<T>? = null, throwable: Throwable? = null) {

    var httpCode = 0
    var message = ""
    var method = ""
    var url = ""

    init {
        method = call.request().method
        url = call.request().url.toUrl().toString()

        response?.let {
            httpCode = response.code()
            message = response.message()
            response.errorBody()?.string()
        }
        throwable?.let {
            message = throwable.localizedMessage ?: throwable.message.orEmpty()
        }
    }
}