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
    File base;
    String path;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Получить через файл макета RecyclerView и присвоить локальной переменной
        fileList = (RecyclerView) inflater.inflate(R.layout.media_list_fragment, container, false);

        // Получить переданный путь и присвоить локальной переменной
        path = getArguments().get("path").toString();

        // Создать экземпляр GridLayoutManager для показа содержимого RecyclerView в виде плитки
        glm = new GridLayoutManager(getContext(), 2);
        // Присвоить экземпляру RecyclerView экзмепляр класса GridLayoutManager
        fileList.setLayoutManager(glm);

        // Вызов функции для установки адаптера для RecyclerView
        setAdapter();
        // Вернуть полученный через LayoutInflater RecyclerView в качестве объекта View для показа в Activity
        return fileList;
    }

    public void setAdapter() {
        // Переменная для хранения объекта класса File, который хранит в себе путь к папке с изображениями и видео
        base = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "My Lesson Camera/" + path);

        // Проверить существует ли корневая папка
        if (!base.exists()) {
            // Если папка не существует что может быть при первой установке программы создать эти папки
            base.mkdirs();
        }
        // Создать адптер, куда передаётся путь к файлам и параметр со значением 0 если надо создать миниатюры для файлов в папке Photos
        // и 1 если надо сгенерировать миниатюры для файлов в папке Videos
        adapter = new FileAdapter(getContext(), base.listFiles(), path.equals("Photos") ? 0 : 1);
        // Установить адаптер для показа элементов в списке
        fileList.setAdapter(adapter);
    }

    // Функция служит для обновления списка с файлами, которая вызывается из MainActivity
    // при съемке новых фото и видео
    public void UpdateAdapter() {
        // Вызвать функцию UpdateDataSet из адаптера для передачи списка файлов вместе с новыми файлами для обновления списка
        adapter.UpdateDataSet(base.listFiles());
    }

}
