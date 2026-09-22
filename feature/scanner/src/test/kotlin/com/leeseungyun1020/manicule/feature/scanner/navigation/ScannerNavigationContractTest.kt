package com.leeseungyun1020.manicule.feature.scanner.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ScannerNavigationContractTest {
    @Test
    fun scannerScreenRequiresExplicitNavigationCallbacksWithoutDefaults() {
        val clazz = Class.forName("com.leeseungyun1020.manicule.feature.scanner.navigation.ScannerNavigationKt")
        val methods = clazz.declaredMethods
        val scannerScreenMethod = methods.firstOrNull { it.name == "scannerScreen" }
        requireNotNull(scannerScreenMethod) { "scannerScreen extension function not found" }

        // NavGraphBuilder 수신 객체와 세 개의 필수 navigation 콜백
        assertEquals(4, scannerScreenMethod.parameterTypes.size)

        // 기본값이 제거되었으므로 바이트코드에 합성 $default 메서드가 생성되지 않아야 함
        val defaultMethod = methods.firstOrNull { it.name.startsWith("scannerScreen\$default") }
        assertNull("scannerScreen must not have default lambda parameter values", defaultMethod)
    }
}
