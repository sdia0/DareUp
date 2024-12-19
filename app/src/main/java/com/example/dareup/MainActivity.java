package com.example.dareup;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Получаем данные из Intent
        Intent intent = getIntent();
        long retryAfter = intent.getLongExtra("retryAfter", 0);  // Получаем retryAfter (по умолчанию 0)
        boolean updateNeeded = intent.getBooleanExtra("updateNeeded", false);  // Проверяем флаг

        if (updateNeeded) {
            // Передаем сигнал фрагменту для обновления данных
            Bundle result = new Bundle();
            result.putBoolean("update", true);  // Добавляем флаг
            getSupportFragmentManager().setFragmentResult("updateRequest", result);
        }

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        // Передаем retryAfter и другие параметры в адаптер
        viewPager.setAdapter(new ScreenSlidePagerAdapter(this, retryAfter));

        // Устанавливаем текущий элемент на второй (индекс 1) - HomeFragment
        viewPager.setCurrentItem(1);

        // Привязываем TabLayout к ViewPager2
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setIcon(R.drawable.ic_tab1_inactive);
                            break;
                        case 1:
                            tab.setIcon(R.drawable.ic_tab2_inactive);
                            break;
                        case 2:
                            tab.setIcon(R.drawable.ic_tab3_inactive);
                            break;
                    }
                }
        ).attach();

        showTabLayoutForAWhile();

        // Прозрачный статус-бар
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(Color.TRANSPARENT);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        // Устанавливаем слушатель для отслеживания смены фрагментов
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // Показать TabLayout при смене страницы
                showTabLayoutForAWhile();
            }
        });
    }

    private class ScreenSlidePagerAdapter extends FragmentStateAdapter {
        private long retryAfter;  // Переменная для хранения retryAfter
        public ScreenSlidePagerAdapter(FragmentActivity fa, long retryAfterValue) {
            super(fa);
            this.retryAfter = retryAfterValue;  // Присваиваем значение переменной класса
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new TasksFragment();
                case 1:
                    return HomeFragment.newInstance(retryAfter);  // Передаем параметры в HomeFragment
                case 2:
                    return new LeaderboardFragment();
                default:
                    return new HomeFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 3; // Количество фрагментов
        }
    }

    private void showTabLayoutForAWhile() {
        // Показать TabLayout
        tabLayout.setVisibility(View.VISIBLE);
        tabLayout.setAlpha(1f); // прозрачность на максимум

        // Таймер для скрытия TabLayout через 1 секунду
        new Handler().postDelayed(() -> {
            // Анимация для скрытия TabLayout
            tabLayout.animate()
                    .alpha(0f) // прозрачность до 0
                    .setDuration(500) // длительность анимации в миллисекундах
                    .withEndAction(() -> tabLayout.setVisibility(View.GONE)); // скрыть по завершении анимации
        }, 1000); // Показать TabLayout на 1 секунду
    }

    public void updateTasksFragment() {
        // Получение TasksFragment
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("f" + viewPager.getCurrentItem());
        if (fragment instanceof TasksFragment) {
            ((TasksFragment) fragment).updateData(); // Вызов метода обновления
        }
    }
}