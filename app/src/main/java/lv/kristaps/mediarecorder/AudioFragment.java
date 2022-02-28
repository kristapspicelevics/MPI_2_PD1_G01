package lv.kristaps.mediarecorder;

import android.Manifest;
import android.content.ContentProvider;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AudioFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AudioFragment extends Fragment {

    private static int MICROPHONE_PERMISSION_CODE = 200;
    MediaRecorder mediaRecorder;
    MediaPlayer mediaPlayer;
    ArrayList<String> mAudioList = new ArrayList<>();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AudioFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AudioFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AudioFragment newInstance(String param1, String param2) {
        AudioFragment fragment = new AudioFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public static Fragment newInstance() {
        AudioFragment fragment = new AudioFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View audioView = inflater.inflate(R.layout.fragment_audio, container, false);
        Button start = audioView.findViewById(R.id.startRecord);
        Button stop = audioView.findViewById(R.id.stopRecord);
        ListView mAudioListView = audioView.findViewById(R.id.audioListView);



        //detail of each audio
        String[] mAudioDetailArray = { MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DISPLAY_NAME};

        //INTERNAL_CONTENT_URI to display audio from internal storage
        //EXTERNAL_CONTENT_URI to display audio from external storage
        ContextWrapper contextWrapper = new ContextWrapper(getActivity().getApplicationContext());
        File musicDirectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        try {
            Cursor mAudioCursor = getActivity().getContentResolver().query(Uri.fromFile(musicDirectory), mAudioDetailArray, null, null, null);
        //    Cursor mAudioCursor = getActivity().getContentResolver().query(MediaStore.Audio.Media.INTERNAL_CONTENT_URI, mAudioDetailArray, null, null, null);
            if(mAudioCursor != null){
                if(mAudioCursor.moveToFirst()){
                    do{
                        int audioIndex = mAudioCursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);

                        mAudioList.add(mAudioCursor.getString(audioIndex));
                    }while(mAudioCursor.moveToNext());
                }
            }
            mAudioCursor.close();
            }
            catch (Exception e){
                e.printStackTrace();
            }
        ArrayAdapter<String> mAdapter = new ArrayAdapter<>(getActivity(),android.R.layout.simple_list_item_1,android.R.id.text1, mAudioList);
        mAudioListView.setAdapter(mAdapter);

        if (isMicPresent()){
            getMicPermission();
        }
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    mediaRecorder = new MediaRecorder();
                    mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                    mediaRecorder.setOutputFile(getRecordingFilePath());
                    mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                    mediaRecorder.prepare();
                    mediaRecorder.start();

                    Toast.makeText(getActivity(), "Recording starting", Toast.LENGTH_LONG).show();
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    mediaRecorder.stop();
                    mediaRecorder.release();
                    mediaRecorder = null;
                    Toast.makeText(getActivity(), "End of recording", Toast.LENGTH_LONG).show();
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        mAudioListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try{
                    int index = position;
                    String pathToFile = mAudioList.get(index);
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setDataSource(pathToFile);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        return audioView;
    }

    private boolean isMicPresent(){
        if (getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_MICROPHONE)){
            return true;
        }
        else{
            return false;
        }
    }
    private void getMicPermission (){
        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.RECORD_AUDIO},MICROPHONE_PERMISSION_CODE );
        }
    }

    private String getRecordingFilePath(){
            int num = new Random().nextInt(10000);
            ContextWrapper contextWrapper = new ContextWrapper(getActivity().getApplicationContext());
            File musicDirectory = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
            File file = new File(musicDirectory, "AudioFile"+ num + ".mp3");
            return file.getPath();
        }

}