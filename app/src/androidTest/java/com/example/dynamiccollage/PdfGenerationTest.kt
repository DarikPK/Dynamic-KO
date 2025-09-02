package com.example.dynamiccollage

import android.content.Context
import android.net.Uri
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.dynamiccollage.data.model.CoverPageConfig
import com.example.dynamiccollage.data.model.PageGroup
import com.example.dynamiccollage.ui.screens.MainActivity
import com.example.dynamiccollage.utils.PdfContentManager
import com.example.dynamiccollage.utils.PdfGenerator
import com.tom_roush.pdfbox.pdmodel.PDDocument
import org.junit.Assert
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

    @Test
    fun testPageCropping_Enabled() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val imageFile = createImageFile(context) // 100x100 bitmap
        val imageUri = Uri.fromFile(imageFile).toString()

        // Use a blank cover config
        val coverConfig = CoverPageConfig()

        val pageGroup = PageGroup(
            groupName = "Crop Test Group",
            imageUris = listOf(imageUri),
            photosPerSheet = 1,
            smartLayoutEnabled = false,
            verticalAdjustment = true,
            horizontalAdjustment = true
        )

        val generatedPages = PdfContentManager.groupImagesForPdf(context, listOf(pageGroup))

        val pdfFile = PdfGenerator.generate(
            context,
            coverConfig,
            generatedPages,
            "cropping_test_enabled",
            emptyMap()
        )

        Assert.assertTrue(pdfFile != null && pdfFile.exists())

        val pdDocument = PDDocument.load(pdfFile)
        // Since cover is blank, we expect only 1 page
        Assert.assertEquals(1, pdDocument.numberOfPages)
        val page = pdDocument.getPage(0)

        // A4 is 595x842. The cropped page should be smaller.
        Assert.assertTrue("Page width should be less than A4", page.mediaBox.width < 595)
        Assert.assertTrue("Page height should be less than A4", page.mediaBox.height < 842)

        pdDocument.close()
    }

    @Test
    fun testPageCropping_Disabled() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val imageFile = createImageFile(context)
        val imageUri = Uri.fromFile(imageFile).toString()

        val coverConfig = CoverPageConfig()
        val pageGroup = PageGroup(
            groupName = "Crop Test Group Disabled",
            imageUris = listOf(imageUri),
            photosPerSheet = 1,
            smartLayoutEnabled = false,
            verticalAdjustment = false,
            horizontalAdjustment = false,
            orientation = com.example.dynamiccollage.data.model.PageOrientation.Vertical // Explicitly set for A4 check
        )

        val generatedPages = PdfContentManager.groupImagesForPdf(context, listOf(pageGroup))

        val pdfFile = PdfGenerator.generate(
            context,
            coverConfig,
            generatedPages,
            "cropping_test_disabled",
            emptyMap()
        )

        Assert.assertTrue(pdfFile != null && pdfFile.exists())

        val pdDocument = PDDocument.load(pdfFile)
        Assert.assertEquals(1, pdDocument.numberOfPages)
        val page = pdDocument.getPage(0)

        // A4 vertical is 595x842.
        Assert.assertEquals("Page width should be A4 width", 595f, page.mediaBox.width, 0.1f)
        Assert.assertEquals("Page height should be A4 height", 842f, page.mediaBox.height, 0.1f)

        pdDocument.close()
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
