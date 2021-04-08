package org.andresoviedo.android_3d_model_engine.camera;

import android.util.Log;

import org.andresoviedo.android_3d_model_engine.controller.TouchEvent;
import org.andresoviedo.android_3d_model_engine.inclass.ui.SceneViewModel;
import org.andresoviedo.android_3d_model_engine.model.Camera;
import org.andresoviedo.android_3d_model_engine.view.ModelRenderer;
import org.andresoviedo.util.event.EventListener;

import java.util.EventObject;

public final class CameraController implements EventListener {

    private final Camera camera;
    private final SceneViewModel sceneViewModel;
    private int width;
    private int height;

    public CameraController(Camera camera, SceneViewModel sceneViewModel) {
        this.camera = camera;
        this.sceneViewModel = sceneViewModel;
    }

    @Override
    public boolean onEvent(EventObject event) {
        if (event instanceof ModelRenderer.ViewEvent){
            this.width = ((ModelRenderer.ViewEvent) event).getWidth();
            this.height = ((ModelRenderer.ViewEvent) event).getHeight();
        }
        else if (event instanceof TouchEvent) {
            TouchEvent touchEvent = (TouchEvent) event;
            switch (touchEvent.getAction()){
                case CLICK:
                    break;
                case MOVE:
                    float dx1 = touchEvent.getdX();
                    float dy1 = touchEvent.getdY();
                    sceneViewModel.updateCameraPos(dx1, dy1);
                    translateCamera(dx1, dy1);
                    break;
                case PINCH:
                    float zoom = ((TouchEvent) event).getZoom();
                    sceneViewModel.updateCameraZoom(zoom);
                    cameraZoom(zoom);
                    break;
                case SPREAD:
                    float[] rotation = touchEvent.getRotation();
                    Log.v("CameraController", "Rotating camera '" + Math.signum(rotation[2]) + "'...");
                    camera.Rotate((float) (Math.signum(rotation[2]) / Math.PI) / 4);
                    break;
            }
        }
        return true;
    }

    public void translateCamera(float dx1, float dy1){
        float max = Math.max(width, height);
        Log.v("CameraController", "Translating camera (dx,dy) '" + dx1 + "','" + dy1 + "'...");
        dx1 = (float) (dx1 / max * Math.PI * 2);
        dy1 = (float) (dy1 / max * Math.PI * 2);
        camera.translateCamera(dx1, dy1);
    }

    public void cameraZoom(float zoom){
        float zoomFactor = zoom / 10;
        Log.v("CameraController", "Zooming '" + zoomFactor + "'...");
        camera.MoveCameraZ(zoomFactor);
    }
}
