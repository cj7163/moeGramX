package moe.kirao.mgx.utils;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.StringRes;

import org.drinkless.tdlib.TdApi;
import org.thunderdog.challegram.BaseActivity;
import org.thunderdog.challegram.FileProvider;
import org.thunderdog.challegram.Log;
import org.thunderdog.challegram.R;
import org.thunderdog.challegram.U;
import org.thunderdog.challegram.config.Config;
import org.thunderdog.challegram.core.Background;
import org.thunderdog.challegram.core.Lang;
import org.thunderdog.challegram.data.TD;
import org.thunderdog.challegram.tool.UI;
import org.thunderdog.challegram.util.Permissions;

import java.io.File;
import java.util.List;

public class SystemUtils {
  @SuppressLint("PrivateApi")
  public static String getSystemProperty(String key) {
    try {
      Class<?> props = Class.forName("android.os.SystemProperties");
      return (String) props.getMethod("get", String.class).invoke(null, key);
    } catch (Exception ignore) {
      //
    }
    return null;
  }

  public static boolean shouldShowClipboardToast() {
    return ((Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) || OEMUtils.isMIUI()) && ((Build.VERSION.SDK_INT < Build.VERSION_CODES.S) || !OEMUtils.hasBuiltInClipboardToasts());
  }

  public static void copyFileToClipboard (TdApi.File file, @StringRes int toast) {
    try {
      ClipboardManager clipboard = (ClipboardManager) UI.getAppContext().getSystemService(Context.CLIPBOARD_SERVICE);
      if (clipboard != null) {
        Uri uri = FileProvider.getUriForFile(UI.getAppContext(), Config.FILE_PROVIDER_AUTHORITY, new File(file.local.path));
        ClipData clip = ClipData.newUri(UI.getAppContext().getContentResolver(), "image", uri);
        clipboard.setPrimaryClip(clip);
        if (shouldShowClipboardToast()) {
          UI.showToast(toast, Toast.LENGTH_SHORT);
        }
      }
    } catch (Exception e) {
      Log.e(e);
    }
  }

  public static void savePhotoToGallery (BaseActivity context, List<TD.DownloadedFile> files) {
    try {
      if (context.permissions().requestWriteExternalStorage(Permissions.WriteType.DOWNLOADS, granted -> {
        if (granted) {
          savePhotoToGallery(context, files);
        }
      })) {
        return;
      }
      Background.instance().post(() -> {
        int savedCount = 0;
        for (TD.DownloadedFile file : files) {
          if (U.copyToGalleryImpl(file.getPath(), U.TYPE_PHOTO, null)) {
            savedCount++;
          }
        }
        if (savedCount > 0) {
          if (savedCount == 1)
            UI.showToast(R.string.PhotoHasBeenSavedToGallery, Toast.LENGTH_SHORT);
          else
            UI.showToast(Lang.pluralBold(R.string.XPhotoSaved, savedCount), Toast.LENGTH_SHORT);
        }
      });
    } catch (Exception e) {
      Log.e(e);
    }
  }
}
