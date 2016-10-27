package demo.com.facemanagement.addface;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.test.InstrumentationRegistry;
import android.test.suitebuilder.annotation.SmallTest;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

@SmallTest
public class SelectPhotoActivityTest {

    private SelectPhotoActivity selectPhotoActivity;

    @Before
    public void init() {
        selectPhotoActivity = new SelectPhotoActivity();
    }

    @Test
    public void detectFace_Test() throws IOException {
        Bitmap bitmap = getBitmap("satya.jpg");
        if (bitmap == null) {
            assertFalse(true);
        } else {

        }
    }

    private Bitmap getBitmap(String fileName) throws IOException {
        Context ctx = InstrumentationRegistry.getContext();
        InputStream is = ctx.getResources().getAssets().open(fileName);

        Bitmap bitmap = BitmapFactory.decodeStream(is);
        return bitmap;
    }
}
