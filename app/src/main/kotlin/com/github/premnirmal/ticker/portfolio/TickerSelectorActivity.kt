package com.github.premnirmal.ticker.portfolio

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ListView
import com.github.premnirmal.ticker.BaseActivity
import com.github.premnirmal.ticker.CrashLogger
import com.github.premnirmal.ticker.InAppMessage
import com.github.premnirmal.ticker.Injector
import com.github.premnirmal.ticker.SimpleSubscriber
import com.github.premnirmal.ticker.Tools
import com.github.premnirmal.ticker.model.IStocksProvider
import com.github.premnirmal.ticker.network.SuggestionApi
import com.github.premnirmal.ticker.network.data.Suggestion
import com.github.premnirmal.tickerwidget.R
import kotlinx.android.synthetic.main.activity_ticker_selector.toolbar
import io.reactivex.Subscription
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * Created by premnirmal on 2/25/16.
 */
class TickerSelectorActivity : BaseActivity() {

  @Inject
  lateinit internal var suggestionApi: SuggestionApi

  @Inject
  lateinit internal var stocksProvider: IStocksProvider

  internal var subscription: Subscription? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Injector.inject(this)
    setContentView(R.layout.activity_ticker_selector)
    updateToolbar(toolbar)
    toolbar.setNavigationOnClickListener {
      finish()
    }

    val searchView = findViewById(R.id.query) as EditText
    val listView = findViewById(R.id.resultList) as ListView

    searchView.addTextChangedListener(object : TextWatcher {
      override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

      }

      override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

      }

      override fun afterTextChanged(s: Editable) {
        val query = s.toString().trim { it <= ' ' }.replace(" ".toRegex(), "")
        if (!query.isEmpty()) {
          subscription?.unsubscribe()
          if (Tools.isNetworkOnline(applicationContext)) {
            val observable = suggestionApi.getSuggestions(query)
            subscription = bind(observable)
                .map { suggestions -> suggestions.ResultSet?.Result }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(object : SimpleSubscriber<List<Suggestion>>() {
                  override fun onError(e: Throwable) {
                    CrashLogger.logException(e)
                    InAppMessage.showMessage(this@TickerSelectorActivity,
                        R.string.error_fetching_suggestions)
                  }

                  override fun onNext(result: List<Suggestion>) {
                    val suggestionList = result
                    listView.adapter = SuggestionsAdapter(suggestionList)
                  }
                })
          } else {
            InAppMessage.showMessage(this@TickerSelectorActivity, R.string.no_network_message)
          }
        }
      }
    })

    listView.setOnItemClickListener({ parent, view, position, id ->
      val suggestionsAdapter = parent.adapter as SuggestionsAdapter
      val suggestion: Suggestion = suggestionsAdapter.getItem(position)
      val ticker = suggestion.symbol
      if (!stocksProvider.getTickers().contains(ticker)) {
        stocksProvider.addStock(ticker)
        InAppMessage.showMessage(this@TickerSelectorActivity, ticker + " added to list")
        // don't allow positions for indices
        if (!ticker.startsWith("^") && !ticker.contains("=") && !ticker.startsWith(".")) {
          showDialog("Do you want to add positions for $ticker?",
              true,
              DialogInterface.OnClickListener { dialog, which ->
                val intent = Intent(this@TickerSelectorActivity, AddPositionActivity::class.java)
                intent.putExtra(EditPositionActivity.TICKER, ticker)
                startActivity(intent)
              },
              DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() })
        }
      } else {
        showDialog("$ticker is already in your portfolio")
      }
    })

  }

}