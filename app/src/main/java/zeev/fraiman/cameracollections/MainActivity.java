package zeev.fraiman.cameracollections;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    Context context;
    Button bCamera, bFullPhoto, bVideo, bGallery;
    ImageView iv;
    ActivityResultLauncher<Intent> arlMakePhoto;
    ActivityResultLauncher<Uri>  arlFullPhoto;
    ActivityResultLauncher<Intent> arlVideo;
    ActivityResultLauncher<String> arlFromGallery;
    VideoView vivi;
    Uri imageUri, videoUri;
    private String mCurrentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initAll();

        ActivityCompat.requestPermissions((Activity) context,
                new String[]{Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                1);


        bCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentMakePhoto=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                arlMakePhoto.launch(intentMakePhoto);
            }
        });

        bFullPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    imageUri=createUri();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                arlFullPhoto.launch(imageUri);
            }
        });

        bVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iv.setVisibility(View.GONE);
                Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                arlVideo.launch(takeVideoIntent);
            }
        });

        bGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                arlFromGallery.launch("image/*");
            }
        });

        vivi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vivi.start();
            }
        });
    }

    private Uri createUri() throws IOException {
        int imageID=(int)(1000000*Math.random());
        String imageFileName = "ZeFra_pic_"+imageID;
        File storageDir = getExternalFilesDir("/ZeFra_pics/");
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        //mCurrentPhotoPath = image.getAbsolutePath();
        return FileProvider.getUriForFile(getApplicationContext(),
                "zeev.fraiman.cameracollections.fileProvider",
                image);
    }

    private void initAll() {
        context=this;
        bCamera= (Button) findViewById(R.id.bCamera);
        bFullPhoto= (Button) findViewById(R.id.bFullPhoto);
        bVideo= (Button) findViewById(R.id.bVideo);
        bGallery= (Button) findViewById(R.id.bGallery);
        iv= (ImageView) findViewById(R.id.iv);
        iv.setVisibility(View.GONE);
        vivi= (VideoView) findViewById(R.id.vivi);
        vivi.setVisibility(View.GONE);

        arlMakePhoto=registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode()==RESULT_OK && result.getData()!=null) {
                            Bundle bundle = result.getData().getExtras();
                            Bitmap bitmap = (Bitmap) bundle.get("data");
                            iv.setVisibility(View.VISIBLE);
                            iv.setImageBitmap(bitmap);
                        }
                    }
                }
        );
        arlFullPhoto=registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                new ActivityResultCallback<Boolean>() {
                    @Override
                    public void onActivityResult(Boolean result) {
                        try {
                            if (result) {
                                vivi.setVisibility(View.GONE);
                                iv.setVisibility(View.VISIBLE);
                                iv.setImageURI(imageUri);
                            }
                        } catch (Exception e) {
                            Toast.makeText(context, "Not founf photo",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        arlVideo=registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK && result.getData()!=null) {
                            Intent data = result.getData();
                            Uri videoUri = data.getData();

                            if (videoUri != null) {
                                iv.setVisibility(View.INVISIBLE);
                                vivi.setVisibility(View.VISIBLE);
                                vivi.setVideoURI(videoUri);
                                vivi.start();
                            }
                            else
                                Toast.makeText(context, "Not found video", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
        
        arlFromGallery=registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri result) {
                        if (result!=null)  {
                            Toast.makeText(context, "Gallery", Toast.LENGTH_SHORT).show();
                            vivi.setVisibility(View.GONE);
                            iv.setVisibility(View.VISIBLE);
                            iv.setImageURI(result);
                        }
                    }
                }
        );
    }

}