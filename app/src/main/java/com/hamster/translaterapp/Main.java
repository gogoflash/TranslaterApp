package com.hamster.translaterapp;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.hamster.translaterapp.data.TranslaterModel;
import com.hamster.translaterapp.fragments.FavoritesFragment;
import com.hamster.translaterapp.fragments.HistoryFragment;
import com.hamster.translaterapp.fragments.MainFragmentPagerAdapter;
import com.hamster.translaterapp.fragments.SettingsFragment;
import com.hamster.translaterapp.fragments.TranslateFragment;

/************************** ОСНОВНЫЕ КЛАССЫ И ИХ РОЛИ *********************************************
 *
 * TranslaterModel - главный класс-модель и класс-менеджер.
 *
 * TranslateManager - работает с запросами и ответами api. Учитывает ошибки и кидает алерты.
 *
 * TranslatesCacheDbHelper - работа с базой данных и простейшие операции.
 *
 * TranslateDataItem - главная моделька для всех параметров одной единицы перевода
 *
 * Основные экраны-фрагменты с соответствующими названиями:
 * TranslateFragment
 * FavoritesFragment
 * HistoryFragment
 * SettingsFragment
 *
 **************************** ФИШЕЧКИ *************************************************************
 *
 * - Список языков подтягивается запросом в api. Основные языки сортируются сверху.
 *
 * - Реализован кеш, откуда подтягиваются данные вместо запроса в апи.
 *
 * - История != кеш! Это важно, так как позволяет при желании иметь оффлайн-базу, если засеттить кеш заранее.
 *
 * - Автоматический перевод введенного текста срабатывает через таймауты. Сами таймауты ступенчато изменяются
 * в зависимости от объема текста. Последнее чтобы не дергать лишний раз апи и реже гонять перевод
 * больших объемов текста. Но при большой задержке юзер всегда может нажать на кнопку, инициирующую перевод.
 *
 * - При восстановлении приложения запоминаются данные последнего перевода. Но именно перевода, а не
 * данные из перехода из истории или избранного.
 *
 * - При переходе из истории или избранного при выбранном автоопределении языка этот спиннер не сбрасывается.
 * Смысл в мнении, что юзер большую часть времени и будет сидеть на автоопределении и сбрасывать каждый
 * раз этот спиннер с языками было бы некомфортно.
 *
 * - Корректное отображение в landscape-ориентации. Возможность скролла объемного текста
 *
 * - По правилам сервиса на основном экране и экране описания(настроек в данном случае)
 * указан текст с активной ссылкой «Переведено сервисом «Яндекс.Переводчик». Важная мелочь.
 *
 ************** ЧТО ПОЯВИТСЯ В ПЛАТНОЙ ВЕРСИИ  (в смысле что не было сделано :)) ******************
 *
 * - оптимизация работы списков истории и избранного при очень длинных списках
 * - работа с кешом переводов который сейчас может только расти, в отличии от чистящихся истории и избранного
 * - больше настроек в экране настроек с блекджеком и шлюпками! С выбором любимых языков как минимум.
 * - подтяг оффлаин-базы переводов
 * - тонкая оптимизация интерфейса под непопулярные разрешения экранов и соотношения сторон
 * - локализация. Это механическое действие и смысла тратить на нее время нет.
 * - указание даты перевода в истории. Сейчас заложено сохранение unix-time в базу.
 * - работа с разными апи переводчиков, но с внедрением неправильных результатов у сторонних апи, чтобы
 * юзер убеждался, что лучший апи у яндекса )
 * - при единичном переводимом слове с подтягом данных api yandex-словаря.
 * - пасхалки на 1 апреля
 * - юнит-тесты. Не успел.
 *
 * ************************************************************************************************/

public class Main extends AppCompatActivity implements ViewPager.OnPageChangeListener {

    private MainFragmentPagerAdapter adapter;

    private ViewPager fragmentPager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
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

        tabLayout.setupWithViewPager(fragmentPager);

        TranslaterModel.getIstance().setTranslateFragment(translateFragment);

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