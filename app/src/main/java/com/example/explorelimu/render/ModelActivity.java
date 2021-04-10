package com.example.explorelimu.render;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.ColorFilter;
import android.media.AudioManager;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.example.explorelimu.BuildConfig;
import com.example.explorelimu.data.session.Session;
import com.example.explorelimu.xmpp.RoosterConnection;
import com.example.explorelimu.xmpp.RoosterConnectionService;
import com.example.render.AddModelActivity;
import com.example.render.R;
import com.example.render.ReviewActivity;
import com.example.render.demo.DemoLoaderTask;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.koushikdutta.ion.Ion;
import com.twilio.audioswitch.AudioDevice;
import com.twilio.audioswitch.AudioSwitch;
import com.twilio.video.AudioCodec;
import com.twilio.video.ConnectOptions;
import com.twilio.video.EncodingParameters;
import com.twilio.video.G722Codec;
import com.twilio.video.IsacCodec;
import com.twilio.video.LocalAudioTrack;
import com.twilio.video.LocalParticipant;
import com.twilio.video.LocalVideoTrack;
import com.twilio.video.OpusCodec;
import com.twilio.video.PcmaCodec;
import com.twilio.video.PcmuCodec;
import com.twilio.video.RemoteAudioTrack;
import com.twilio.video.RemoteAudioTrackPublication;
import com.twilio.video.RemoteDataTrack;
import com.twilio.video.RemoteDataTrackPublication;
import com.twilio.video.RemoteParticipant;
import com.twilio.video.RemoteVideoTrack;
import com.twilio.video.RemoteVideoTrackPublication;
import com.twilio.video.Room;
import com.twilio.video.TwilioException;
import com.twilio.video.Video;
import com.twilio.video.VideoTrack;

import org.andresoviedo.android_3d_model_engine.camera.CameraController;
import org.andresoviedo.android_3d_model_engine.collision.CollisionController;
import org.andresoviedo.android_3d_model_engine.controller.TouchController;
import org.andresoviedo.android_3d_model_engine.inclass.data.SceneRepository;
import org.andresoviedo.android_3d_model_engine.inclass.ui.SceneViewModel;
import org.andresoviedo.android_3d_model_engine.model.Object3DData;
import org.andresoviedo.android_3d_model_engine.services.LoaderTask;
import org.andresoviedo.android_3d_model_engine.services.SceneLoader;
import org.andresoviedo.android_3d_model_engine.view.ModelRenderer;
import org.andresoviedo.android_3d_model_engine.view.ModelSurfaceView;
import org.andresoviedo.util.android.ContentUtils;
import org.andresoviedo.util.event.EventListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.EventObject;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import kotlin.Unit;

import static com.example.explorelimu.util.HelperKt.MOVE;
import static com.example.explorelimu.util.HelperKt.MOVE_INTENT;
import static com.example.explorelimu.util.HelperKt.PINCH;
import static com.example.explorelimu.util.HelperKt.PINCH_INTENT;
import static com.example.explorelimu.util.HelperKt.PREF_AUDIO_CODEC;
import static com.example.explorelimu.util.HelperKt.PREF_AUDIO_CODEC_DEFAULT;
import static com.example.explorelimu.util.HelperKt.PREF_ENABLE_AUTOMATIC_SUBSCRIPTION;
import static com.example.explorelimu.util.HelperKt.PREF_ENABLE_AUTOMATIC_SUBSCRIPTION_DEFAULT;
import static com.example.explorelimu.util.HelperKt.PREF_SENDER_MAX_AUDIO_BITRATE;
import static com.example.explorelimu.util.HelperKt.PREF_SENDER_MAX_AUDIO_BITRATE_DEFAULT;
import static com.example.explorelimu.util.HelperKt.PREF_SENDER_MAX_VIDEO_BITRATE;
import static com.example.explorelimu.util.HelperKt.PREF_SENDER_MAX_VIDEO_BITRATE_DEFAULT;
import static com.example.explorelimu.util.HelperKt.SELECTION;
import static com.example.explorelimu.util.HelperKt.SELECTION_MODE;
import static com.example.explorelimu.util.HelperKt.SELECTION_MODE_INTENT;
import static com.example.explorelimu.util.HelperKt.SESSION;
import static com.example.explorelimu.util.HelperKt.STUDENT;
import static com.example.explorelimu.util.HelperKt.TEACHER;
import static com.example.explorelimu.util.HelperKt.UPDATE_SELECTION_INTENT;
import static com.example.explorelimu.util.HelperKt.USER_TYPE;
import static com.example.explorelimu.util.HelperKt.X_DIFF;
import static com.example.explorelimu.util.HelperKt.Y_DIFF;
import static com.example.explorelimu.util.HelperKt.Z_DIFF;
import static com.example.explorelimu.util.HelperKt.getScreenHeight;
import static com.example.explorelimu.util.HelperKt.getScreenWidth;

/**
 * This activity represents the container for our 3D viewer.
 *
 * @author andresoviedo
 */
public class ModelActivity extends AppCompatActivity implements EventListener {
    private static final int MIC_PERMISSION_REQUEST_CODE = 1;

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
    private RoosterConnectionService boundService;
    private boolean isBound = false;
    private XMPPTCPConnection xmpptcpConnection;
    private RoosterConnection roosterConnection;

//    private BroadcastReceiver sessionBroadcastReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            sceneViewModel.updateCameraPos((float) (intent.getDoubleExtra(RCVD_X, 0.0f)*screenWidth), (float) (intent.getDoubleExtra(RCVD_Y, 0.0f)*screenHeight));
//            Log.d(getClass().getName() + " received broadcast", RCVD_X + "," + RCVD_Y);
//        }
//    };

    private SceneRepository sceneRepository;
    private SceneViewModel sceneViewModel;

    private String userType;
    private Session session;

    private int screenWidth, screenHeight;

    private float xFactor = 100000f, yFactor = 100f;

    private static final String LOCAL_AUDIO_TRACK_NAME = "mic";

    /*
     * You must provide a Twilio Access Token to connect to the Video service
     */
    private static final String TWILIO_ACCESS_TOKEN = BuildConfig.TWILIO_ACCESS_TOKEN;
    private static final String ACCESS_TOKEN_SERVER = BuildConfig.TWILIO_ACCESS_TOKEN_SERVER;

    /*
     * Access token used to connect. This field will be set either from the console generated token
     * or the request to the token server.
     */
    private String accessToken;

    /*
     * A Room represents communication between a local participant and one or more participants.
     */
    private Room room;
    private LocalParticipant localParticipant;

    /*
     * AudioCodec represents the preferred codec for encoding and decoding audio.
     */
    private AudioCodec audioCodec;

    /*
     * Encoding parameters represent the sender side bandwidth constraints.
     */
    private EncodingParameters encodingParameters;

    /*
     * Audio management
     */
    private AudioSwitch audioSwitch;
    private int savedVolumeControlStream;

    /*
     * Android shared preferences used for settings
     */
    private SharedPreferences preferences;

    private LocalAudioTrack localAudioTrack;

    private boolean disconnectedFromOnDestroy;
    private boolean enableAutomaticSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("ModelActivity", "Loading activity...");
        super.onCreate(savedInstanceState);

        /*
         * Get shared preferences to read settings
         */
        preferences = android.preference.PreferenceManager.getDefaultSharedPreferences(this);

        /*
         * Setup audio management and set the volume control stream
         */
        audioSwitch = new AudioSwitch(getApplicationContext());
        savedVolumeControlStream = getVolumeControlStream();
        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);

        audioSwitch.start((audioDevices, audioDevice) -> {
//            updateAudioDeviceIcon(audioDevice);

            return Unit.INSTANCE;
        });

        /*
         * Check camera and microphone permissions. Needed in Android M.
         */
        if (!checkPermissionForCameraAndMicrophone()) {
            requestPermissionForCameraAndMicrophone();
        } else {
            createAudioAndVideoTracks();
            setAccessToken();
        }

        screenWidth = getScreenWidth(this);
        screenHeight = getScreenHeight(this);
        Log.d(getLocalClassName(),"width:"+screenWidth+", height:"+screenHeight);

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
                if (b.getParcelable(SESSION) != null) {
                    this.session = b.getParcelable(SESSION);
                }

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

        sceneRepository = new SceneRepository(this).getInstance();
        sceneViewModel = new ViewModelProvider(this, SceneViewModel.Companion.getFACTORY().invoke(sceneRepository)).get(SceneViewModel.class);

        userType = PreferenceManager.getDefaultSharedPreferences(this).getString(USER_TYPE, STUDENT);
        if (session != null) {
            listenForChanges();
        }
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.d(getLocalClassName(), "Service connected");
                RoosterConnectionService.MyBinder myBinder = (RoosterConnectionService.MyBinder) service;
                boundService = myBinder.getService();
                isBound = true;

                xmpptcpConnection = boundService.getmConnection();
                roosterConnection = boundService.getmRoosterConnection();

                if (session != null) {
                    try {
                        MultiUserChat multiUserChat = MultiUserChatManager
                                .getInstanceFor(xmpptcpConnection)
                                .getMultiUserChat(JidCreate
                                        .entityBareFrom(session.component1() + "@"
                                                + getResources().getString(R.string.muc_service)));

                        multiUserChat.addMessageListener(new MessageListener() {
                            @Override
                            public void processMessage(Message message) {
                                String msg = message.getBody();
                                Log.d(getClass().getName() + " msg received", msg);

                                if (msg.startsWith(MOVE)){
                                    double x = Float.parseFloat(msg.substring(msg.indexOf("x") + 1, msg.indexOf(",")));
                                    double y = Float.parseFloat(msg.substring(msg.indexOf("y") + 1));

                                    if (userType.equals(STUDENT)){
                                        x = (x/xFactor)*screenWidth;
                                        y = (y/yFactor)*screenHeight;
                                        sceneViewModel.updateCameraPos((float) x, (float) y);
                                    }
                                } else if (msg.startsWith(PINCH)){
                                    float z = Float.parseFloat(msg.substring(msg.indexOf("z") + 1));
                                    Log.d(getLocalClassName() + "received z", String.valueOf(z));
                                    sceneViewModel.updateCameraZoom(z);
                                } else if (msg.startsWith(SELECTION_MODE)){

                                } else if (msg.startsWith(SELECTION)){
                                    int selection = Integer.parseInt(msg.substring(msg.indexOf(":")));
                                    sceneViewModel.updateObjId(selection);
                                }
                            }
                        });

                    } catch (XmppStringprepException e) {
                        Log.e(getClass().getName() + "muc", e.getMessage());
                    }
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };

        handler = new Handler(getMainLooper());

        // Create our 3D scenario
        Log.i("ModelActivity", "Loading Scene...");
        scene = new SceneLoader(this, paramUri, paramType, gLView, sceneViewModel);
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
            cameraController = new CameraController(scene.getCamera(), sceneViewModel);
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

        audioSwitch.selectDevice(audioSwitch.getAvailableAudioDevices().get(0));

//        if (selectedDevice == null) {
//            Log.e(getLocalClassName(), "No selected audio device");
//        }



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

        /*
         * Start the audio device selector after the menu is created and update the icon when the
         * selected audio device changes.
         */

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
                sceneViewModel.updateSelectionMode(SceneLoader.Mode.WIRE_OUT);
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
                sceneViewModel.updateSelectionMode(SceneLoader.Mode.ISOLATE);
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
                sceneViewModel.updateSelectionMode(SceneLoader.Mode.DROP);
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
        Intent intent = new Intent(getApplicationContext(), RoosterConnectionService.class);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);

        Intent broadcastIntent = new Intent("com.myapp.main.TEST_INTENT");
        broadcastIntent.putExtra("value", 0);

        sendBroadcast(broadcastIntent);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onResume() {
        super.onResume();
//        registerReceiver(sessionBroadcastReceiver, new IntentFilter(RCVD_COORD));

        /*
         * Update preferred audio and video codec in case changed in settings
         */
        audioCodec = getAudioCodecPreference(PREF_AUDIO_CODEC,
                PREF_AUDIO_CODEC_DEFAULT);
        enableAutomaticSubscription = getAutomaticSubscriptionPreference(PREF_ENABLE_AUTOMATIC_SUBSCRIPTION,
                PREF_ENABLE_AUTOMATIC_SUBSCRIPTION_DEFAULT);
        /*
         * Get latest encoding parameters
         */
        final EncodingParameters newEncodingParameters = getEncodingParameters();

        /*
         * Update encoding parameters
         */
        encodingParameters = newEncodingParameters;

        /*
         * Update reconnecting UI
         */
//        if (room != null) {
//            reconnectingProgressBar.setVisibility((room.getState() != Room.State.RECONNECTING) ?
//                    View.GONE :
//                    View.VISIBLE);
//        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(serviceConnection);
    }

    @Override
    protected void onDestroy() {
        /*
         * Tear down audio management and restore previous volume stream
         */
        audioSwitch.stop();
        setVolumeControlStream(savedVolumeControlStream);

        /*
         * Always disconnect from the room before leaving the Activity to
         * ensure any memory allocated to the Room resource is freed.
         */
        if (room != null && room.getState() != Room.State.DISCONNECTED) {
            room.disconnect();
            disconnectedFromOnDestroy = true;
        }

        /*
         * Release the local audio and video tracks ensuring any memory allocated to audio
         * or video is freed.
         */
        if (localAudioTrack != null) {
            localAudioTrack.release();
            localAudioTrack = null;
        }

        super.onDestroy();
    }

    private void listenForChanges(){

        sceneViewModel.get_cameraPos().observe(this, new Observer<Float[]>() {
            @Override
            public void onChanged(Float[] floats) {
                if (userType!= null && userType.equals(TEACHER)){
                    Intent intent = new Intent(MOVE_INTENT);
                    intent.putExtra(SESSION, session.component1());

                    float xOrdinate = xFactor*(floats[0]/screenWidth);
                    intent.putExtra(X_DIFF, xOrdinate);

                    float yOrdinate = yFactor*(floats[1]/screenHeight);
                    intent.putExtra(Y_DIFF, yOrdinate);

                    sendBroadcast(intent);
                } else {
                    cameraController.translateCamera(floats[0], floats[1]);
                }
            }
        });

        sceneViewModel.get_cameraZoom().observe(this, new Observer<Float>() {
            @Override
            public void onChanged(Float aFloat) {
                if (userType != null && userType.equals(TEACHER)){
                    Intent intent = new Intent(PINCH_INTENT);
                    intent.putExtra(SESSION, session.component1());

                    float zoom = aFloat;
                    intent.putExtra(Z_DIFF, aFloat);

                    sendBroadcast(intent);
                } else
                    cameraController.cameraZoom(aFloat);
            }
        });

        sceneViewModel.get_objId().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                if (userType.equals(TEACHER)){
                    Intent intent = new Intent(UPDATE_SELECTION_INTENT);
                    intent.putExtra(SELECTION, integer);
                    sendBroadcast(intent);
                } else
                    scene.setSelectedObjectIndex(integer);
            }
        });

        sceneViewModel.get_selectionMode().observe(this, new Observer<SceneLoader.Mode>() {
            @Override
            public void onChanged(SceneLoader.Mode mode) {
                if (userType.equals(TEACHER)){
                    Intent intent = new Intent(SELECTION_MODE_INTENT);
                    intent.putExtra(SELECTION_MODE, mode.name());
                    sendBroadcast(intent);
                } else {
                    scene.setCurrentMode(mode);
                    Toast.makeText(getApplicationContext(), "Selection mode updated: " + mode.name(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /*
     * Get the preferred audio codec from shared preferences
     */
    private AudioCodec getAudioCodecPreference(String key, String defaultValue) {
        final String audioCodecName = preferences.getString(key, defaultValue);

        switch (audioCodecName) {
            case IsacCodec.NAME:
                return new IsacCodec();
            case OpusCodec.NAME:
                return new OpusCodec();
            case PcmaCodec.NAME:
                return new PcmaCodec();
            case PcmuCodec.NAME:
                return new PcmuCodec();
            case G722Codec.NAME:
                return new G722Codec();
            default:
                return new OpusCodec();
        }
    }

    private boolean getAutomaticSubscriptionPreference(String key, boolean defaultValue) {
        return preferences.getBoolean(key, defaultValue);
    }

    private EncodingParameters getEncodingParameters() {
        final int maxAudioBitrate = Integer.parseInt(
                preferences.getString(PREF_SENDER_MAX_AUDIO_BITRATE,
                        PREF_SENDER_MAX_AUDIO_BITRATE_DEFAULT));
        final int maxVideoBitrate = Integer.parseInt(
                preferences.getString(PREF_SENDER_MAX_VIDEO_BITRATE,
                        PREF_SENDER_MAX_VIDEO_BITRATE_DEFAULT));

        return new EncodingParameters(maxAudioBitrate, maxVideoBitrate);
    }

    /*
     * The actions performed during disconnect.
     */
//    private void setDisconnectAction() {
//        connectActionFab.setImageDrawable(ContextCompat.getDrawable(this,
//                R.drawable.ic_call_end_white_24px));
//        connectActionFab.show();
//        connectActionFab.setOnClickListener(disconnectClickListener());
//    }

    private void createAudioAndVideoTracks() {
        // Share your microphone
        localAudioTrack = LocalAudioTrack.create(this, true, LOCAL_AUDIO_TRACK_NAME);
    }

    private void setAccessToken() {
        if (!BuildConfig.USE_TOKEN_SERVER) {
            /*
             * OPTION 1 - Generate an access token from the getting started portal
             * https://www.twilio.com/console/video/dev-tools/testing-tools and add
             * the variable TWILIO_ACCESS_TOKEN setting it equal to the access token
             * string in your local.properties file.
             */
            this.accessToken = TWILIO_ACCESS_TOKEN;
        } else {
            /*
             * OPTION 2 - Retrieve an access token from your own web app.
             * Add the variable ACCESS_TOKEN_SERVER assigning it to the url of your
             * token server and the variable USE_TOKEN_SERVER=true to your
             * local.properties file.
             */
            retrieveAccessTokenfromServer();
        }

//        if (session != null) {
//            connectToRoom("sessioncomponent1");
//            Log.d(getLocalClassName(), "Connect to room called");
//        }
    }

    private void connectToRoom() {
        String roomName = null;
        if (this.getIntent().hasExtra(SESSION)){
            roomName = ((Session) getIntent().getParcelableExtra(SESSION)).component1();
        }
        audioSwitch.activate();
        ConnectOptions.Builder connectOptionsBuilder = new ConnectOptions.Builder(accessToken)
                .roomName(roomName);

        /*
         * Add local audio track to connect options to share with participants.
         */
        if (localAudioTrack != null) {
            connectOptionsBuilder
                    .audioTracks(Collections.singletonList(localAudioTrack));
        }

        /*
         * Set the preferred audio and video codec for media.
         */
        connectOptionsBuilder.preferAudioCodecs(Collections.singletonList(audioCodec));

        /*
         * Set the sender side encoding parameters.
         */
        connectOptionsBuilder.encodingParameters(encodingParameters);

        /*
         * Toggles automatic track subscription. If set to false, the LocalParticipant will receive
         * notifications of track publish events, but will not automatically subscribe to them. If
         * set to true, the LocalParticipant will automatically subscribe to tracks as they are
         * published. If unset, the default is true. Note: This feature is only available for Group
         * Rooms. Toggling the flag in a P2P room does not modify subscription behavior.
         */
        connectOptionsBuilder.enableAutomaticSubscription(enableAutomaticSubscription);

        room = Video.connect(this, connectOptionsBuilder.build(), roomListener());
        Log.d(getLocalClassName(), "Done connecting to room");
//        setDisconnectAction();
    }

    private boolean checkPermissionForCameraAndMicrophone() {
        int resultMic = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        return resultMic == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissionForCameraAndMicrophone() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.RECORD_AUDIO)) {
            Toast.makeText(this,
                    R.string.permissions_needed,
                    Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    MIC_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MIC_PERMISSION_REQUEST_CODE) {
            boolean cameraAndMicPermissionGranted = true;

            for (int grantResult : grantResults) {
                cameraAndMicPermissionGranted &= grantResult == PackageManager.PERMISSION_GRANTED;
            }

            if (cameraAndMicPermissionGranted) {
                createAudioAndVideoTracks();
                setAccessToken();
            } else {
                Toast.makeText(this,
                        R.string.permissions_needed,
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void retrieveAccessTokenfromServer() {
        Ion.with(this)
                .load(String.format("%s?identity=%s", ACCESS_TOKEN_SERVER,
                        UUID.randomUUID().toString()))
                .asString()
                .setCallback((e, token) -> {
                    if (e == null) {
                        accessToken = token;
                        connectToRoom();
                        Log.d(getLocalClassName(), "Connected to room after token obtained");
                    } else {
                        Toast.makeText(this,
                                R.string.error_retrieving_access_token, Toast.LENGTH_LONG)
                                .show();
                        Log.e(getLocalClassName() + " retrieving token", e.getMessage());
                    }
                });
    }

    /*
     * Room events listener
     */
    @SuppressLint("SetTextI18n")
    private Room.Listener roomListener() {
        return new Room.Listener() {
            @Override
            public void onConnected(Room room) {
                localParticipant = room.getLocalParticipant();
                setTitle(room.getName());

                for (RemoteParticipant remoteParticipant : room.getRemoteParticipants()) {
                    addRemoteParticipant(remoteParticipant);
                    break;
                }
            }

            @Override
            public void onReconnecting(@NonNull Room room, @NonNull TwilioException twilioException) {
//                reconnectingProgressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onReconnected(@NonNull Room room) {
//                reconnectingProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onConnectFailure(Room room, TwilioException e) {
                audioSwitch.deactivate();
//                intializeUI();
            }

            @Override
            public void onDisconnected(Room room, TwilioException e) {
                localParticipant = null;
//                reconnectingProgressBar.setVisibility(View.GONE);
                ModelActivity.this.room = null;
                // Only reinitialize the UI if disconnect was not called from onDestroy()
                if (!disconnectedFromOnDestroy) {
                    audioSwitch.deactivate();
//                    intializeUI();
//                    moveLocalVideoToPrimaryView();
                }
            }

            @Override
            public void onParticipantConnected(Room room, RemoteParticipant remoteParticipant) {
                addRemoteParticipant(remoteParticipant);
            }

            @Override
            public void onParticipantDisconnected(Room room, RemoteParticipant remoteParticipant) {
//                removeRemoteParticipant(remoteParticipant);
            }

            @Override
            public void onRecordingStarted(Room room) {
                /*
                 * Indicates when media shared to a Room is being recorded. Note that
                 * recording is only available in our Group Rooms developer preview.
                 */
                Log.d(getLocalClassName(), "onRecordingStarted");
            }

            @Override
            public void onRecordingStopped(Room room) {
                /*
                 * Indicates when media shared to a Room is no longer being recorded. Note that
                 * recording is only available in our Group Rooms developer preview.
                 */
                Log.d(getLocalClassName(), "onRecordingStopped");
            }
        };
    }

    @SuppressLint("SetTextI18n")
    private RemoteParticipant.Listener remoteParticipantListener() {
        return new RemoteParticipant.Listener() {
            @Override
            public void onAudioTrackPublished(RemoteParticipant remoteParticipant,
                                              RemoteAudioTrackPublication remoteAudioTrackPublication) {
                Log.i(getLocalClassName(), String.format("onAudioTrackPublished: " +
                                "[RemoteParticipant: identity=%s], " +
                                "[RemoteAudioTrackPublication: sid=%s, enabled=%b, " +
                                "subscribed=%b, name=%s]",
                        remoteParticipant.getIdentity(),
                        remoteAudioTrackPublication.getTrackSid(),
                        remoteAudioTrackPublication.isTrackEnabled(),
                        remoteAudioTrackPublication.isTrackSubscribed(),
                        remoteAudioTrackPublication.getTrackName()));
            }

            @Override
            public void onAudioTrackUnpublished(RemoteParticipant remoteParticipant,
                                                RemoteAudioTrackPublication remoteAudioTrackPublication) {
                Log.i(getLocalClassName(), String.format("onAudioTrackUnpublished: " +
                                "[RemoteParticipant: identity=%s], " +
                                "[RemoteAudioTrackPublication: sid=%s, enabled=%b, " +
                                "subscribed=%b, name=%s]",
                        remoteParticipant.getIdentity(),
                        remoteAudioTrackPublication.getTrackSid(),
                        remoteAudioTrackPublication.isTrackEnabled(),
                        remoteAudioTrackPublication.isTrackSubscribed(),
                        remoteAudioTrackPublication.getTrackName()));
            }

            @Override
            public void onDataTrackPublished(RemoteParticipant remoteParticipant,
                                             RemoteDataTrackPublication remoteDataTrackPublication) {
                Log.i(getLocalClassName(), String.format("onDataTrackPublished: " +
                                "[RemoteParticipant: identity=%s], " +
                                "[RemoteDataTrackPublication: sid=%s, enabled=%b, " +
                                "subscribed=%b, name=%s]",
                        remoteParticipant.getIdentity(),
                        remoteDataTrackPublication.getTrackSid(),
                        remoteDataTrackPublication.isTrackEnabled(),
                        remoteDataTrackPublication.isTrackSubscribed(),
                        remoteDataTrackPublication.getTrackName()));
            }

            @Override
            public void onDataTrackUnpublished(RemoteParticipant remoteParticipant,
                                               RemoteDataTrackPublication remoteDataTrackPublication) {
                Log.i(getLocalClassName(), String.format("onDataTrackUnpublished: " +
                                "[RemoteParticipant: identity=%s], " +
                                "[RemoteDataTrackPublication: sid=%s, enabled=%b, " +
                                "subscribed=%b, name=%s]",
                        remoteParticipant.getIdentity(),
                        remoteDataTrackPublication.getTrackSid(),
                        remoteDataTrackPublication.isTrackEnabled(),
                        remoteDataTrackPublication.isTrackSubscribed(),
                        remoteDataTrackPublication.getTrackName()));
            }

            @Override
            public void onVideoTrackPublished(RemoteParticipant remoteParticipant,
                                              RemoteVideoTrackPublication remoteVideoTrackPublication) {
                Log.i(getLocalClassName(), String.format("onVideoTrackPublished: " +
                                "[RemoteParticipant: identity=%s], " +
                                "[RemoteVideoTrackPublication: sid=%s, enabled=%b, " +
                                "subscribed=%b, name=%s]",
                        remoteParticipant.getIdentity(),
                        remoteVideoTrackPublication.getTrackSid(),
                        remoteVideoTrackPublication.isTrackEnabled(),
                        remoteVideoTrackPublication.isTrackSubscribed(),
                        remoteVideoTrackPublication.getTrackName()));
            }

            @Override
            public void onVideoTrackUnpublished(RemoteParticipant remoteParticipant,
                                                RemoteVideoTrackPublication remoteVideoTrackPublication) {
                Log.i(getLocalClassName(), String.format("onVideoTrackUnpublished: " +
                                "[RemoteParticipant: identity=%s], " +
                                "[RemoteVideoTrackPublication: sid=%s, enabled=%b, " +
                                "subscribed=%b, name=%s]",
                        remoteParticipant.getIdentity(),
                        remoteVideoTrackPublication.getTrackSid(),
                        remoteVideoTrackPublication.isTrackEnabled(),
                        remoteVideoTrackPublication.isTrackSubscribed(),
                        remoteVideoTrackPublication.getTrackName()));
            }

            @Override
            public void onAudioTrackSubscribed(RemoteParticipant remoteParticipant,
                                               RemoteAudioTrackPublication remoteAudioTrackPublication,
                                               RemoteAudioTrack remoteAudioTrack) {
                Log.i(getLocalClassName(), String.format("onAudioTrackSubscribed: " +
                                "[RemoteParticipant: identity=%s], " +
                                "[RemoteAudioTrack: enabled=%b, playbackEnabled=%b, name=%s]",
                        remoteParticipant.getIdentity(),
                        remoteAudioTrack.isEnabled(),
                        remoteAudioTrack.isPlaybackEnabled(),
                        remoteAudioTrack.getName()));
            }

            @Override
            public void onAudioTrackUnsubscribed(RemoteParticipant remoteParticipant,
                                                 RemoteAudioTrackPublication remoteAudioTrackPublication,
                                                 RemoteAudioTrack remoteAudioTrack) {
                Log.i(getLocalClassName(), String.format("onAudioTrackUnsubscribed: " +
                                "[RemoteParticipant: identity=%s], " +
                                "[RemoteAudioTrack: enabled=%b, playbackEnabled=%b, name=%s]",
                        remoteParticipant.getIdentity(),
                        remoteAudioTrack.isEnabled(),
                        remoteAudioTrack.isPlaybackEnabled(),
                        remoteAudioTrack.getName()));
            }

            @Override
            public void onAudioTrackSubscriptionFailed(RemoteParticipant remoteParticipant,
                                                       RemoteAudioTrackPublication remoteAudioTrackPublication,
                                                       TwilioException twilioException) {
                Log.i(getLocalClassName(), String.format("onAudioTrackSubscriptionFailed: " +
                                "[RemoteParticipant: identity=%s], " +
                                "[RemoteAudioTrackPublication: sid=%b, name=%s]" +
                                "[TwilioException: code=%d, message=%s]",
                        remoteParticipant.getIdentity(),
                        remoteAudioTrackPublication.getTrackSid(),
                        remoteAudioTrackPublication.getTrackName(),
                        twilioException.getCode(),
                        twilioException.getMessage()));
            }

            @Override
            public void onDataTrackSubscribed(RemoteParticipant remoteParticipant,
                                              RemoteDataTrackPublication remoteDataTrackPublication,
                                              RemoteDataTrack remoteDataTrack) {
                Log.i(getLocalClassName(), String.format("onDataTrackSubscribed: " +
                                "[RemoteParticipant: identity=%s], " +
                                "[RemoteDataTrack: enabled=%b, name=%s]",
                        remoteParticipant.getIdentity(),
                        remoteDataTrack.isEnabled(),
                        remoteDataTrack.getName()));
            }

            @Override
            public void onDataTrackUnsubscribed(RemoteParticipant remoteParticipant,
                                                RemoteDataTrackPublication remoteDataTrackPublication,
                                                RemoteDataTrack remoteDataTrack) {
                Log.i(getLocalClassName(), String.format("onDataTrackUnsubscribed: " +
                                "[RemoteParticipant: identity=%s], " +
                                "[RemoteDataTrack: enabled=%b, name=%s]",
                        remoteParticipant.getIdentity(),
                        remoteDataTrack.isEnabled(),
                        remoteDataTrack.getName()));
            }

            @Override
            public void onDataTrackSubscriptionFailed(RemoteParticipant remoteParticipant,
                                                      RemoteDataTrackPublication remoteDataTrackPublication,
                                                      TwilioException twilioException) {
                Log.i(getLocalClassName(), String.format("onDataTrackSubscriptionFailed: " +
                                "[RemoteParticipant: identity=%s], " +
                                "[RemoteDataTrackPublication: sid=%b, name=%s]" +
                                "[TwilioException: code=%d, message=%s]",
                        remoteParticipant.getIdentity(),
                        remoteDataTrackPublication.getTrackSid(),
                        remoteDataTrackPublication.getTrackName(),
                        twilioException.getCode(),
                        twilioException.getMessage()));
            }

            @Override
            public void onVideoTrackSubscribed(RemoteParticipant remoteParticipant,
                                               RemoteVideoTrackPublication remoteVideoTrackPublication,
                                               RemoteVideoTrack remoteVideoTrack) {
                Log.i(getLocalClassName(), String.format("onVideoTrackSubscribed: " +
                                "[RemoteParticipant: identity=%s], " +
                                "[RemoteVideoTrack: enabled=%b, name=%s]",
                        remoteParticipant.getIdentity(),
                        remoteVideoTrack.isEnabled(),
                        remoteVideoTrack.getName()));
//                addRemoteParticipantVideo(remoteVideoTrack);
            }

            @Override
            public void onVideoTrackUnsubscribed(RemoteParticipant remoteParticipant,
                                                 RemoteVideoTrackPublication remoteVideoTrackPublication,
                                                 RemoteVideoTrack remoteVideoTrack) {
                Log.i(getLocalClassName(), String.format("onVideoTrackUnsubscribed: " +
                                "[RemoteParticipant: identity=%s], " +
                                "[RemoteVideoTrack: enabled=%b, name=%s]",
                        remoteParticipant.getIdentity(),
                        remoteVideoTrack.isEnabled(),
                        remoteVideoTrack.getName()));
//                removeParticipantVideo(remoteVideoTrack);
            }

            @Override
            public void onVideoTrackSubscriptionFailed(RemoteParticipant remoteParticipant,
                                                       RemoteVideoTrackPublication remoteVideoTrackPublication,
                                                       TwilioException twilioException) {
                Log.i(getLocalClassName(), String.format("onVideoTrackSubscriptionFailed: " +
                                "[RemoteParticipant: identity=%s], " +
                                "[RemoteVideoTrackPublication: sid=%b, name=%s]" +
                                "[TwilioException: code=%d, message=%s]",
                        remoteParticipant.getIdentity(),
                        remoteVideoTrackPublication.getTrackSid(),
                        remoteVideoTrackPublication.getTrackName(),
                        twilioException.getCode(),
                        twilioException.getMessage()));
                Toast.makeText(ModelActivity.this,
                        String.format("Failed to subscribe to %s video track",
                                remoteParticipant.getIdentity()),
                        Toast.LENGTH_LONG)
                        .show();
            }

            @Override
            public void onAudioTrackEnabled(RemoteParticipant remoteParticipant,
                                            RemoteAudioTrackPublication remoteAudioTrackPublication) {

            }

            @Override
            public void onAudioTrackDisabled(RemoteParticipant remoteParticipant,
                                             RemoteAudioTrackPublication remoteAudioTrackPublication) {

            }

            @Override
            public void onVideoTrackEnabled(RemoteParticipant remoteParticipant,
                                            RemoteVideoTrackPublication remoteVideoTrackPublication) {

            }

            @Override
            public void onVideoTrackDisabled(RemoteParticipant remoteParticipant,
                                             RemoteVideoTrackPublication remoteVideoTrackPublication) {

            }
        };
    }

    /*
     * Called when remote participant joins the room
     */
    @SuppressLint("SetTextI18n")
    private void addRemoteParticipant(RemoteParticipant remoteParticipant) {

        /*
         * Start listening for participant events
         */
        remoteParticipant.setListener(remoteParticipantListener());
    }
}
