package com.example

import android.app.Application
import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.CutoutMaskShape
import com.example.data.model.FrameStyle
import com.example.data.model.ProfileType
import com.example.data.sample.SampleData
import com.example.engine.CutoutProcessor
import com.example.engine.PosterBitmapRenderer
import com.example.ui.components.PosterCard
import com.example.ui.components.ProfileBadgeView
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun `verify app name resource is Jaadu App`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Jaadu App", appName)
  }

  @Test
  fun `verify sample templates and profiles integrity`() {
    val templates = SampleData.defaultTemplates
    assertTrue("Should have default templates", templates.isNotEmpty())
    assertTrue("Should contain festival and daily templates", templates.size >= 10)

    val personalProfile = SampleData.defaultPersonalProfile
    assertEquals(ProfileType.PERSONAL, personalProfile.profileType)
    assertEquals("Rahul Sharma", personalProfile.name)

    val businessProfile = SampleData.defaultBusinessProfile
    assertEquals(ProfileType.BUSINESS, businessProfile.profileType)
    assertEquals("Apex Real Estate & Developers", businessProfile.businessName)
  }

  @Test
  fun `verify poster bitmap renderer generates valid bitmap`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val template = SampleData.defaultTemplates.first()
    val profile = SampleData.defaultPersonalProfile

    val bitmap = PosterBitmapRenderer.renderPoster(
      context = context,
      template = template,
      userProfile = profile
    )

    assertNotNull("Rendered bitmap must not be null", bitmap)
    assertEquals(1080, bitmap.width)
    assertEquals(1080, bitmap.height)
  }

  @Test
  fun `verify cutout processor generates masked cutout`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val cutoutPath = CutoutProcessor.processCutout(
      context = context,
      source = "sample_portrait",
      maskShape = CutoutMaskShape.GOLDEN_FRAME,
      applyBgRemoval = false
    )

    assertNotNull("Cutout path should be generated", cutoutPath)
    assertTrue("Cutout file should exist", java.io.File(cutoutPath!!).exists())
  }

  @Test
  fun `verify PosterCard renders correctly in Compose`() {
    val template = SampleData.defaultTemplates.first()
    val profile = SampleData.defaultPersonalProfile

    composeTestRule.setContent {
      MyApplicationTheme {
        PosterCard(
          template = template,
          userProfile = profile,
          onSelect = {},
          onFavoriteToggle = {},
          onQuickShare = {}
        )
      }
    }

    composeTestRule.onNodeWithTag("poster_card_${template.id}").assertIsDisplayed()
    composeTestRule.onNodeWithTag("customize_btn_${template.id}").assertIsDisplayed()
  }
}
