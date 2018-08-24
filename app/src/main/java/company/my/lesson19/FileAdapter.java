package company.my.lesson19;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

// Класс адаптера для привязки и показа данных в RecyclerView
public class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder> {
    // Локальный массив для хранения файлов
    private File[] items;
    private HashMap<String, Bitmap> thumbs = new HashMap<>();
    private Context context;
    int type = 0;

    // Вложенный класс класса FileAdapter для хранения объектов элементов RecyclerView
    static class ViewHolder extends RecyclerView.ViewHolder {
        // Ссылка на TextView в котором выводится заголовок файла
        TextView title;
        TextView date;
        // Ссылка на ImageView для показа миниатюры изображения или видео
        ImageView thumb;

        // Конструктор класса для присвоения переменным соответствующих
        // объектов из файла макета
        ViewHolder(View itemView) {
            // Необходимо вызвать суперкласс ViewHolder-а и передать корневой View
            super(itemView);
            // Получить ссылку к виджетам в корневом View
            title = itemView.findViewById(R.id.fileTitle);
            thumb = itemView.findViewById(R.id.thumb);
            date = itemView.findViewById(R.id.date);
        }
    }

    // Конструктор класса FileAdapter где получаются значения
    // из вызываюшего кода и присваиваются локальным переменным класса
    FileAdapter(Context context, File[] items, int type) {
        this.items = items;
        this.context = context;
        this.type = type;
    }

    // Функция onCreateViewHolder вызывается при первом создании объекта для хранения элементов списка
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Получаем View объект созданный на основе написанной структуры макета в xml файле file_item.xml
        View v = LayoutInflater.from(context).inflate(R.layout.file_item, parent, false);
        // Создаётся новый объект класса ViewHolder и ему передаётся объект View полученный из файла макета
        return new ViewHolder(v);
    }

    // Функция вызывается при присвоении значений переменным класса ViewHolder
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.title.setText(items[position].getName());
        holder.date.setText(String.format(Locale.ROOT, "Размер: %d Кб", (int) ((float) items[position].length() / (float) 1024)));
        if (thumbs.containsKey(items[position].getAbsolutePath())) {
            holder.thumb.setImageBitmap(thumbs.get(items[position].getAbsolutePath()));
        } else {
            holder.thumb.setImageDrawable(context.getResources().getDrawable(R.drawable.placeholder));
            GenerateThumb gt = new GenerateThumb();
            gt.execute(items[position].getAbsolutePath(), holder.thumb);
        }
    }

    @Override
    public int getItemCount() {
        return items.length;
    }

    class GenerateThumb extends AsyncTask<Object, Bitmap, Bitmap> {
        ImageView iv;

        @Override
        protected Bitmap doInBackground(Object... strings) {
            iv = (ImageView) strings[1];
            String path = strings[0].toString();
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            Bitmap thumb;
            if (type == 0) {
                thumb = ThumbnailUtils.extractThumbnail(bitmap, 256, 256);
            } else {
                thumb = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Video.Thumbnails.MINI_KIND);
            }
            thumbs.put(path, thumb);
            publishProgress(thumb);
            return null;
        }

        @Override
        protected void onProgressUpdate(Bitmap... values) {
            super.onProgressUpdate(values);
            iv.setImageBitmap(values[0]);
        }
    }
}
