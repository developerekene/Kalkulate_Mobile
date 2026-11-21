package com.devekene.kalkulate

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string and version from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Kalkulate", appName)
    assertEquals("com.devekene.kalkulate", BuildConfig.APPLICATION_ID)
    assertEquals("0.0.3", BuildConfig.VERSION_NAME)
    assertEquals(3, BuildConfig.VERSION_CODE)
  }
}
