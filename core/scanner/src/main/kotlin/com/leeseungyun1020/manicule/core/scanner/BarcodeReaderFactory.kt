package com.leeseungyun1020.manicule.core.scanner

import java.util.concurrent.Executor

fun interface BarcodeReaderFactory {
    fun create(executor: Executor): BarcodeReader
}
