package company.my.lesson19;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

// Класс адаптера для привязки и показа данных в RecyclerView
public class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder> {
    // Локальный массив для хранения файлов
    private List<File> items;
    // Ассоциативный массив (словарь) - массив с ключами и значениями, в отличии от обычного массива
    // в котором ключи это числовые индексы позиции элементов, в HashMap ключом может быть объект любого класса
    private HashMap<String, Bitmap> thumbs = new HashMap<>();
    // Контекст в котором заружен RecyclerView к которому прикреплен этот адаптер
    private Context context;
    // По значению этой переменной определяется генерация миниатюр для изображений или видео
    int type = 0;

    // Функция для обновления списка для показа новых добавленных файлов
    public void UpdateDataSet(File[] items) {
        // Обновить массив, используя конвертацию обычного массива в ArrayList
        this.items = new ArrayList<>(Arrays.asList(items));
        // Вызвать функцию для привязки элементов из нового источника к RecyclerView
        notifyDataSetChanged();
    }

    // Вложенный класс класса FileAdapter для хранения объектов элементов RecyclerView
    static class ViewHolder extends RecyclerView.ViewHolder {
        // Ссылка на TextView в котором выводится заголовок файла
        TextView title;
        // Ссылка на TextView для вывода размера файла
        TextView size;
        // Ссылка на ImageView для показа миниатюры изображения или видео
        ImageView thumb;

        ImageButton rename;
        ImageButton delete;

        // Конструктор класса для присвоения переменным соответствующих
        // объектов из файла макета
        ViewHolder(View itemView) {
            // Необходимо вызвать суперкласс ViewHolder-а и передать корневой View
            super(itemView);
            // Получить ссылку к виджетам в корневом View
            title = itemView.findViewById(R.id.fileTitle);
            thumb = itemView.findViewById(R.id.thumb);
            size = itemView.findViewById(R.id.size);
            rename = itemView.findViewById(R.id.rename);
            delete = itemView.findViewById(R.id.delete);
        }
    }

    // Конструктор класса FileAdapter где получаются значения
    // из вызываюшего кода и присваиваются локальным переменным класса
    FileAdapter(Context context, File[] items, int type) {
        this.items = new ArrayList<>(Arrays.asList(items));
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
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.title.setText(items.get(position).getName());
        holder.size.setText(String.format(Locale.ROOT, "Размер: %d Кб", (int) ((float) items.get(position).length() / (float) 1024)));
        if (thumbs.containsKey(items.get(position).getAbsolutePath())) {
            holder.thumb.setImageBitmap(thumbs.get(items.get(position).getAbsolutePath()));
        } else {
            holder.thumb.setImageDrawable(context.getResources().getDrawable(R.drawable.placeholder));
            GenerateThumb gt = new GenerateThumb();
            // Запустить поток для генерации миниатюр для изображений или видео
            gt.execute(items.get(position).getAbsolutePath(), holder.thumb);
        }

        // Установить обработчик нажатия на кнопку редактирования объекта
        holder.rename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Инициализация диалогового окна для переименования файла
                RenameDialog renameDialog = new RenameDialog();
                // Объект для хранения параметров для передачи в диалоговое окно переименования файла
                Bundle args = new Bundle();
                // Добавить название файла и позицию файла с ключами name и position в парметры
                // передаваемые в диалоговое окно переименования файла
                args.putString("name", items.get(position).getName());
                args.putInt("position", position);
                // Установить аргументы для передачи
                renameDialog.setArguments(args);
                // Указать ссылку на адаптер, для получения доступа к адаптеру через диалоговое окно переименования файла
                renameDialog.adapter = FileAdapter.this;
                // Показать диалоговое окно для переименования файла
                renameDialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "RenameDialog");
            }
        });

        // Установить обработчик клика на кнопку удалить
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Заключить попытку аделния файла в блок try catch для перехвата ошибок
                // и предотвращения сбоев в работе приложения
                try {
                    // Объявить временную переменную для хранения выбранного для удаление файла
                    File f = new File(items.get(position).toURI());
                    // Функция File.delete() возвращает true при удалении файла без ошибок,
                    // здесь проверяется удалился ли файл, если да тогда...
                    if (f.delete()) {
                        // ... удалить объект из массива
                        items.remove(position);
                        // сообщить адаптеру что объект удален, нужно для удаления объекта из списка  и анимации
                        notifyItemRemoved(position);
                        // сообщить адаптеру что объекты в указанном диапазоне изменились, нужно для корректировки новых позиции элементов
                        notifyItemRangeChanged(position, getItemCount());
                        // сообщить пользователю что файл удален
                        Toast.makeText(context, "Файл удален", Toast.LENGTH_SHORT).show();
                    } else {
                        // Если вызывается эта часть кода значит фалй не был удален
                        Toast.makeText(context, "Файл не удалён", Toast.LENGTH_SHORT).show();
                    }
                    // В случае возникновения ошибок перехватить их и обработать здесь
                } catch (Exception e) {
                    // Вывод в консоль информацию о возникшей ошибке
                    e.printStackTrace();
                }
            }
        });

    }

    // Функция для переименования файла
    public void renameFile(String newName, int position) {
        // Создать новый пустой файл с новым именем переименованного файла
        File file = new File(items.get(position).getParentFile().getAbsolutePath() + "/" + newName);
        // Переименовать выбранный файл и присвоить новое имя
        if (items.get(position).renameTo(file)) {
            // Если получилось переименовать тогда удалить из массива старый файл
            items.remove(position);
            // Добавить новый файл на место удаленного файла
            items.add(position, file);
            // Сообщить адаптеру что инфрмация изменилась
            notifyItemChanged(position);
        }
    }

    // Получить количество элементов в списке
    @Override
    public int getItemCount() {
        return items.size();
    }

    // Поток для генерации миниатюр изображений
    class GenerateThumb extends AsyncTask<Object, Bitmap, Bitmap> {
        // Ссылка на ImageView в котором необходимо показать сгенерированную миниатюру
        ImageView iv;

        // Функция выполняется в фоновом режиме, чтобы приложение не зависло при генерации миниатюр
        @Override
        protected Bitmap doInBackground(Object... params) {
            // Присвоить локальной переменной ImageView ссылку на ImageView для которой генерируется миниатюра
            iv = (ImageView) params[1];
            // Получить путь к файлу на основе которого генерируется миниатюра
            // Путь передан через параметры во время запуска потока
            String path = params[0].toString();
            // Переменная для хранения миниатюры
            Bitmap thumb;
            // Если type == 0 значит надо сгенерировать миниатюру для изображения
            if (type == 0) {
                // Создать объект Bitmap на основе пути к файлу
                Bitmap bitmap = BitmapFactory.decodeFile(path);
                // Присвоить временной переменной значение сгенерированного Bitmap-a размером 256 на 256 пикселей
                thumb = ThumbnailUtils.extractThumbnail(bitmap, 256, 256);
                // Иначе сгенерировать миниатюру для видео
            } else {
                // Сгенерировать миниатюру для видео и присвоить временной переменной, размер миниатюры поставить системным рамзером 512 на 384 пикселей,
                // потому что для миниатюр есть два размера которые указываются через константы, это MINI_KIND и MICRO_KIND 96 на 96 пикселей
                thumb = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Video.Thumbnails.MINI_KIND);
            }
            // Добавить миниатюру в HashMap, указав ключом путь к файлу и значением объект Bitmap
            thumbs.put(path, thumb);
            // publishProgress выполняется в главном потоке и служит для обновления пользовательского
            // интерфейса, который вызывается из потока выполнемого в фоновом режиме, так как поток фонового режима
            // не имеет возможности изменять объекты View
            publishProgress(thumb);
            // Значение return в данном случае не исользуется и если необходимо использовать надо дописать
            // функцию onPostExecute который и принимает в качестве параметра значения рассчитанные или обработанные
            // в фоновом потоке
            return null;
        }

        // Функция вызывается для обновления пользовательского интерфейса в промежутках обработки фонового потока
        // если не вызывать эту функцию то пользователю придется ждать пока все миниатюры не будут сгенерированы
        // и только потом показать их, а вызывая эту функцию при генерации каждой миниатюры они постепенно появляются уже
        // в пользовательском интерфейсе
        @Override
        protected void onProgressUpdate(Bitmap... values) {
            super.onProgressUpdate(values);
            // Устанавливает в ImageView сгенерированную миниатюру
            iv.setImageBitmap(values[0]);
        }
    }
}
