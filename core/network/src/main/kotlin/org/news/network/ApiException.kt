package org.news.network

import org.news.network.model.ApiError

class ApiException(val error: ApiError) : Exception(error.message)