package fi.efelantti.frisbeegolfer

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer

fun <T> LiveData<T>.observeOnce(observer: (T) -> Unit) {
    observeForever(object : Observer<T> {
        override fun onChanged(value: T) {
            removeObserver(this)
            observer(value)
        }
    })
}

fun <T> LiveData<T>.observeOnce(owner: LifecycleOwner, observer: (T) -> Unit) {
    observe(owner, object : Observer<T> {
        override fun onChanged(value: T?) {
            if (value != null) {
                removeObserver(this)
                observer(value)
            }
        }
    })
}

// MediatorLiveData approach
fun <A, B, C, D> combine(
    liveData1: LiveData<A>,
    liveData2: LiveData<B>,
    liveData3: LiveData<C>,
    onChanged: (A?, B?, C?) -> D
): MediatorLiveData<D> {
    return MediatorLiveData<D>().apply {
        addSource(liveData1) {
            value = onChanged(liveData1.value, liveData2.value, liveData3.value)
        }
        addSource(liveData2) {
            value = onChanged(liveData1.value, liveData2.value, liveData3.value)
        }
        addSource(liveData3) {
            value = onChanged(liveData1.value, liveData2.value, liveData3.value)
        }
    }
}