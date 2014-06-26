package eu.livotov.labs.android.robotools.graphics;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import eu.livotov.labs.android.robotools.hardware.DeviceInfo;

import java.io.*;
import java.util.UUID;

public class PhotoPickup {

    private PhotoPickup() {
    }

    public static final int REQUEST_CODE_SELECT_IMAGE_GALLERY = 82;
    public static final int REQUEST_CODE_SELECT_IMAGE_CAMERA = 83;
    private static File photoFile = null;

    public static void pickPictureFromGallery(Activity ctx) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
        intent.setType("image/*");
        ctx.startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_CODE_SELECT_IMAGE_GALLERY);
    }

    public static void pickPictureFromGallery(Fragment ctx) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
        intent.setType("image/*");
        ctx.startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_CODE_SELECT_IMAGE_GALLERY);
    }

    public static void pickPictureFromCamera(Activity ctx, boolean frontalCamera) {
        File path = DeviceInfo.getExternalStorage("photos");

        if (DeviceInfo.isExternalStorageReady() && path != null && path.exists()) {
            photoFile = new File(path, UUID.randomUUID().toString().replaceAll("-", "") + ".jpg");

            try {
                photoFile.createNewFile();
                Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));

                if (frontalCamera) {
                    intent.putExtra("android.intent.extras.CAMERA_FACING", 1);
                }

                ctx.startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE_CAMERA);
            } catch (IOException e) {
                Log.e(PhotoPickup.class.getSimpleName(), e.getMessage(), e);
                throw new RuntimeException(e);
            }
        } else {
            throw new RuntimeException("Storage is not writable");
        }
    }

    public static void pickPictureFromCamera(Fragment ctx, boolean frontalCamera) {
        File path = DeviceInfo.getExternalStorage("photos");

        if (DeviceInfo.isExternalStorageReady() && path != null && path.exists()) {
            photoFile = new File(path, UUID.randomUUID().toString().replaceAll("-", "") + ".jpg");

            try {
                photoFile.createNewFile();
                Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));

                if (frontalCamera) {
                    intent.putExtra("android.intent.extras.CAMERA_FACING", 1);
                }

                ctx.startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE_CAMERA);
            } catch (IOException e) {
                Log.e(PhotoPickup.class.getSimpleName(), e.getMessage(), e);
                throw new RuntimeException(e);
            }
        } else {
            throw new RuntimeException("Storage is not writable");
        }
    }

    public static String doFinalzieImageSelectionFromCamera(Activity ctx, Intent data) {
        return getSelectedPhotoPath(ctx, data);
    }

    public static String doFinalzieImageSelectionFromGallery(Activity ctx, Intent data) {
        Uri selectedImage = data.getData();
        return getPath(ctx, selectedImage);
    }

    public static Bitmap generateThumbnailFromFile(String path, int w, int h) {
        try {
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(new File(path)), null, o);

            float widthTmp = (float) o.outWidth, heightTmp = (float) o.outHeight;
            int scale = 1;
            while (true) {
                if (widthTmp / 2.f <= w || heightTmp / 2.0f <= h) {
                    break;
                }
                widthTmp /= 2.f;
                heightTmp /= 2.f;
                scale *= 2;
            }
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(new FileInputStream(path), null, o2);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Bitmap generateThumbnailFromBlob(byte[] blob, int w, int h) {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(blob, 0, blob.length);

        int widthTmp = o.outWidth, heightTmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (widthTmp / 2 < w || heightTmp / 2 < h) {
                break;
            }
            widthTmp /= 2;
            heightTmp /= 2;
            scale *= 2;
        }
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeByteArray(blob, 0, blob.length, o2);
    }

    public static byte[] saveBitmapToBlob(Bitmap bm, int quality) {
        if (bm == null) {
            return null;
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, quality, bos);
        return bos.toByteArray();
    }

    public static void saveBitmapToFile(Bitmap bm, int quality, File file) throws IOException {
        if (bm != null) {
            FileOutputStream fos = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.JPEG, quality, fos);
            fos.flush();
            fos.close();
        } else {
            file.delete();
        }
    }

    private static String getSelectedPhotoPath(Activity ctx, Intent data) {
        if (photoFile != null && photoFile.exists() && photoFile.length() > 0) {
            return photoFile.getAbsolutePath();
        }

        if (data != null && data.getData() != null) {
            String galleryPath = data.getData().getPath();
            String albumPath = getPath(ctx, data.getData());
            if (albumPath != null) {
                return albumPath;
            } else if (galleryPath != null) {
                return galleryPath;
            }
        } else if (data != null && data.hasExtra("data") && data.getBooleanExtra("bitmap-data", false)) {
            try {
                Bitmap bm = (Bitmap) data.getExtras().get("data");
                File file = new File(ctx.getFilesDir(), UUID.randomUUID().toString().replaceAll("-", "") + ".jpg");
                FileOutputStream fos = new FileOutputStream(file);
                bm.compress(Bitmap.CompressFormat.JPEG, 70, fos);
                fos.close();
                return file.getAbsolutePath();
            } catch (Throwable err) {
                err.printStackTrace();
                return "";
            }
        }
        return "";
    }

    private static String getPath(Activity ctx, Uri uri) {
        if (uri != null && uri.getScheme().contains("file")) {
            return uri.getPath();
        }

        String selectedImagePath;
        //1:MEDIA GALLERY --- query from MediaStore.Images.Media.DATA
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = ctx.managedQuery(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            selectedImagePath = cursor.getString(column_index);
        } else {
            selectedImagePath = null;
        }
        return selectedImagePath;
    }

    public static void displayMultimedia(final Context ctx, final File file) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse("file://" + file.getAbsolutePath()), "image/*");
        ctx.startActivity(intent);
    }

    public static Bitmap loadBitmapWithScale(File file, int scaleFactor) {
        Bitmap b = null;
        try {
            //Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;

            FileInputStream fis = new FileInputStream(file);
            BitmapFactory.decodeStream(fis, null, o);
            fis.close();

            int sz = Math.max(o.outWidth, o.outHeight);
            int newMaxSize = (sz * scaleFactor) / 100;

            int scale = 1;
            if (o.outHeight > newMaxSize || o.outWidth > newMaxSize) {
                scale = (int) Math.pow(2, (int) Math.round(Math.log(newMaxSize / (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
            }

            //Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            fis = new FileInputStream(file);
            b = BitmapFactory.decodeStream(fis, null, o2);
            fis.close();
        } catch (IOException e) {
        }
        return b;
    }

    public static int getBitmapSizeInBytes(Bitmap bitmap) {
        if (Build.VERSION.SDK_INT > 11) {
            return bitmap.getByteCount();
        } else {
            return bitmap.getRowBytes() * bitmap.getHeight();
        }
    }
}