package com.hamster.translaterapp;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import android.support.v7.app.AppCompatActivity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.hamster.translaterapp.fragments.FavoritesFragment;
import com.hamster.translaterapp.fragments.HistoryFragment;
import com.hamster.translaterapp.fragments.MainFragmentPagerAdapter;
import com.hamster.translaterapp.fragments.SettingsFragment;
import com.hamster.translaterapp.fragments.TranslateFragment;


public class Main extends AppCompatActivity implements ViewPager.OnPageChangeListener {

    private MainFragmentPagerAdapter adapter;

    private ViewPager fragmentPager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        // сохранять в опции экран и языки и восстанавливать корректно при восстановлении вкладки
        // при назначаении из истории любимое не работает

        // Фишка: история независима от кеша
        // Фишка: изменение времени автоперевода при разном количестве рекста. больше текста больше время
        // фищка при переходе из истории или избранного не удалять автонаправление перевода
        // не забыть уузать почему не заюзал виртуал лейаут в истори и лббоми

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        TranslaterModel.init(this);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        fragmentPager = (ViewPager)findViewById(R.id.viewpager);

        adapter = new MainFragmentPagerAdapter(getSupportFragmentManager());
        TranslateFragment translateFragment = new TranslateFragment();
        adapter.addFragment(translateFragment, getResources().getString(R.string.first_screen));
        adapter.addFragment(new FavoritesFragment(), getResources().getString(R.string.second_screen));
        adapter.addFragment(new HistoryFragment(), getResources().getString(R.string.third_screen));
        adapter.addFragment(new SettingsFragment(), getResources().getString(R.string.fourth_screen));

        fragmentPager.setAdapter(adapter);

        // это можно снести непонятно че оно делает
        //adapter.notifyDataSetChanged();

        // связка табов и вьюпейджера
        tabLayout.setupWithViewPager(fragmentPager);

        TranslaterModel.getIstance().setTranslateFragment(translateFragment);
        //TranslaterModel.getIstance().setHistoryFragment(historyFragment);



        int[] tabIcons = {
                R.drawable.ic_translate_black_24dp,
                R.drawable.ic_favorite_black_24dp,
                R.drawable.ic_assignment_black_24dp,
                R.drawable.ic_settings_black_24dp
        };

        for (int i = 0; i < Math.min(tabIcons.length, tabLayout.getTabCount()); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            tab.setIcon(tabIcons[i]);
        }

        fragmentPager.addOnPageChangeListener(this);


    }

    public void goToFragmnetByIndex(int index) {
        fragmentPager.setCurrentItem(index);
    }

    @Override
    protected void onResume() {
        super.onResume();

        TranslaterModel.init(this);
        //TranslaterModel.getIstance().setTranslateFragment(translateFragment);
        //TranslaterModel.getIstance().setHistoryFragment(historyFragment);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
       // скрытие клавиатуры при переходе между экранами. Нужно для перехода из экрана перевода.
        if(getCurrentFocus() != null)
            TranslaterModel.getIstance().closeKeyBoard(getCurrentFocus().getWindowToken());
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }
}


/*
https://academy.yandex.ru/events/mobdev/msk-2017/

Поле для ввода текста, который будет переведён на другой язык; переключатель языка и варианты перевода, которые появляются, когда пользователь вводит текст в поле.
Возможность добавить переведённое слово или предложение в избранное.
Возможность просмотра истории переводов.
Возможность просмотра избранного.
Перевод с одного языка на другой с помощью API Яндекс.Переводчика.
Чтобы вам было легче, мы подготовили примерный вид экранов тестового приложения:

Наш тестировщик, который будет проверять общую работоспособность и качество приложения, очень радуется, когда замечает:
- отсутствие «падений» и непредусмотренного поведения приложения при выполнении основных действий,
- общую плавность и отзывчивость интерфейса, красивую анимацию,
- понятный интерфейс — чтобы перед использованием приложения не приходилось читать инструкции.

Наш разработчик, который будет проверять код, очень радуется, когда видит:
- комментарии в коде,
- обработку ошибок,
- кэширование (например, можно научить приложение сохранять предыдущий ответ сервера),
- тесты.*/
