package testpolyface.com.testpolyface;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceContour;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.Facing;
import com.otaliastudios.cameraview.Frame;
import com.otaliastudios.cameraview.FrameProcessor;
import com.otaliastudios.cameraview.Size;

import java.util.List;

import testpolyface.com.testpolyface.graphics.DotDotOverlayYellow;
import testpolyface.com.testpolyface.graphics.DotDotOverlayRed;
import testpolyface.com.testpolyface.graphics.GraphicOverlay;

public class RealtimeActivity extends AppCompatActivity {

    private CameraView cameraView;
    private GraphicOverlay graphicOverlay;


    private FirebaseVisionFaceDetectorOptions options;
    private FirebaseVisionFaceDetector detector;

    private Computation computation;
    private ImageButton referenceButton;

    private int frameCounter;

    private FirebaseVisionFaceContour contourRef;
    private DotDotOverlayRed dotRef;
    private TextView emotionTextView;
    private TextView counterView;



    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraView.stop();
    }

    @Override
    public void onStop() {
        super.onStop();
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realtime);


        //EmotionTextView
        emotionTextView = (TextView) findViewById(R.id.activity_realtime_emotionTextView);
        counterView = (TextView) findViewById(R.id.activity_realtime_counter);
        // Init
        cameraView = findViewById(R.id.activity_realtime_camera_view);
        graphicOverlay = findViewById(R.id.activity_realtime_graphic_overlay);


        // Setting front camera
        cameraView.setFacing(Facing.FRONT);

        // Detection options
        options = new FirebaseVisionFaceDetectorOptions.Builder()
                .setPerformanceMode(FirebaseVisionFaceDetectorOptions.FAST)
                .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
                .setClassificationMode(FirebaseVisionFaceDetectorOptions.NO_CLASSIFICATIONS)
                .build();

        // Creating face detector
        detector = FirebaseVision.getInstance().getVisionFaceDetector(options);


        //Computation
        computation = new Computation(RealtimeActivity.this);
        frameCounter = 0;
        referenceButton = (ImageButton) findViewById(R.id.activity_realtime_reference);
        counterView.setText(String.valueOf(frameCounter));



        // Frame processing to draw points
        cameraView.addFrameProcessor(new FrameProcessor() {
            @Override
            @WorkerThread
            public void process(final Frame frame) {
                graphicOverlay.clear();
                frameCounter++;
                counterView.setText(String.valueOf(frameCounter));
                Log.i("FRAMENB", String.valueOf(frameCounter));

                byte[] data = frame.getData();
                int rotation = frame.getRotation();
                Size size = frame.getSize();

                int frameRotation = rotation / 90;



                FirebaseVisionImageMetadata metadata = new FirebaseVisionImageMetadata.Builder()
                        .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
                        .setWidth(size.getWidth())
                        .setRotation(frameRotation)
                        .setHeight(size.getHeight())
                        .build();

                FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromByteArray(data, metadata);

                int height = firebaseVisionImage.getBitmapForDebugging().getHeight() ;
                int width = firebaseVisionImage.getBitmapForDebugging().getWidth();

                Log.i("SIZE1", "Height " + frame.getSize().getHeight() + " Width " + frame.getSize().getWidth());
                Log.i("SIZE1", "Height " + height + " Width " + width);
                height = cameraView.getPictureSize().getHeight();
                width = cameraView.getPictureSize().getWidth();
                Log.i("SIZE1", "Height " + height + " Width " + width);
                Log.i("SIZE1", "ss");


                Bitmap bitmapdebug = firebaseVisionImage.getBitmapForDebugging();
                detector.detectInImage(firebaseVisionImage)
                        .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace>>()
                        {
                            @Override
                            public void onSuccess(final List<FirebaseVisionFace> firebaseVisionFaces)
                            {
                                if(firebaseVisionFaces.size()!=0)
                                {
                                    //Listener Reference Button
                                    referenceButton.setOnClickListener(new View.OnClickListener()
                                    {
                                        @Override
                                        public void onClick(View v)
                                        {
                                            //TODO set reference points
                                            if(firebaseVisionFaces.size()!=0)
                                            {
                                                computation.setReference(new FacialPoints(firebaseVisionFaces.get(0)));
                                                Toast.makeText(RealtimeActivity.this, "FaceContour referenced", Toast.LENGTH_SHORT).show();
                                                contourRef = firebaseVisionFaces.get(0).getContour(FirebaseVisionFaceContour.ALL_POINTS);
                                                dotRef = new DotDotOverlayRed(graphicOverlay, contourRef.getPoints());
                                            }
                                        }
                                    });

                                    //If counter==5 -> compute

                                    if(frameCounter>15 && computation.getReference()!=null && firebaseVisionFaces.size()!=0)
                                    {
                                        emotionTextView.clearComposingText();
                                        frameCounter = 0;
                                        computation.setActual(new FacialPoints(firebaseVisionFaces.get(0)));
                                        String results = computation.compute();
                                        //TODO affichage des emotions
                                        emotionTextView.setText(results);
                                    }

                                    FirebaseVisionFaceContour contour = firebaseVisionFaces.get(0).getContour(FirebaseVisionFaceContour.ALL_POINTS);
                                    DotDotOverlayYellow dot = new DotDotOverlayYellow(graphicOverlay, contour.getPoints());
                                    if(dotRef!=null)
                                    {
                                        graphicOverlay.add(dotRef);
                                    }
                                    graphicOverlay.add(dot);
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener()
                        {
                            @Override
                            public void onFailure(@NonNull Exception e)
                            {
                                Toast.makeText(RealtimeActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }
}
