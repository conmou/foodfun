package edu.foodfun

import edu.foodfun.enums.FieldChangeType

open class ModelChanges<T>(val changeType: FieldChangeType, val value: T)