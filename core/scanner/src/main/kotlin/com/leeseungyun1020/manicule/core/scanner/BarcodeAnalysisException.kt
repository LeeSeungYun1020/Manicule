package com.leeseungyun1020.manicule.core.scanner

class BarcodeAnalysisException(
    cause: Throwable,
) : Exception("Barcode analysis failed", cause)
