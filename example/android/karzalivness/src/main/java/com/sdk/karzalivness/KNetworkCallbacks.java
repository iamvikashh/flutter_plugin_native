package com.sdk.karzalivness;

public interface KNetworkCallbacks<T, E> {

    void onData(T data);

    void onError(E error);

}
