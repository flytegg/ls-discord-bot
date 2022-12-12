package com.learnspigot.bot.http


class FinanceService : HttpService() {

    fun getPriceOfStock(symbol: String): String {
        return sendJsonRequest(buildRequest("https://query1.finance.yahoo.com/v11/finance/quoteSummary/$symbol?modules=financialData"))
            .body().asJsonObject["quoteSummary"].asJsonObject["result"].asJsonArray[0]
            .asJsonObject["financialData"].asJsonObject["currentPrice"].asJsonObject["fmt"].asString
    }
}