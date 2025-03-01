package otus.homework.coroutines

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.lang.Exception
import java.net.SocketTimeoutException

class CatsPresenter(
    private val catsService: CatsService,
    private val catsImageService: CatImageService
) {

    private var _catsView: ICatsView? = null
    private val scope = PresenterScope()


    fun onInitComplete() {
        scope.launch {

                val fact = async { catsService.getCatFact().fact }
                val url = async { catsImageService.getImage()[0].url }

                try {
                    _catsView?.populate(
                        CatData(
                        fact = fact.await(),
                        url = url.await()
                        )
                    )

                } catch (exc: Exception) {
                    when(exc) {
                        is SocketTimeoutException ->
                            _catsView?.showToast("Не удалось получить ответ от сервера")
                        is CancellationException ->
                            throw exc
                        else -> {
                            val msg = exc.message.toString()
                            CrashMonitor.trackWarning(msg)
                            _catsView?.showToast(msg)
                        }
                    }
            }
        }
    }

    fun attachView(catsView: ICatsView) {
        _catsView = catsView
    }

    fun detachView() {
        scope.cancel()
    }

}