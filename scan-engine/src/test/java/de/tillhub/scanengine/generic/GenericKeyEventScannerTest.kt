package de.tillhub.scanengine.generic

import android.app.Activity
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import de.tillhub.scanengine.data.ScannerEvent
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class GenericKeyEventScannerTest : FunSpec({

    lateinit var activity: Activity
    lateinit var mockEditText: EditText
    lateinit var mutableScannerEvents: MutableStateFlow<ScannerEvent>
    lateinit var scanner: GenericKeyEventScanner
    lateinit var testScope: TestScope

    beforeTest {
        activity = mockk(relaxed = true)
        mockEditText = mockk(relaxed = true)
        testScope = TestScope(UnconfinedTestDispatcher())
        mutableScannerEvents = spyk(MutableStateFlow(ScannerEvent.External.NotConnected))

        scanner = GenericKeyEventScanner(activity, mutableScannerEvents)
    }

    test("dispatchKeyEvent should append input and emit ScanResult when Enter is pressed") {
        val keyEventA = mockk<KeyEvent> {
            every { action } returns KeyEvent.ACTION_DOWN
            every { unicodeChar } returns 'A'.code
            every { eventTime } returns 100L
            every { keyCode } returns KeyEvent.KEYCODE_A
        }

        val keyEventEnter = mockk<KeyEvent> {
            every { action } returns KeyEvent.ACTION_DOWN
            every { unicodeChar } returns 1
            every { eventTime } returns 101L
            every { keyCode } returns KeyEvent.KEYCODE_ENTER
        }

        every { activity.findViewById<View>(android.R.id.content).findFocus() } returns mockEditText
        every { mockEditText.hasFocus() } returns false

        scanner.dispatchKeyEvent(keyEventA, "ScanKey")

        scanner.dispatchKeyEvent(keyEventEnter, "ScanKey")

        val testResults = mutableListOf<ScannerEvent>()
        testScope.launch {
            scanner.observeScannerResults().toList(testResults)
        }

        verify(exactly = 1) { mutableScannerEvents.tryEmit(any()) }

        val result = testResults.first() as ScannerEvent.ScanResult
        result.value shouldBe "A"
        result.scanKey shouldBe "ScanKey"
    }

    test("dispatchKeyEvent should not append non-printable keys and emit ScanResult when Enter is pressed") {
        val keyEventA = mockk<KeyEvent> {
            every { action } returns KeyEvent.ACTION_DOWN
            every { unicodeChar } returns 'A'.code
            every { eventTime } returns 100L
            every { keyCode } returns KeyEvent.KEYCODE_A
        }

        val keyEventShiftLef = mockk<KeyEvent> {
            every { action } returns KeyEvent.KEYCODE_SHIFT_LEFT
            every { unicodeChar } returns 0
            every { eventTime } returns 101L
            every { keyCode } returns KeyEvent.KEYCODE_SHIFT_LEFT
        }

        val keyEventEnter = mockk<KeyEvent> {
            every { action } returns KeyEvent.ACTION_DOWN
            every { unicodeChar } returns 1
            every { eventTime } returns 101L
            every { keyCode } returns KeyEvent.KEYCODE_ENTER
        }

        every { activity.findViewById<View>(android.R.id.content).findFocus() } returns mockEditText
        every { mockEditText.hasFocus() } returns false

        scanner.dispatchKeyEvent(keyEventA, "ScanKey")

        scanner.dispatchKeyEvent(keyEventShiftLef, "ScanKey")

        scanner.dispatchKeyEvent(keyEventEnter, "ScanKey")

        val testResults = mutableListOf<ScannerEvent>()
        testScope.launch {
            scanner.observeScannerResults().toList(testResults)
        }

        verify(exactly = 1) { mutableScannerEvents.tryEmit(any()) }

        val result = testResults.first() as ScannerEvent.ScanResult
        result.value shouldBe "A"
        result.scanKey shouldBe "ScanKey"
    }

    test("dispatchKeyEvent should not handle key events on invalid action") {
        val keyEventA = mockk<KeyEvent> {
            every { action } returns KeyEvent.ACTION_UP
        }
        scanner.dispatchKeyEvent(keyEventA, "ScanKey")

        val testResults = mutableListOf<ScannerEvent>()
        testScope.launch {
            scanner.observeScannerResults().toList(testResults)
        }
        verify(exactly = 0) { mutableScannerEvents.tryEmit(any()) }

        testResults.first().shouldBeInstanceOf<ScannerEvent.External.NotConnected>()
    }

    test("dispatchKeyEvent should not handle key events on invalid keycode") {
        val keyEventA = mockk<KeyEvent> {
            every { action } returns KeyEvent.ACTION_UP
            every { keyCode } returns KeyEvent.KEYCODE_SHIFT_LEFT
        }
        scanner.dispatchKeyEvent(keyEventA, "ScanKey")

        val testResults = mutableListOf<ScannerEvent>()
        testScope.launch {
            scanner.observeScannerResults().toList(testResults)
        }
        verify(exactly = 0) { mutableScannerEvents.tryEmit(any()) }

        testResults.first().shouldBeInstanceOf<ScannerEvent.External.NotConnected>()
    }

    test("dispatchKeyEvent should not handle key events on invalid unicodeChar") {
        val keyEventA = mockk<KeyEvent> {
            every { action } returns KeyEvent.ACTION_DOWN
            every { unicodeChar } returns 0
            every { eventTime } returns 100L
            every { keyCode } returns KeyEvent.KEYCODE_A
        }
        scanner.dispatchKeyEvent(keyEventA, "ScanKey")

        val testResults = mutableListOf<ScannerEvent>()
        testScope.launch {
            scanner.observeScannerResults().toList(testResults)
        }
        verify(exactly = 0) { mutableScannerEvents.tryEmit(any()) }

        testResults.first().shouldBeInstanceOf<ScannerEvent.External.NotConnected>()
    }

    test("dispatchKeyEvent should not handle key events when exceed scan threshold") {
        val testResults = mutableListOf<ScannerEvent>()
        val keyEventA = mockk<KeyEvent> {
            every { action } returns KeyEvent.ACTION_DOWN
            every { unicodeChar } returns 'A'.code
            every { eventTime } returns 100L
            every { keyCode } returns KeyEvent.KEYCODE_A
        }

        val keyEventEnter = mockk<KeyEvent> {
            every { action } returns KeyEvent.ACTION_DOWN
            every { unicodeChar } returns 0
            every { eventTime } returns 151L
            every { keyCode } returns KeyEvent.KEYCODE_ENTER
        }

        every { activity.findViewById<View>(android.R.id.content).findFocus() } returns mockEditText
        every { mockEditText.hasFocus() } returns false

        scanner.dispatchKeyEvent(keyEventA, "ScanKey")

        scanner.dispatchKeyEvent(keyEventEnter, "ScanKey")

        testScope.launch {
            scanner.observeScannerResults().toList(testResults)
        }

        verify(exactly = 0) { mutableScannerEvents.tryEmit(any()) }

        testResults.first().shouldBeInstanceOf<ScannerEvent.External.NotConnected>()
    }

    test("dispatchKeyEvent should not handle key events when EditText has focus") {
        runTest {
            val keyEventA = mockk<KeyEvent> {
                every { action } returns KeyEvent.ACTION_DOWN
                every { unicodeChar } returns 'A'.code
                every { eventTime } returns 100L
                every { keyCode } returns KeyEvent.KEYCODE_A
            }

            every { activity.findViewById<View>(android.R.id.content).findFocus() } returns mockEditText
            every { mockEditText.hasFocus() } returns true

            scanner.dispatchKeyEvent(keyEventA, "ScanKey")

            verify(exactly = 0) { mutableScannerEvents.tryEmit(any()) }
        }
    }
})
