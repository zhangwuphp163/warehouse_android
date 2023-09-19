package hk.timeslogistics.wms;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;


import java.util.Objects;

import hk.timeslogistics.wms.fragments.asn.AsnItemFragment;
import hk.timeslogistics.wms.fragments.asn.AsnListFragment;
import hk.timeslogistics.wms.fragments.handover.ConfirmHandoverFragment;
import hk.timeslogistics.wms.fragments.handover.HandoverByTrackingNumberFragment;
import hk.timeslogistics.wms.fragments.HomeFragment;
import hk.timeslogistics.wms.fragments.inbound.InboundBySNFragment;
import hk.timeslogistics.wms.fragments.inbound.InboundFragment;
import hk.timeslogistics.wms.fragments.inventory_check.InventoryCheckListFragment;
import hk.timeslogistics.wms.fragments.inventory_check.InventoryCheckTaskFragment;
import hk.timeslogistics.wms.fragments.inventory_check.InventoryCheckTaskItemFragment;
import hk.timeslogistics.wms.fragments.inventory.InventoryFragment;
import hk.timeslogistics.wms.fragments.order.OrderFragment;
import hk.timeslogistics.wms.fragments.pick_wave.HandlingUnitFragment;
import hk.timeslogistics.wms.fragments.pick_wave.PickWaveDetailFragment;
import hk.timeslogistics.wms.fragments.pick_wave.PickWaveFragment;
import hk.timeslogistics.wms.fragments.put_away.PutAwayByBoxIdFragment;
import hk.timeslogistics.wms.fragments.put_away.PutAwayFragment;
import hk.timeslogistics.wms.fragments.put_away.PutAwayTaskFragment;
import hk.timeslogistics.wms.fragments.relocation.RelocationByBoxIdFragment;
import hk.timeslogistics.wms.fragments.relocation.RelocationFragment;
import hk.timeslogistics.wms.fragments.pick_wave.SecondSortFragment;
import hk.timeslogistics.wms.utils.CredentialManager;

public class MainActivity extends AppCompatActivity {
    public static Fragment homeFragment;
    public static Fragment inboundFragment ;
    public static Fragment inboundBySnFragment ;
    public static Fragment asnListFragment;
    public static Fragment relocationFragment;
    public static Fragment putAwayFragment;
    public static Fragment inventoryFragment;
    public static Fragment pickWaveFragment;
    public static Fragment handoverByTrackingNumberFragment;
    public static Fragment confirmHandoverFragment;
    public static Fragment secondSortFragment;
    public static Fragment currentFragment;
    public static Fragment pickWaveDetailFragment;
    public static Fragment inventoryCheckListFragment;
    public static Fragment inventoryCheckTaskFragment;
    public static Fragment inventoryCheckTaskItemFragment;
    public static Fragment asnItemFragment;
    public static Fragment handlingUnitFragment;
    public static Fragment putAwayByBoxIdFragment;
    public static Fragment relocationByBoxIdFragment;
    public static Fragment putAwayTaskFragment;
    public static Fragment orderFragment;
    public static ImageView soft;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CredentialManager credentialManager = new CredentialManager(this);
        credentialManager.setFragmentIndex("");
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        ImageView soft = (ImageView) findViewById(R.id.soft);
        setSupportActionBar(toolbar);
        ImageView iconBack = (ImageView) findViewById(R.id.iconBack);
        homeFragment = new HomeFragment();
        inboundFragment = new InboundFragment();
        inboundBySnFragment = new InboundBySNFragment();
        asnListFragment = new AsnListFragment();
        relocationFragment = new RelocationFragment();
        putAwayFragment = new PutAwayFragment();
        putAwayByBoxIdFragment = new PutAwayByBoxIdFragment();
        inventoryFragment = new InventoryFragment();
        inventoryCheckListFragment = new InventoryCheckListFragment();
        inventoryCheckTaskFragment = new InventoryCheckTaskFragment();
        inventoryCheckTaskItemFragment = new InventoryCheckTaskItemFragment();
        pickWaveFragment = new PickWaveFragment();
        handoverByTrackingNumberFragment = new HandoverByTrackingNumberFragment();
        confirmHandoverFragment = new ConfirmHandoverFragment();
        secondSortFragment = new SecondSortFragment();
        pickWaveDetailFragment = new PickWaveDetailFragment();
        asnItemFragment = new AsnItemFragment();
        handlingUnitFragment = new HandlingUnitFragment();
        relocationByBoxIdFragment = new RelocationByBoxIdFragment();
        putAwayTaskFragment = new PutAwayTaskFragment();
        orderFragment = new OrderFragment();
        iconBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goHome();
            }
        });
        goHome();
        // Handle intent
        Intent intent = getIntent();
        String intentUrl = intent.getDataString();
        if (intentUrl != null) {
            Fragment fragment = new HomeFragment();
            Bundle bundle = new Bundle();
            bundle.putString("url", intentUrl);
            fragment.setArguments(bundle);

            switchFragment(fragment);
        }
        soft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideKeyboard();
            }
        });
    }

    public static class NoImeEditText extends android.support.v7.widget.AppCompatEditText {
        public NoImeEditText(Context context, AttributeSet attrs) {
            super(context, attrs);
        }
        @Override
        public boolean onCheckIsTextEditor() {
            return false;
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        assert imm != null;
        imm.toggleSoftInput(1,InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.popBackStackImmediate();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if(currentFragment == MainActivity.inventoryCheckTaskItemFragment){
            Fragment fragment = MainActivity.inventoryCheckTaskFragment;
            String cycleCountCode = fragment.getArguments().getString("cycle_count_code");
            transaction.remove(fragment);
            fragment = new InventoryCheckTaskFragment();
            Bundle args = new Bundle();
            args.putString("cycle_count_code", cycleCountCode);
            fragment.setArguments(args);
            transaction.add(R.id.flContent, fragment);
            transaction.hide(MainActivity.currentFragment).show(fragment).commit();

            MainActivity.currentFragment = fragment;
            MainActivity.inventoryCheckTaskFragment = fragment;
        }else if(currentFragment == MainActivity.inventoryCheckTaskFragment){
            Fragment fragment = MainActivity.inventoryCheckListFragment;
            transaction.remove(fragment);
            fragment = new InventoryCheckListFragment();
            transaction.add(R.id.flContent, fragment);
            transaction.hide(MainActivity.currentFragment).show(fragment).commit();
            MainActivity.currentFragment = fragment;
            MainActivity.inventoryCheckListFragment = fragment;
        }else if(homeFragment.isHidden()){
            goHome();
        }else{
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                super.onBackPressed();
            }
        }
    }

    public void onDestroy() {
        super.onDestroy();
    }
    private void switchFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.popBackStackImmediate();
        fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();
    }

    public void goHome() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.popBackStackImmediate();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if(!homeFragment.isAdded()){
            if(currentFragment != null){
                transaction.hide(MainActivity.currentFragment);
            }
            transaction.add(R.id.flContent, homeFragment).commit();
        }else{
            transaction.hide(currentFragment).show(homeFragment).commit();
        }
        currentFragment = homeFragment;
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.home);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.openDrawer(GravityCompat.START);
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
}
