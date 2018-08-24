package company.my.lesson19;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.io.File;
import java.util.List;

public class MediaList extends Fragment {
    RecyclerView fileList;
    FileAdapter adapter;
    GridLayoutManager glm;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fileList = (RecyclerView) inflater.inflate(R.layout.media_list_fragment, container, false);
//        fileList = v.findViewById(R.id.fileList);

        String path = getArguments().get("path").toString();

        File photos = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "My Lesson Camera/" + path);

        if (!photos.exists()) {
            photos.mkdirs();
        }

        glm = new GridLayoutManager(getContext(), 2);
        fileList.setLayoutManager(glm);

        adapter = new FileAdapter(getContext(), photos.listFiles(), path.equals("Photos") ? 0 : 1);
        fileList.setAdapter(adapter);

        return fileList;
    }
}
