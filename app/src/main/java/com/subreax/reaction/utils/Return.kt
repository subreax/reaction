package com.subreax.reaction.utils

enum class ReturnCode {
    Ok, NetworkError, ServerError, BadRequest, Unspecified
}

sealed class Return<out T>(val code: ReturnCode) {
    class Ok<T>(val value: T) : Return<T>(ReturnCode.Ok)
    class Fail(val message: UiText, code: ReturnCode = ReturnCode.Unspecified) : Return<Nothing>(code)
}

data class ReturnHolder<T>(var ret: Return<T>)
