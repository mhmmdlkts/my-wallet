package com.mali.mywallet;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.mali.mywallet.ui.main.SectionsPagerAdapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.zip.Inflater;

public class MainActivity extends AppCompatActivity {


    private static final int IMG_FRONT_ID = 0;
    private static final int IMG_BACK_ID = 1;
    private static final String CARD = "card_";
    private static final String FRONT_CARD_PATH = CARD + "front_";
    private static final String BACK_CARD_PATH = CARD + "back_";
    private static final String CARD_NAME = CARD + "name_";
    private static final char LEFT_ARROW  = '<';
    private static final char RIGHT_ARROW = '>';
    private LinearLayout layoutContext;

    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FloatingActionButton fab = findViewById(R.id.fab);
        layoutContext = (LinearLayout) findViewById(R.id.context_cards);

                fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDialog();
            }
        });

        init_shared_pref();
        init_allKarts();
    }

    private String getRorL(boolean getL, String path) {
        int j = -1;
        for (int i = 0; i < path.length(); i++) {
            if((path.charAt(i) == LEFT_ARROW && getL) || (path.charAt(i) == RIGHT_ARROW && !getL))  {
                j = i;
                break;
            }
        }
        if(getL) {
            path = path.substring(0,j);
        } else {
            path = path.substring(j+1);
        }
        return path;
    }

    String frontPath = null, backPath = null, cardName = null;
    ImageButton iButtonFrontSide;
    ImageButton iButtonBackSide;
    private void openDialog() {
        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        dialog.setContentView(R.layout.new_card_dialog);

        iButtonFrontSide = dialog.findViewById(R.id.imageButton_frontSide);
        iButtonBackSide = dialog.findViewById(R.id.imageButton_back_Side);
        Button buttonYes = dialog.findViewById(R.id.button_dlg_add);
        Button buttonNo = dialog.findViewById(R.id.button_dlg_cancel);
        final EditText editTextCardName = dialog.findViewById(R.id.editText_card_name);
        dialog.show();

        iButtonFrontSide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                frontPath = openCamera(IMG_FRONT_ID);
            }
        });

        iButtonBackSide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backPath = openCamera(IMG_BACK_ID);
            }
        });

        buttonYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(addKart(editTextCardName.getText().toString()))
                    dialog.hide();
            }
        });

        buttonNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lastPathBack = null;
                lastPathFront = null;
                dialog.hide();
            }
        });

    }

    private void init_allKarts() {
        int i = 0;
        String frontPath, cardName;
        do {
            frontPath = sharedPref.getString(FRONT_CARD_PATH + i, null);
            cardName = sharedPref.getString(CARD_NAME + i, null);
            i++;
            System.out.println(i+ " "+frontPath);
            if(cardName == null)
                continue;

            System.out.println("TTT " + frontPath);
            createImagButton(frontPath);
        } while (frontPath != null);
    }

    private void createImagButton(String path) {
        ImageButton imageView = new ImageButton(MainActivity.this);
        imageView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 650 ));
        imageView.setAdjustViewBounds(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            imageView.setTransitionName(path);
        }
        Bitmap bitmap = BitmapFactory.decodeFile(flipCard(imageView));
        imageView.setImageBitmap(bitmap);
        imageView.setBackgroundColor(Color.TRANSPARENT);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        layoutContext.addView(imageView);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bitmap = BitmapFactory.decodeFile(flipCard(v));
                ((ImageButton) v).setImageBitmap(bitmap);
            }
        });
    }

    private String flipCard(View view) {
        String path = "";
        if (view == null)
            return path;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            path = view.getTransitionName();
            System.out.println("flip All path: "+path);
            String pathToSave = null;
            if (path.contains(String.valueOf(LEFT_ARROW))) {
                pathToSave = path.replace(LEFT_ARROW, RIGHT_ARROW);
                path = getRorL(true, path);
                System.out.println("fliped in left");
            } else if (path.contains(String.valueOf(RIGHT_ARROW))) {
                pathToSave = path.replace(RIGHT_ARROW, LEFT_ARROW);
                path = getRorL(false, path);
                System.out.println("fliped in right");
            }
            System.out.println("fliped All to save: "+pathToSave);
            view.setTransitionName(pathToSave);
        }
        System.out.println("fliped All new: "+path);
        return path;
    }

    private boolean addKart(String name) {
        if(lastPathFront == null | lastPathBack == null) {
            lastPathBack = null;
            lastPathFront = null;
            Toast.makeText(MainActivity.this,"Take 2 pictures",Toast.LENGTH_LONG).show();
            return false;
        }
        editor = sharedPref.edit();
        int i = 0;
        String img_path;
        do {
            img_path = sharedPref.getString(FRONT_CARD_PATH + i, null);
            i++;
        } while (img_path != null);
        i--;
        String path = lastPathFront + LEFT_ARROW + lastPathBack;
        editor.putString(FRONT_CARD_PATH + i, path);
        editor.putString(CARD_NAME + i, name);
        editor.commit();
        System.out.println(lastPathFront + "    " + lastPathBack);
        createImagButton(path);
        return true;
    }

    private String getLeft(String path) {
        if (path.contains(String.valueOf(LEFT_ARROW))) {
            path = path.split(String.valueOf(LEFT_ARROW))[0];
        } else if (path.contains(String.valueOf(RIGHT_ARROW))) {
            path = path.split(String.valueOf(RIGHT_ARROW))[0];
        }
        return path;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_remove_cards:
                sharedPref.edit().clear().commit();
                finish();
                startActivity(getIntent());
        }
        return true;
    }

    private void init_shared_pref() {
        sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        //sharedPref.edit().clear().commit();
    }

    String lastPathFront, lastPathBack;

    private String openCamera(int requestCode) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if(intent.resolveActivity(getPackageManager()) == null)
            return null;
        File imageFile = null;

        try {
            imageFile = getImageFile("nameTest",requestCode==0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if ( imageFile == null)
            return null;

        Uri imageUri = FileProvider.getUriForFile(this, "com.example.android.fileprovider", imageFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        if (requestCode==0)
            lastPathFront = imageFile.getAbsolutePath();
        else
            lastPathBack = imageFile.getAbsolutePath();
        startActivityForResult(intent,requestCode);

        return null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            System.out.println("aaa:" + lastPathFront + " " + lastPathBack);
            System.out.println("bbb:" + requestCode+"-"+resultCode);
            if (requestCode==0) {
                Bitmap bitmap = BitmapFactory.decodeFile(lastPathFront);
                iButtonFrontSide.setImageBitmap(bitmap);
            } else {
                Bitmap bitmap = BitmapFactory.decodeFile(lastPathBack);
                iButtonBackSide.setImageBitmap(bitmap);
            }
        } else if (resultCode == RESULT_CANCELED) {
            lastPathBack = null;
            lastPathFront = null;
        }

    }



    private File getImageFile(String name, boolean isFront) throws IOException {
        name = name.replaceAll(" ", "-");
        name = name.replaceAll("_", "-");
        String imageName = name+(isFront?"_front_":"_back_");
        //String imageName = start + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(imageName, ".jpg", storageDir);
        return imageFile;
    }
}