package com.example.crypto.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.crypto.HistoryRanges
import com.example.crypto.Status
import com.example.crypto.network.CoinEntity
import com.example.crypto.network.RangeData
import com.example.crypto.network.btc
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*

private const val TIME_OUT = 60_000

const val mainURL = "https://api.coingecko.com/api/v3/coins/markets?" +
        "vs_currency=usd&order=market_cap_desc&per_page=100&page=1&sparkline=false"


class CryptoViewModel: ViewModel() {

//     client with Android engine using Kotlinx serializer
    val client = HttpClient(Android) {
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }

        engine {
            connectTimeout = TIME_OUT
            socketTimeout = TIME_OUT
        }
    }

    // these are the top 100 most popular coins from gecko api, ordered by descending popularity
    private val _listOfCoins = MutableLiveData<List<CoinEntity>>()
    val listOfCoins: LiveData<List<CoinEntity>> = _listOfCoins

    // if true we are in detail fragment
    private val _detailView = MutableLiveData<Boolean>(false)
    val detailView: LiveData<Boolean> = _detailView

    private val _coinDetail = MutableLiveData<CoinEntity>(btc)
    val coinDetail: LiveData<CoinEntity> = _coinDetail

    private val _coinHistoryData = MutableLiveData<RangeData>()
    val coinHistoryData: LiveData<RangeData> = _coinHistoryData

    private val _status = MutableLiveData<Status>(Status.LOADING)
    val status: LiveData<Status> = _status

    private val _selectedHistoryButton = MutableLiveData<HistoryRanges>(HistoryRanges.THREEMONTHS)
    val selectedHistoryButton: LiveData<HistoryRanges> = _selectedHistoryButton


    fun select(rangeDate: HistoryRanges){
        _selectedHistoryButton.value = rangeDate
    }

    /**
     * After event of pressing to crypto card we load given crypto data
     */
    suspend fun loadCoinHistoryData(coin: CoinEntity, days: Int = 90) {
        _status.value = Status.LOADING
        _coinDetail.value = coin
        _coinHistoryData.value = client.get("https://api.coingecko.com/api/v3/coins/${coin.id}/market_chart?vs_currency=usd&days=${days}")
        _status.value = Status.DONE
    }

    /**
     * If detailView is true [DetailView] Composable covers the entire screen
     * otherwise we hide this Composable.
     */
    fun openDetail() {
        _detailView.value = true
    }

    fun closeDetail(){
        _detailView.value = false
    }

    /**
     * Loading 100 most popular crypto info. [mainURL] is final.
     */
    suspend fun loadMainData() {
        _status.value = Status.LOADING
        _listOfCoins.value = client.get(mainURL)
        _status.value = Status.DONE
    }
}
