package company.my.lesson19;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    // Коды используемые для отправки запроса на получение разрешений
    // и получения результата снятых на камеру данных
    // RCODE для фото, VCODE для видео
    public static final int RCODE = 0;
    public static final int VCODE = 1;
    public static final int FCODE = 2;

    // Перменные для хранения ссылок виджетов объявленных в файле макета
    private Toolbar toolbar;
    private ViewPager viewPager;
    private TabLayout tabLayout;

    // Переменная для сохранения сгенерированного пути для фото и видео
    // в этой переменной храниться путь для последнего созданного файла
    String mediaPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Получить ссылку виджета Toolbar, который необходим для замены стандартного тулбара
        toolbar = findViewById(R.id.toolbar);
        // Установить в качестве системного тулбара, Toolbar объявленный в макете
        setSupportActionBar(toolbar);
        // Установить заголовок для Activity, который показывается в Toolbar-e
        setTitle("Галерея");

        // ViewPager - виджет для просмотра фрагментов в виде страниц переключаемых по свайпу
        viewPager = findViewById(R.id.viewpager);
        // TabLayout - виджет для создания вкладок
        tabLayout = findViewById(R.id.tabs);

        // Если нет необходимых розрешений, тогда запросить их
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // Если не дано разрешение на использование камеры, спросить разрешение и отправить код запроса
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, FCODE);
        } else {
            // Если есть разрешеиня, вызвать функцию для настройки ViewPager-a и добавления необходимых фрагментов
            setupViewPager(viewPager);
            // Функция для привязки ViewPager-a к TabLayout-у
            tabLayout.setupWithViewPager(viewPager);
        }
    }

    // Функция для добавления фрагментов в ViewPager
    void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        MediaList photoList = new MediaList();
        Bundle args = new Bundle();
        args.putString("path", "Photos");
        photoList.setArguments(args);

        MediaList videoList = new MediaList();
        Bundle args2 = new Bundle();
        args2.putString("path", "Videos");
        videoList.setArguments(args2);

        adapter.addFragment(photoList, "Фото");
        adapter.addFragment(videoList, "Видео");
        viewPager.setAdapter(adapter);
    }


    // Класс адаптера для ViewPager-a
    class ViewPagerAdapter extends FragmentPagerAdapter {
        // Массив для хранения фрагментов (страниц)
        private final List<Fragment> mFragmentList = new ArrayList<>();
        // Массив для хранения массива заголовков фрагментов дяя показа на вкладках
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    // Функция для запуска камеры для снятия фотографий
    void showCamera() {
        // Инициализировать экземпляр камеры для снятия фото
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Объявить и инициализировать файл со значением null
        File photoFile = null;
        try {
            // Попытаться присвоить созданному файлу значение возвращаемое функцией createImageFile
            photoFile = createImageFile();

            // Перехват исключений связанных с созданием нового файла
        } catch (Exception e) {
            // Вывод трассировки стека вызовов функций в Logcat
            e.printStackTrace();
        }

        // В случае создания временного файла значение photoFile меняется с null на значение созданного файла
        // и данная проверка возвращает результат true
        if (photoFile != null) {
            // Получить URI для файла, который служит путём для сохранения снятой фотографии
            Uri photoURI = FileProvider.getUriForFile(this, "com.example.android.fileprovider", photoFile);
            // Добавить значение пути сохранения фото через extra передаваемое Intent-у камеры
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            // Запустить Activity и ждать до получения результата (пока Activity камеры не будет закрыто)
            // и отправить код запроса в Activity
            startActivityForResult(cameraIntent, RCODE);
        }
    }

    // Функция для создания временного фото
    File createImageFile() throws IOException {
        // Получить значение текущей даты отформатированного по формату yyyyMMdd_HHmmss:
        // y - год, M - месяц, d - день, H - часы, m - минуты, s - секунды
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        // Строка для хранения названия файла имеет формат CAM_yyyyMMdd_HHmmss_
        String fileName = "CAM_" + timestamp + "_";
        // Получить путь к папке для хранения фотографии
        // в данном случае выбрана стандартная папка, где хранятся фотографии
        // затем внутри папки фотографий создаётся папка My Lesson Camera и внутри неё создаётся папка Photos
        // это необходимо, чтобы отделить снятые видео от снятых фотографий и в общем снятые через это приложение
        // файлы от других файлов в памяти устройства
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "My Lesson Camera/Photos");

        // Проверить удалось ли создать указанные выше папки, если они не существуют
        if (!storageDir.exists() && !storageDir.mkdirs()) {
            // Вывыести ошибку в консоль Logcat если не получилось создать папки
            Log.e("TAG", "Problems creating directory");
        }

        // Создать новый временный файл, который затем планируется заменить фото полученным с камеры
        File image = File.createTempFile(fileName, ".jpg", storageDir);

        // Присвоить путь файла переменой mediaPath для дальнейшего оповещения
        // галереи о новом файле, для незамедлителього появления данного файла в галерее
        mediaPath = image.getAbsolutePath();

        // Верунть созденный временный файл вызвавшей данную функцию строке
        return image;
    }

    // Функция openVideo работает так же как openCamera
    void openVideo() {
        // Инициализация экземпляра камеры для снятия видео
        Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        File videoFile = null;

        try {
            videoFile = createVideoFile();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (videoFile != null) {
            Uri videoUri = FileProvider.getUriForFile(this, "com.example.android.fileprovider", videoFile);
            videoIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri);
            // Запустить Activity и передать код запроса видео
            startActivityForResult(videoIntent, VCODE);
        }
    }

    // Данная функция работает аналогично функции createImageFile
    // отличается лишь путь сохранения видео и расширение видео
    File createVideoFile() throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = "VCAM_" + timestamp + "_";
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "My Lesson Camera/Videos");
        if (!storageDir.exists() && !storageDir.mkdirs()) {
            Log.e("TAG", "Problems creating directory");
        }
//        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File video = File.createTempFile(fileName, ".mp4", storageDir);

        mediaPath = video.getAbsolutePath();
        return video;
    }

    // Оповещение галереи о добавлении нового файла
    void galleryAddMedia() {
        // Получить ссылку на Intent проводящий сканирование медиафайлов в файловой системе
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        // Создать на основе пути созданного медиафайла
        // в функции createImageFile или createVideoFile
        File f = new File(mediaPath);
        // Получить уникальный идентификатор ресурса на основе файла
        Uri contentUri = Uri.fromFile(f);
        // Передать путь интенту для сканирования и добавления данного файла в галерею
        mediaScanIntent.setData(contentUri);
        // Отправить широковещательное сообщение с параметрами интента, которое затем
        // перехватывается необходимым системным процессом для обработки
        this.sendBroadcast(mediaScanIntent);
    }

    // Функция вызывается при возвращении значений с других Activity,
    // которые были запущены через функцию startActivityForResult
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Если возвращенный код равен коду запроса на снятие фото и код результата имеет статус RESULT_OK
        if (requestCode == RCODE && resultCode == RESULT_OK) {
            galleryAddMedia();

            // Получить доступ к адаптеру и ...
            ViewPagerAdapter adapter = (ViewPagerAdapter)viewPager.getAdapter();
            // ... через адаптер получить доступ к фрагменту ...
            MediaList f = (MediaList) adapter.getItem(viewPager.getCurrentItem());
            // ... вызвать функцию обновления адаптера для фрагмента, для обновления списка и показа нового файла в списке
            f.UpdateAdapter();
        }
        if (requestCode == RCODE && resultCode == RESULT_CANCELED) {
            File file = new File(mediaPath);
            if (file.delete()) {
                Log.i("TAG", "Temp file deleted");
            }
        }
        // Если возвращенный код равен коду запроса на снятие видео и код результата имеет статус RESULT_OK
        if (requestCode == VCODE && resultCode == RESULT_OK) {
            galleryAddMedia();
            ViewPagerAdapter adapter = (ViewPagerAdapter)viewPager.getAdapter();
            MediaList f = (MediaList) adapter.getItem(viewPager.getCurrentItem());
            f.UpdateAdapter();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mainmenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.takePhoto) {
            // Проверить разрешение для доступа к камере
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // Если не дано разрешение на использование камеры, спросить разрешение и отправить код запроса
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                        Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, RCODE);

            } else {
                // Если получено разрешение открыть камеру для снятия фото
                showCamera();
            }
        }
        if (item.getItemId() == R.id.recordVideo) {
            // Спросить разрешение на использование камеры
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // Если нет разрешение, запросить и отправить код запроса видео
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                        Manifest.permission.CAMERA}, VCODE);

            } else {
                // Если есть разрешение открыть камеру для снятия видео
                openVideo();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    // Функция вызывается после запроса на разрешение использования системных ресурсов
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // Если код запроса равен коду запроса на снятие фото или видео
        if (requestCode == RCODE || requestCode == VCODE || requestCode == FCODE) {
            // Если в списке результатов ответа на запрос разрешения есть элементы, значит разрешение дано
            if (grantResults.length > 0) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    // Если код полученного разрешения равен коду запроса открытия камеры на съёмку фото
                    // открыть камеру в режиме записи видео
                    if (requestCode == RCODE) {
                        showCamera();
                    }
                    // Если код полученного разрешения равен коду запроса открытия камеры на съёмку видео
                    // открыть камеру в режиме записи видео
                    if (requestCode == VCODE) {
                        openVideo();
                    }

                    // Результат первоначального запроса для получения доступа к камере и файловой системе
                    if (requestCode == FCODE) {
                        setupViewPager(viewPager);
                        tabLayout.setupWithViewPager(viewPager);
                    }
                }
            }
        }
    }
}