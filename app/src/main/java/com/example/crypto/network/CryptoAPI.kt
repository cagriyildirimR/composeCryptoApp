package com.example.crypto.network

import kotlinx.serialization.*


@Serializable
data class CoinEntity(
    val id: String?,
    val symbol: String?,
    val name: String?,
    val image: String?,
    val current_price: Double?,
    val market_cap: Double?,
    val market_cap_rank: Int?,
    val fully_diluted_valuation: Double?,
    val total_volume: Double?,
    val high_24h: Double?,
    val low_24h: Double?,
    val price_change_24h: Double?,
    val price_change_percentage_24h: Double?,
    val market_cap_change_24h: Double?,
    val market_cap_change_percentage_24h: Double?,
    val circulating_supply: Double?,
    val total_supply: Double?,
    val max_supply: Double?,
    val ath: Double?,
    val ath_change_percentage: Double?,
    val ath_date: String?,
    val atl: Double?,
    val atl_change_percentage: Double?,
    val atl_date: String?,
    val roi: Roi?,
    val last_updated: String?
)

@Serializable
class Roi(
    val times: Double?,
    val currency: String?,
    val percentage: Double?
)

@Serializable
data class RangeData(
    val prices: List<List<Double>>,
    val market_caps: List<List<Double>>,
    val total_volumes: List<List<Double>>
)

// debugging purposes
val btc = CoinEntity(
    id = "bitcoin",
    symbol = "btc",
    name = "Bitcoin",
    image = "https://assets.coingecko.com/coins/images/1/large/bitcoin.png?1547033579",
    current_price = 46864.0,
    market_cap = 885132774398.0,
    market_cap_rank = 1,
    fully_diluted_valuation = null,
    total_volume = null,
    high_24h = 49137.0,
    low_24h = 46002.0,
    price_change_24h = -1962.04,
    price_change_percentage_24h = -4.0184,
    market_cap_change_24h = null,
    market_cap_change_percentage_24h = null,
    circulating_supply = null,
    total_supply = null,
    max_supply = null,
    ath = null,
    ath_change_percentage = null,
    ath_date = null,
    atl = null,
    atl_change_percentage = null,
    atl_date = null,
    roi = null,
    last_updated = null
)