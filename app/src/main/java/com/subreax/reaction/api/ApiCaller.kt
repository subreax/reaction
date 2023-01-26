package com.subreax.reaction.api

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

sealed class ApiResult<out T> {
    class Success<out T>(val value: T) : ApiResult<T>()
    class Error(val code: Int, val message: String) : ApiResult<Nothing>()
    object NetworkError : ApiResult<Nothing>()

    fun <X> convert(converter: (T) -> X): ApiResult<X> {
        return when (this) {
            is Success -> {
                Success<X>(converter(this.value))
            }
            is Error -> {
                this
            }
            else -> {
                this as NetworkError
            }
        }
    }

    fun errorToString(): String {
        return when (this) {
            is Error -> {
                "Ошибка: $message"
            }
            is NetworkError -> {
                "Ошибка соединения с сервером"
            }
            else -> {
                ""
            }
        }
    }
}

suspend fun <T> safeApiCall(
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    call: suspend () -> T
): ApiResult<T> {
    return withContext(dispatcher) {
        try {
            ApiResult.Success(call())
        } catch (th: Throwable) {
            when (th) {
                is HttpException -> {
                    parseHttpException(th)
                }
                is IOException -> {
                    ApiResult.NetworkError
                }
                else -> {
                    ApiResult.Error(-1, "Unknown error")
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

private fun parseHttpException(ex: HttpException): ApiResult.Error {
    val bodyJson = ex.response()?.errorBody()?.string() ?: "{}"

    return try {
        val json = JSONObject(bodyJson)
        val code = json.getInt("statusCode")
        val messageObj = json.get("message")
        val message = if (messageObj is JSONArray) {
            messageObj[0].toString()
        } else {
            messageObj.toString()
        }
        ApiResult.Error(code, message)

        /*val body = Gson().fromJson(bodyJson, HttpExceptionBody::class.java)
        ApiResult.Error(body.statusCode, body.message)*/
    } catch (ex: Exception) {
        ApiResult.Error(-1, "Failed to parse error body: $bodyJson")
    }
    //return ApiResult.Error(code = -1, message = "Unknown http error")
}
