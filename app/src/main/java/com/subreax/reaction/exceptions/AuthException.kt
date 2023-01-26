package com.subreax.reaction.exceptions

open class AuthException(msg: String) : Exception(msg)
class SignInException : AuthException("Incorrect password or username")
class UnauthorizedException : AuthException("User is unauthorized")
