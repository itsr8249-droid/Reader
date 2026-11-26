package com.example

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.WorkspaceRepository
import com.example.ui.viewmodel.WorkspaceViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `app name matches Reader`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Reader", appName)
    }

    @Test
    fun `internet permission is not requested`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val pkgInfo = context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_PERMISSIONS
        )
        val permissions = pkgInfo.requestedPermissions?.toList().orEmpty()
        assertFalse(permissions.contains(Manifest.permission.INTERNET))
    }

    @Test
    fun `formatting helpers work accurately`() {
        assertEquals("1.4 MB", WorkspaceViewModel.formatFileSize(1_400_000L))
        assertEquals("420 KB", WorkspaceViewModel.formatFileSize(420_000L))
        assertEquals("01:18", WorkspaceViewModel.formatDuration(78))
        assertEquals(4, WorkspaceRepository.countWords("## Heading\n- **One** two three"))
    }
}
