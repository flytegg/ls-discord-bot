package com.learnspigot.bot.http

class FinanceService : HttpService() {

    override val endpoint: String = "query1.finance.yahoo.com"
    override val pathPrefix: String = "/v11/finance"

    fun getPriceOfStock(symbol: String): String {
        return sendHttpRequest("/quoteSummary/$symbol?modules=financialData")
            .bodyJson()
            .asJsonObject["quoteSummary"]
            .asJsonObject["result"]
            .asJsonArray[0]
            .asJsonObject["financialData"]
            .asJsonObject["currentPrice"]
            .asJsonObject["fmt"]
            .asString
    }
}