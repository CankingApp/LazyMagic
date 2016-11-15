package com.canking.inject.bindview.processor;


import com.canking.inject.bindview.Finder;

public interface AbstractInjector<T> {

    void inject(Finder finder, T target, Object source);

}
