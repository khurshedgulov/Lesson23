package company.my.lesson19;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class RenameDialog extends DialogFragment {
    // Объвление переменных для хранения ссылки на экземпляр адаптера, целое значение позиции
    // названия файла и расширения файла, которое получаем из названия
    public FileAdapter adapter;
    public int position;
    public String name;
    public String extension = "";
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Объявление экземпляра AlertDialog.Builder для создания диалогового окна
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        // Получение LayoutInflater-a
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // Присвоить переменной View макет загруженный через LayoutInflater
        View v = inflater.inflate(R.layout.rename_file_dialog, null, false);

        // Текстовое поле для ввода нового имени файла
        final EditText newFileName = v.findViewById(R.id.newFileName);

        // Получение переданных аргументов с ключами name и position и присвоение локальным переменным
        Bundle args = getArguments();
        name = args.getString("name");
        position = args.getInt("position");

        // Установить в качестве введенного текста старое название файла
        newFileName.setText(name);

        // Проверить на существование точки, которая пишется перед расширением файла
        // String.lastInfdexOf(char) - возвращает последнюю позицию в виде числового значения указанного внутри скобок символа или строки
        if(name.lastIndexOf('.') > 0)
        {
            // Если есть расширение файла присвоить это расширение переменной extension путём получения только расширения
            // через функцию String.substring(startIndex, endIndex)
            extension = name.substring(name.lastIndexOf('.'), name.length());
        }

        // При клике на текстовое поле открывается клавиатура и в это время надо выбрать
        // из старого названия часть строки до расширения файла, для удобства работы пользователя
        // для исключения траты времени на выбор текста в текстовом поле и удаление ненужной части
        newFileName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // При клике текстовое поле передаётся в качестве объекта класса View
                // тут необходимо используя приведение типов из типа View привести к типу EditText
                EditText editText = (EditText) v;
                // Метод setSelection(start, stop) принимает два параметра: начальный индекс символа от куда начать выделение
                // и конечную позицию символа до какого символа выделить
                // тут выбирается с первого символа (это индекс 0) и до позиции последней точки, которая идёт перед расширением файла, точка не выделяется
                newFileName.setSelection(0, editText.getText().toString().lastIndexOf('.'));
            }
        });

        // Установить полученный через LayoutInflater макет диаологовому окну
        builder.setView(v);
        // Установить сообщение диаологового окна для удобства объяснения пользователю назначения диалогового окна
        builder.setMessage("Введите новое название файла");
        // Установить кнопку Сохранить и соответствующее действие выполняемое при клике на кнопку сохранить
        builder.setPositiveButton("Сохранить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Получить и присвоить переменной введенный в текстовое поле текст
                String tempName = newFileName.getText().toString();
                // Если длина введенного названия больше 0
                if (tempName.length() > 0) {
                    // Здесь идет проверка не удалено ли расширение файла
                    // и если функция lastIndexOf возвращает значение меньше 0, значит расширение в текстовом поле удалено
                    if(tempName.lastIndexOf('.') <= 0)
                    {
                        // в таком случае расширение которое было получено в верхней части этого кода добавить к концу нового названия файла
                        tempName += extension;
                    }
                    // Вызвать функцию renameFile через ссылку на экземпляр адаптера
                    adapter.renameFile(tempName, position);
                    // Закрыть диалоговое окно
                    dismiss();
                }
            }
        });

        // Установить кнопку Отмена, которая ничего не делает и только закрывает диалоговое окно
        builder.setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });

        // Вернуть экземпляр диалогового окна
        return builder.create();
    }
}
