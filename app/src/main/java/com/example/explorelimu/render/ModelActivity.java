package com.example.explorelimu.render;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.ColorFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.render.AddModelActivity;
import com.example.render.R;
import com.example.render.ReviewActivity;
import com.example.render.demo.DemoLoaderTask;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.andresoviedo.android_3d_model_engine.camera.CameraController;
import org.andresoviedo.android_3d_model_engine.collision.CollisionController;
import org.andresoviedo.android_3d_model_engine.controller.TouchController;
import org.andresoviedo.android_3d_model_engine.model.Object3DData;
import org.andresoviedo.android_3d_model_engine.services.LoaderTask;
import org.andresoviedo.android_3d_model_engine.services.SceneLoader;
import org.andresoviedo.android_3d_model_engine.view.ModelRenderer;
import org.andresoviedo.android_3d_model_engine.view.ModelSurfaceView;
import org.andresoviedo.util.android.ContentUtils;
import org.andresoviedo.util.event.EventListener;

import java.io.IOException;
import java.net.URI;
import java.util.EventObject;
import java.util.List;
import java.util.Map;

/**
 * This activity represents the container for our 3D viewer.
 *
 * @author andresoviedo
 */
public class ModelActivity extends AppCompatActivity implements EventListener {

    private static final int REQUEST_CODE_LOAD_TEXTURE = 1000;
    private static final int FULLSCREEN_DELAY = 10000;

    /**
     * Type of model if file name has no extension (provided though content provider)
     */
    private int paramType;
    /**
     * The file to load. Passed as input parameter
     */
    private URI paramUri;
    /**
     * Enter into Android Immersive mode so the renderer is full screen or not
     */
    private boolean immersiveMode;
    /**
     * Background GL clear color. Default is light gray
     */
    private float[] backgroundColor = new float[]{0.0f, 0.0f, 0.0f, 1.0f};

    private ModelSurfaceView gLView;
    private TouchController touchController;
    private SceneLoader scene;
    private ModelViewerGUI gui;
    private CollisionController collisionController;


    private Handler handler;
    private CameraController cameraController;
    private String fileName;
    ColorFilter initial;

    private int vertexCount;
    private int normalsCount;
    private int modelId;

    private ServiceConnection serviceConnection;

    private BroadcastReceiver sessionBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("ModelActivity", "Loading activity...");
        super.onCreate(savedInstanceState);

        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.d(getLocalClassName(), "Service connected");
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };

        // Try to get input parameters
        Bundle b = getIntent().getExtras();
        if (b != null) {
            try {
                if (b.getString("uri") != null) {
                    this.paramUri = new URI(b.getString("uri"));
                    Log.i("ModelActivity", "Params: uri '" + paramUri + "'");
                }
                this.paramType = b.getString("type") != null ? Integer.parseInt(b.getString("type")) : -1;
                this.immersiveMode = "true".equalsIgnoreCase(b.getString("immersiveMode"));
                this.modelId = b.getInt("model_id");

                if (b.getString("backgroundColor") != null) {
                    String[] backgroundColors = b.getString("backgroundColor").split(" ");
                    backgroundColor[0] = Float.parseFloat(backgroundColors[0]);
                    backgroundColor[1] = Float.parseFloat(backgroundColors[1]);
                    backgroundColor[2] = Float.parseFloat(backgroundColors[2]);
                    backgroundColor[3] = Float.parseFloat(backgroundColors[3]);
                }
            } catch (Exception ex) {
                Log.e("ModelActivity", "Error parsing activity parameters: " + ex.getMessage(), ex);
            }

        }

        handler = new Handler(getMainLooper());

        // Create our 3D scenario
        Log.i("ModelActivity", "Loading Scene...");
        scene = new SceneLoader(this, paramUri, paramType, gLView);
        if (paramUri == null) {
            final LoaderTask task = new DemoLoaderTask(this, null, scene);
            task.execute();
        }

/*        Log.i("ModelActivity","Loading Scene...");
        if (paramUri == null) {
            scene = new ExampleSceneLoader(this);
        } else {
            scene = new SceneLoader(this, paramUri, paramType, gLView);
        }*/

        try {
            Log.i("ModelActivity", "Loading GLSurfaceView...");
            gLView = new ModelSurfaceView(this, backgroundColor, this.scene);
            gLView.addListener(this);
            setContentView(gLView);
            scene.setView(gLView);
        } catch (Exception e) {
            Log.e("ModelActivity", e.getMessage(), e);
            Toast.makeText(this, "Error loading OpenGL view:\n" + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        try {
            Log.i("ModelActivity", "Loading TouchController...");
            touchController = new TouchController(this);
            touchController.addListener(this);
        } catch (Exception e) {
            Log.e("ModelActivity", e.getMessage(), e);
            Toast.makeText(this, "Error loading TouchController:\n" + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        try {
            Log.i("ModelActivity", "Loading CollisionController...");
            collisionController = new CollisionController(gLView, scene);
            collisionController.addListener(scene);
            touchController.addListener(collisionController);
            touchController.addListener(scene);
        } catch (Exception e) {
            Log.e("ModelActivity", e.getMessage(), e);
            Toast.makeText(this, "Error loading CollisionController\n" + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        try {
            Log.i("ModelActivity", "Loading CameraController...");
            cameraController = new CameraController(scene.getCamera());
            gLView.getModelRenderer().addListener(cameraController);
            touchController.addListener(cameraController);
        } catch (Exception e) {
            Log.e("ModelActivity", e.getMessage(), e);
            Toast.makeText(this, "Error loading CameraController" + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        try {
            // TODO: finish UI implementation
            Log.i("ModelActivity", "Loading GUI...");
            gui = new ModelViewerGUI(gLView, scene);
            touchController.addListener(gui);
            gLView.addListener(gui);
            scene.addGUIObject(gui);
        } catch (Exception e) {
            Log.e("ModelActivity", e.getMessage(), e);
            Toast.makeText(this, "Error loading GUI" + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        // Show the Up button in the action bar.
        setupActionBar();

        setupOnSystemVisibilityChangeListener();

        // load model
        scene.init();

        Log.i("ModelActivity", "Finished loading");
//        setContentView(R.layout.activity_model);
//
//        parent = findViewById(R.id.parent_rl);
//        createRadioButton();
    }

    private void createRadioButton() {
        final RadioButton[] rb = new RadioButton[5];
        RadioGroup rg = new RadioGroup(this); //create the RadioGroup
        rg.setOrientation(RadioGroup.VERTICAL);//or RadioGroup.VERTICAL
        for(int i=0; i<5; i++){
            rb[i]  = new RadioButton(this);
            rb[i].setText("radio");
            rb[i].setId(i + 100);
            rg.addView(rb[i]);
        }
//        parent.addView(rg);//you add the whole RadioGroup to the layout

    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
        // getActionBar().setDisplayHomeAsUpEnabled(true);
        // }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.model, menu);
        initial = menu.getItem(1).getIcon().getColorFilter();

        return true;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setupOnSystemVisibilityChangeListener() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            return;
        }
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(visibility -> {
            // Note that system bars will only be "visible" if none of the
            // LOW_PROFILE, HIDE_NAVIGATION, or FULLSCREEN flags are set.
            if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                // The system bars are visible. Make any desired
                hideSystemUIDelayed();
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUIDelayed();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.model_explode) {
            scene.setExploded(true);
        } else if (itemId == R.id.model_upload) {
            uploadModel();
        } else if(itemId == R.id.model_review){
            openReviewActivity();
        } else if (itemId == R.id.wire_out_selection) {
            if (item.isChecked()) {
                item.setChecked(false);
//                item.getIcon().setColorFilter(initial);
            } else {
                item.setChecked(true);
                scene.setCurrentMode(SceneLoader.Mode.WIRE_OUT);
//                item.getIcon().setColorFilter(Color.CYAN, PorterDuff.Mode.MULTIPLY);
            }
            return true;
        }else if (itemId == R.id.isolate_selection) {
            if (item.isChecked()) {
                item.setChecked(false);
//                item.getIcon().setColorFilter(initial);
            } else {
                item.setChecked(true);
                scene.setCurrentMode(SceneLoader.Mode.ISOLATE);
//                item.getIcon().setColorFilter(Color.CYAN, PorterDuff.Mode.MULTIPLY);
            }
            return true;
        }else if (itemId == R.id.drop_selection) {
            if (item.isChecked()) {
                item.setChecked(false);
//                item.getIcon().setColorFilter(initial);
            } else {
                item.setChecked(true);
                scene.setCurrentMode(SceneLoader.Mode.DROP);
//                item.getIcon().setColorFilter(Color.CYAN, PorterDuff.Mode.MULTIPLY);
            }
            return true;
        }else if (itemId == R.id.model_toggle_wireframe) {
            scene.toggleWireframe();
        } else if (itemId == R.id.clear_selections) {
            scene.setClearSelectionsFlag(true);
        }
//        else if (itemId == R.id.model_toggle_boundingbox) {
//            scene.toggleBoundingBox();
//        } else if (itemId == R.id.model_toggle_skybox) {
//            gLView.toggleSkyBox();
//        } else if (itemId == R.id.model_toggle_textures) {
//            scene.toggleTextures();
//        } else if (itemId == R.id.model_toggle_animation) {
//            scene.toggleAnimation();
//        } else if (itemId == R.id.model_toggle_smooth) {
//            scene.toggleSmooth();
//        } else if (itemId == R.id.model_toggle_collision) {
//            scene.toggleCollision();
//        }
        else if (itemId == R.id.model_toggle_lights) {
            scene.toggleLighting();
        }
//        else if (itemId == R.id.model_toggle_stereoscopic) {
//            scene.toggleStereoscopic();
//        } else if (itemId == R.id.model_toggle_blending) {
//            scene.toggleBlending();
//        }
        else if (itemId == R.id.model_toggle_immersive) {
            toggleImmersive();
        }
//        else if (itemId == R.id.model_load_texture) {
//            Intent target = ContentUtils.createGetContentIntent("image/*");
//            Intent intent = Intent.createChooser(target, "Select a file");
//            try {
//                startActivityForResult(intent, REQUEST_CODE_LOAD_TEXTURE);
//            } catch (ActivityNotFoundException e) {
//                // The reason for the existence of aFileChooser
//            }
//        }

        hideSystemUIDelayed();
        return super.onOptionsItemSelected(item);
    }

    private void toggleImmersive() {
        this.immersiveMode = !this.immersiveMode;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            return;
        }
        if (this.immersiveMode) {
            hideSystemUI();
        } else {
            showSystemUI();
        }
        Toast.makeText(this, "Fullscreen " + this.immersiveMode, Toast.LENGTH_SHORT).show();
    }

    private void hideSystemUIDelayed() {
        if (!this.immersiveMode) {
            return;
        }
        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(this::hideSystemUI, FULLSCREEN_DELAY);

    }

    private void hideSystemUI() {
        if (!this.immersiveMode) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            hideSystemUIKitKat();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            hideSystemUIJellyBean();
        }
    }

    // This snippet hides the system bars.
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void hideSystemUIKitKat() {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        final View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void hideSystemUIJellyBean() {
        final View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LOW_PROFILE);
    }

    // This snippet shows the system bars. It does this by removing all the flags
    // except for the ones that make the content appear under the system bars.
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void showSystemUI() {
        handler.removeCallbacksAndMessages(null);
        final View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case REQUEST_CODE_LOAD_TEXTURE:
                // The URI of the selected file
                final Uri uri = data.getData();
                if (uri != null) {
                    Log.i("ModelActivity", "Loading texture '" + uri + "'");
                    try {
                        ContentUtils.setThreadActivity(this);
                        scene.loadTexture(null, uri);
                    } catch (IOException ex) {
                        Log.e("ModelActivity", "Error loading texture: " + ex.getMessage(), ex);
                        Toast.makeText(this, "Error loading texture '" + uri + "'. " + ex
                                .getMessage(), Toast.LENGTH_LONG).show();
                    } finally {
                        ContentUtils.setThreadActivity(null);
                    }
                }
        }
    }


    @Override
    public boolean onEvent(EventObject event) {
        if (event instanceof ModelRenderer.ViewEvent) {
            ModelRenderer.ViewEvent viewEvent = (ModelRenderer.ViewEvent) event;
            if (viewEvent.getCode() == ModelRenderer.ViewEvent.Code.SURFACE_CHANGED) {
                touchController.setSize(viewEvent.getWidth(), viewEvent.getHeight());
                gLView.setTouchController(touchController);

                // process event in GUI
                if (gui != null) {
                    gui.setSize(viewEvent.getWidth(), viewEvent.getHeight());
                    gui.setVisible(true);
                }
            }
        }
        return true;
    }

    public final void uploadModel(){
        FirebaseStorage storage = FirebaseStorage.getInstance();

        // Create a storage reference from our app
        StorageReference storageRef = storage.getReference();

        Uri androidUri = Uri.parse(paramUri.toString());

        fileName = androidUri.getLastPathSegment();

        StorageReference riversRef = storageRef.child("models/"+fileName);

        UploadTask uploadTask = riversRef.putFile(androidUri);
        ProgressDialog dialog;
        dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading. Please wait.");
        dialog.show();

        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                Log.e(getClass().getName(), exception.getMessage());
                Toast.makeText(ModelActivity.this, "Upload failed: "+exception.getMessage(), Toast.LENGTH_LONG).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ...
                Toast.makeText(ModelActivity.this, "Model successfully uploaded to cloud", Toast.LENGTH_SHORT).show();
                getModelDetails();
                openUploadActivity();
            }
        });
    }

    Map<String,Object> getModelDetails(){
        List<Object3DData> objects = scene.getObjects();
        boolean rigged = objects.size() > 1;

        vertexCount = 0;
        normalsCount = 0;
        for (Object3DData object : objects){
            normalsCount = (object.getNormalsBuffer() != null ? object.getNormalsBuffer().capacity() / 3 : 0);
            vertexCount = (object.getVertexBuffer() != null ? object.getVertexBuffer().capacity() / 3 : 0);
        }

        Log.v(getLocalClassName(), "rigged? " + rigged + ", polygon count: " + normalsCount + ", vertex count: " + vertexCount);

        return Map.ofEntries(
                Map.entry("rigged", rigged),
                Map.entry("polygonCount", normalsCount),
                Map.entry("vertexCount", vertexCount)
        );
    }

    public void openUploadActivity() {
        Intent myIntent = new Intent(this, AddModelActivity.class);
        myIntent.putExtra("fileName", fileName);
        myIntent.putExtra("vertexCount", vertexCount);
        myIntent.putExtra("normalsCount", normalsCount);
        startActivity(myIntent);
    }

    public void openReviewActivity() {
        Intent myIntent = new Intent(this, ReviewActivity.class);
        myIntent.putExtra("model_id", modelId);
        startActivity(myIntent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = null;
        try {
            intent = new Intent(this,
                    Class.forName("com.example.explorelimu.xmpp.RoosterConnectionService"));
            bindService(intent, serviceConnection, BIND_AUTO_CREATE);

            Intent broadcastIntent = new Intent("com.myapp.main.TEST_INTENT");
            broadcastIntent.putExtra("value", 0);
            broadcastIntent.setComponent(new ComponentName("com.example.explorelimu.xmpp", "com.example.explorelimu.xmpp.MessageReceiver"));

            sendBroadcast(broadcastIntent);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(sessionBroadcastReceiver, new IntentFilter());
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(serviceConnection);
    }
}
