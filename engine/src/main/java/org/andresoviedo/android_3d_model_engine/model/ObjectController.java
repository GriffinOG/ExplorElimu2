package org.andresoviedo.android_3d_model_engine.model;

import android.util.Log;

import org.andresoviedo.android_3d_model_engine.controller.TouchEvent;
import org.andresoviedo.android_3d_model_engine.view.ModelRenderer;
import org.andresoviedo.util.event.EventListener;

import java.util.EventObject;

public class ObjectController implements EventListener {

    private final Object3DData object3DData;
    private int width;
    private int height;

    public ObjectController(Object3DData object3DData) {
        this.object3DData = object3DData;
    }

    private float[] rotateBy;

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
                    float max = Math.max(width, height);
                    Log.v("ObjectController", "Rotating object Initial (dx,dy) '" + dx1 + "','" + dy1 + "'...");
                    dx1 = dx1 * 2;
                    dy1 = dy1 * 2;
                    Log.v("ObjectController", "Rotating object final (dx,dy) '" + dx1 + "','" + dy1 + "'...");

                    rotateBy = new float[] {dx1, dy1, 0};
                    break;
                case PINCH:
//                    float zoomFactor = ((TouchEvent) event).getZoom() / 10;
//                    Log.v("CameraController", "Zooming '" + zoomFactor + "'...");
//                    camera.MoveCameraZ(zoomFactor);
                    break;
                case SPREAD:
//                    float[] rotation = touchEvent.getRotation();
//                    Log.v("CameraController", "Rotating camera '" + Math.signum(rotation[2]) + "'...");
//                    camera.Rotate((float) (Math.signum(rotation[2]) / Math.PI) / 4);
                    break;
            }
        }
        return true;
    }

    public float[] getRotateBy() {
        return rotateBy;
    }

    public void setRotateBy(float[] rotateBy) {
        this.rotateBy = rotateBy;
    }
}
