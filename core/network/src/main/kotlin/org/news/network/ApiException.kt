package org.news.network

import org.news.network.model.ApiErrorResponse

class ApiException(val error: ApiErrorResponse) : Exception(error.message)