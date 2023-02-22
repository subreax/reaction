package com.subreax.reaction.api

import com.subreax.reaction.R
import com.subreax.reaction.utils.Return
import com.subreax.reaction.utils.ReturnCode
import com.subreax.reaction.utils.UiText
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

suspend fun <T> safeApiCall(
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    call: suspend () -> T
): Return<T> {
    return withContext(dispatcher) {
        try {
            Return.Ok(call())
        } catch (th: Throwable) {
            when (th) {
                is HttpException -> {
                    parseHttpException(th)
                }
                is IOException -> {
                    Return.Fail(UiText.Res(R.string.connection_error), ReturnCode.NetworkError)
                }
                else -> {
                    Return.Fail(UiText.Res(R.string.unknown_error))
                }
            }
        }
    }
}

suspend fun <T> unsafeApiCall(
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    call: suspend () -> T
): T {
    return withContext(dispatcher) {
        call()
    }
}

private fun parseHttpException(ex: HttpException): Return.Fail {
    val bodyJson = ex.response()?.errorBody()?.string() ?: "{}"

    return try {
        val json = JSONObject(bodyJson)
        val messageObj = json.get("message")
        val message = if (messageObj is JSONArray) {
            messageObj[0].toString()
        } else {
            messageObj.toString()
        }
        Return.Fail(UiText.Hardcoded(message), ReturnCode.BadRequest)
    } 
    catch (ex: Exception) {
        Return.Fail(UiText.Hardcoded("Failed to parse error body: $bodyJson"), ReturnCode.ServerError)
    }
}
