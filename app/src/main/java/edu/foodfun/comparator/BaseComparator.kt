package edu.foodfun.comparator

abstract class BaseComparator<T> {

    abstract fun compare(oldValue: T?, newValue: T)
}