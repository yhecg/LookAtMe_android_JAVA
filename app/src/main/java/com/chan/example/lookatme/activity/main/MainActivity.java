package com.chan.example.lookatme.activity.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.chan.example.lookatme.R;
import com.chan.example.lookatme.function.BackPressCloseHandler;
import com.chan.example.lookatme.service.ClientServerConnectService;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 로그인 성공 시 들어오는 메인 화면. 프래그먼트.
 * 총 4개의 프래그먼트로 구성되어 있다.
 * FollowPidFragment : 내가 팔로우한 사람들의 피드 정보를 볼 수 있다.
 * AllPidFragment : 전체 사용자의 피드 정보를 볼 수 있다.
 * ChatListFragment : 채팅방 목록을 볼 수 있다.
 * MyPidFragment : 내 피드 정보를 볼 수 있다.
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    /**
     * 뒤로가기 두 번 눌러 앱 종료.
     */
    private BackPressCloseHandler backPressCloseHandler;

    /**
     * BindView
     *  - tabLayout : 화면 상단의 탭레이아웃. 총 4개의 프래그먼트로 구성되어 있다.
     *                FollowPidFragment, AllPidFragment, ChatListFragment, MyPidFragment.
     *  - viewPager : 프래그먼트와 연결된 viewPager.
     */
    @BindView(R.id.tabLayout)
    TabLayout tabLayout;
    @BindView(R.id.viewPager)
    ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        backPressCloseHandler = new BackPressCloseHandler(this);

        final ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragment(R.drawable.ic_frag_after_home, new FollowPidFragment());
        viewPagerAdapter.addFragment(R.drawable.ic_frag_before_search, new AllPidFragment());
        viewPagerAdapter.addFragment(R.drawable.ic_frag_before_chat, new ChatListFragment());
        viewPagerAdapter.addFragment(R.drawable.ic_frag_before_person, new MyPidFragment());

        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setCurrentItem(0);
        viewPager.setOffscreenPageLimit(viewPagerAdapter.getCount()-1);

        tabLayout.setupWithViewPager(viewPager);

        // for 문을 통해 FragmentInfo 에 저장된 iconResId 정보를 가져와서 setIcon 으로 설정.
        for(int i=0; i<viewPager.getAdapter().getCount(); i++){
            tabLayout.getTabAt(i).setIcon(viewPagerAdapter.getFragmentInfo(i).getIconResId());
        }

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                switch (position){
                    case 0:
                        tabLayout.getTabAt(0).setIcon(R.drawable.ic_frag_after_home);
                        tabLayout.getTabAt(1).setIcon(R.drawable.ic_frag_before_search);
                        tabLayout.getTabAt(2).setIcon(R.drawable.ic_frag_before_chat);
                        tabLayout.getTabAt(3).setIcon(R.drawable.ic_frag_before_person);
                        break;
                    case 1:
                        tabLayout.getTabAt(0).setIcon(R.drawable.ic_frag_before_home);
                        tabLayout.getTabAt(1).setIcon(R.drawable.ic_frag_after_search);
                        tabLayout.getTabAt(2).setIcon(R.drawable.ic_frag_before_chat);
                        tabLayout.getTabAt(3).setIcon(R.drawable.ic_frag_before_person);
                        break;
                    case 2:
                        tabLayout.getTabAt(0).setIcon(R.drawable.ic_frag_before_home);
                        tabLayout.getTabAt(1).setIcon(R.drawable.ic_frag_before_search);
                        tabLayout.getTabAt(2).setIcon(R.drawable.ic_frag_after_chat);
                        tabLayout.getTabAt(3).setIcon(R.drawable.ic_frag_before_person);
                        break;
                    case 3:
                        tabLayout.getTabAt(0).setIcon(R.drawable.ic_frag_before_home);
                        tabLayout.getTabAt(1).setIcon(R.drawable.ic_frag_before_search);
                        tabLayout.getTabAt(2).setIcon(R.drawable.ic_frag_before_chat);
                        tabLayout.getTabAt(3).setIcon(R.drawable.ic_frag_after_person);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // 페이지의 상태 자체.
                // state 는 0,1,2 총 3가지 값이 존재
                // 0 : SCROLL-STATE_IDLE 종료 시점
                // 1 : SCROLL_STATE_DRAGGING 드래그 되고 있는 중. Swipe 될 때 호출.
                // 2 : 고정.
            }
        });
    }

    private static class FragmentInfo{
        private int iconResId;
        private Fragment fragment;

        public FragmentInfo(int iconResId, Fragment fragment) {
            this.iconResId = iconResId;
            this.fragment = fragment;
        }

        public int getIconResId() {
            return iconResId;
        }

        public Fragment getFragment() {
            return fragment;
        }
    }

    private class ViewPagerAdapter extends FragmentStatePagerAdapter{

        ArrayList<FragmentInfo> mFragmentInfoArrayList = new ArrayList<FragmentInfo>();

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public void addFragment(int iconResId, Fragment fragment){
            FragmentInfo fragmentInfo = new FragmentInfo(iconResId,fragment);
            mFragmentInfoArrayList.add(fragmentInfo);
        }

        public FragmentInfo getFragmentInfo(int position){
            return mFragmentInfoArrayList.get(position);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentInfoArrayList.get(position).getFragment();
        }

        @Override
        public int getCount() {
            return mFragmentInfoArrayList.size();
        }
    }

    @Override
    public void onBackPressed() {
        backPressCloseHandler.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        Intent intent_service = new Intent(MainActivity.this, ClientServerConnectService.class);
        stopService(intent_service);
        super.onDestroy();
    }
}
