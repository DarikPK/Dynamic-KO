package com.example.dynamiccollage

import android.content.Context
import android.net.Uri
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.dynamiccollage.ui.screens.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

@RunWith(AndroidJUnit4::class)
class PdfGenerationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testPdfGenerationAndPreview() {
        // 1. Navigate to CoverSetupScreen
        composeTestRule.onNodeWithText("Cover Setup").performClick()

        // 2. Select an image
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val imageFile = createImageFile(context)
        val imageUri = Uri.fromFile(imageFile)
        composeTestRule.activity.projectViewModel.updateCoverConfig(
            composeTestRule.activity.projectViewModel.currentCoverConfig.value.copy(
                mainImageUri = imageUri
            )
        )

        // 3. Save the cover configuration
        composeTestRule.onNodeWithText("Save").performClick()

        // 4. Navigate to InnerPagesScreen
        composeTestRule.onNodeWithText("Inner Pages").performClick()

        // 5. Create a new page group
        composeTestRule.onNodeWithText("Add").performClick()
        composeTestRule.onNodeWithText("Save").performClick()

        // 6. Add images to the page group
        composeTestRule.activity.projectViewModel.addPageGroupToProject(
            composeTestRule.activity.projectViewModel.currentPageGroups.value[0].copy(
                imageUris = listOf(imageUri.toString())
            )
        )

        // 7. Save the page groups
        composeTestRule.onNodeWithText("Save").performClick()

        // 8. Navigate back to the MainScreen
        composeTestRule.activity.onBackPressedDispatcher.onBackPressed()

        // 9. Click the "Generate PDF" button
        composeTestRule.onNodeWithText("Generate PDF").performClick()

        // 10. Verify that the PdfPreviewScreen is displayed
        composeTestRule.onNodeWithText("PDF Preview").assertExists()
    }

    private fun createImageFile(context: Context): File {
        val file = File(context.cacheDir, "test_image.png")
        file.createNewFile()

        val bitmap = android.graphics.Bitmap.createBitmap(100, 100, android.graphics.Bitmap.Config.ARGB_8888)
        val outputStream = FileOutputStream(file)
        bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, outputStream)
        outputStream.flush()
        outputStream.close()

        return file
    }
}
