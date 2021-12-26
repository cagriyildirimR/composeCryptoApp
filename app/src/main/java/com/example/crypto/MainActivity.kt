package com.example.crypto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.ButtonDefaults.buttonColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import coil.transform.CircleCropTransformation
import com.example.crypto.network.CoinEntity
import com.example.crypto.network.btc
import com.example.crypto.ui.theme.CryptoTheme
import com.example.crypto.viewmodel.CryptoViewModel
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import com.example.crypto.detail.Graph
import com.example.crypto.detail.btcRange
import com.example.crypto.network.RangeData
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<CryptoViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            val listOfCoins: List<CoinEntity> by viewModel.listOfCoins.observeAsState(listOf(btc))
            val detailView: Boolean by viewModel.detailView.observeAsState(false)
            val rangeData: RangeData by viewModel.coinHistoryData.observeAsState(btcRange)
            val status: Status by viewModel.status.observeAsState(Status.LOADING)
            val selected by viewModel.selectedHistoryButton.observeAsState(HistoryRanges.THREEMONTHS)

            viewModel.viewModelScope.launch {
                viewModel.loadMainData()
            }

            CryptoTheme {
                Surface(color = MaterialTheme.colors.background) {

                    MainView(viewModel = viewModel, listOfCoins = listOfCoins)

                    DetailView(
                        viewModel = viewModel,
                        visible = detailView,
                        range = rangeData,
                        selected = selected
                    ) {
                        viewModel.closeDetail()
                    }
                    LoadingView(status == Status.LOADING)
                }
            }
        }
    }

    override fun onDestroy() {
        viewModel.client.close()
        super.onDestroy()
    }

}

@Composable
fun MainView(viewModel: CryptoViewModel, listOfCoins: List<CoinEntity>) {

    LazyColumn {
        items(listOfCoins) { item: CoinEntity ->
            CoinView(coin = item) {
                viewModel.openDetail()
                viewModel.viewModelScope.launch {
                    viewModel.loadCoinHistoryData(item)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainViewPreview() {

    MainView(viewModel = CryptoViewModel(), listOfCoins = List(20) { btc })
}

@Composable
fun CoinView(coin: CoinEntity, onClick: () -> Unit) {

    val coinInfo = "${coin.name}: ${coin.current_price}\$ (${coin.price_change_percentage_24h}%) "
    val isCoinUp = coin.price_change_percentage_24h ?: 0.0 >= 0.0 //
    val color = if (isCoinUp) Color.Green else Color.Red

    Card(modifier =
    Modifier
        .padding(8.dp)
        .fillMaxWidth()
        .clickable { onClick() }) {

        Row(verticalAlignment = Alignment.CenterVertically) {

            Icon(
                painter = if (isCoinUp) painterResource(id = R.drawable.trending_up_black_24dp)
                else painterResource(
                    id = R.drawable.trending_down_black_24dp
                ),
                contentDescription = "current value is up",
                tint = color
            )

            Spacer(modifier = Modifier.padding(4.dp))

            CoinImage(url = coin.image)

            Text(
                text = coinInfo,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = color
            )
        }
    }
}

@Composable
fun CoinImage(url: String?){
    Image(
        painter = rememberImagePainter(
            data = url,
            builder = {
                crossfade(true)
                placeholder(R.drawable.rocket_launch_black_24dp)
                transformations(CircleCropTransformation())
            },
        ),

        contentDescription = "coin logo",
        modifier = Modifier.size(64.dp)
    )
}

/**
 * Like Fragment, if user selects any crypto [DetailView] will fill the screen
 */
@Composable
fun DetailView(
    viewModel: CryptoViewModel,
    visible: Boolean,
    range: RangeData,
    selected: HistoryRanges,
    onClick: () -> Unit
) {
    if (visible) {
        Surface(
            color = MaterialTheme.colors.background,
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp)
            ) {

                IconButton(onClick = { onClick() }) {
                    Icon(
                        Icons.Default.KeyboardArrowLeft,
                        contentDescription = "back button",
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.padding(2.dp))

                DetailHead(viewModel = viewModel)

                Graph(d = range)

                HistoryRangeButtons(
                    viewModel = viewModel, modifier = Modifier
                        .weight(1f)
                        .padding(4.dp), selected = selected
                )

            }
        }
    }
}

@Composable
fun DetailHead(viewModel: CryptoViewModel) {
    val isCoinUp = viewModel.coinDetail.value?.price_change_24h ?: 0.0 > 0.0

    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {

        // Logo of selected crypto
        CoinImage(url = viewModel.coinDetail.value?.image)

        Spacer(modifier = Modifier.padding(8.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = viewModel.coinDetail.value?.name ?: "Error",
                fontSize = 28.sp
            )

            Spacer(modifier = Modifier.padding(2.dp))

            Text(
                text = "\$${viewModel.coinDetail.value?.current_price}",
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.padding(2.dp))

            Text(
                text = "\$${viewModel.coinDetail.value?.price_change_24h} " +
                        "(${viewModel.coinDetail.value?.price_change_percentage_24h}%)",
                color = if (isCoinUp) Color.Green else Color.Red,
                fontSize = 12.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DetailHeadPreview(){

    DetailHead(viewModel = CryptoViewModel())
}

enum class HistoryRanges(val day: Int, val text: String) {
    ONE(1, "1D"),
    WEEK(7, "1W"),
    MONTH(30, "1M"),
    THREEMONTHS(90, "3M"),
    YEAR(365, "1Y")
}

/**
 * RangeDate is allow us to select date range for selected crypto graph.
 * e.g. one day, one week etc.
 */
@Composable
fun HistoryRangeButtons(viewModel: CryptoViewModel, modifier: Modifier, selected: HistoryRanges) {
    Row {
        for (r in HistoryRanges.values()) {
            Button(
                onClick = {
                    viewModel.viewModelScope.launch {
                        viewModel.loadCoinHistoryData(viewModel.coinDetail.value!!, r.day)
                        viewModel.select(r)
                    }
                }, modifier = modifier,
                colors = buttonColors(
                    backgroundColor =
                    if (selected == r) MaterialTheme.colors.primary
                    else Color.White
                )
            ) {
                Text(text = r.text)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DetailViewPreview() {
    val viewModel = CryptoViewModel()
    DetailView(viewModel, true, btcRange, HistoryRanges.THREEMONTHS) {
    }
}

@Preview(showBackground = true)
@Composable
fun CoinViewPreview() {
    CoinView(btc) { }
}
