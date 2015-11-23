package com.nicodelee.beautyarticle.ui.camara;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ViewSwitcher;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.commonsware.cwac.cam2.CameraView;
import com.nicodelee.beautyarticle.R;
import com.nicodelee.beautyarticle.app.BaseAct;
import com.nicodelee.beautyarticle.ui.camara.view.RevealBackgroundView;
import java.io.File;

/**
 * Created by NicodeLee on 15/11/4.
 */
public class TakePhotoActivity extends BaseAct
    implements RevealBackgroundView.OnStateChangeListener{
  public static final String ARG_REVEAL_START_LOCATION = "reveal_start_location";

  private static final Interpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();
  private static final Interpolator DECELERATE_INTERPOLATOR = new DecelerateInterpolator();
  private static final int STATE_TAKE_PHOTO = 0;
  private static final int STATE_SETUP_PHOTO = 1;

  @Bind(R.id.vRevealBackground) RevealBackgroundView vRevealBackground;
  @Bind(R.id.vPhotoRoot) View vTakePhotoRoot;
  @Bind(R.id.vShutter) View vShutter;
  @Bind(R.id.ivTakenPhoto) ImageView ivTakenPhoto;
  @Bind(R.id.vUpperPanel) ViewSwitcher vUpperPanel;
  @Bind(R.id.vLowerPanel) ViewSwitcher vLowerPanel;
  @Bind(R.id.cameraView) CameraView cameraView;
  @Bind(R.id.rvFilters) RecyclerView rvFilters;
  @Bind(R.id.btnTakePhoto) Button btnTakePhoto;

  private boolean pendingIntro;
  private int currentState;

  private File photoPath;

  public static void startCameraFromLocation(int[] startingLocation, Activity startingActivity) {
    Intent intent = new Intent(startingActivity, TakePhotoActivity.class);
    intent.putExtra(ARG_REVEAL_START_LOCATION, startingLocation);
    startingActivity.startActivity(intent);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_take_photo);
    ButterKnife.bind(this);
    updateStatusBarColor();
    updateState(STATE_TAKE_PHOTO);
    setupRevealBackground(savedInstanceState);
    setupPhotoFilters();

    vUpperPanel.getViewTreeObserver()
        .addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
          @Override public boolean onPreDraw() {
            vUpperPanel.getViewTreeObserver().removeOnPreDrawListener(this);
            pendingIntro = true;
            vUpperPanel.setTranslationY(-vUpperPanel.getHeight());
            vLowerPanel.setTranslationY(vLowerPanel.getHeight());
            return true;
          }
        });
  }

  @TargetApi(Build.VERSION_CODES.LOLLIPOP) private void updateStatusBarColor() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      getWindow().setStatusBarColor(0xff111111);
    }
  }

  private void setupRevealBackground(Bundle savedInstanceState) {
    vRevealBackground.setFillPaintColor(0xFF16181a);
    vRevealBackground.setOnStateChangeListener(this);
    if (savedInstanceState == null) {
      final int[] startingLocation = getIntent().getIntArrayExtra(ARG_REVEAL_START_LOCATION);
      vRevealBackground.getViewTreeObserver()
          .addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override public boolean onPreDraw() {
              vRevealBackground.getViewTreeObserver().removeOnPreDrawListener(this);
              vRevealBackground.startFromLocation(startingLocation);
              return true;
            }
          });
    } else {
      vRevealBackground.setToFinishedFrame();
    }
  }

  private void setupPhotoFilters() {
    PhotoFiltersAdapter photoFiltersAdapter = new PhotoFiltersAdapter(this);
    rvFilters.setHasFixedSize(true);
    rvFilters.setAdapter(photoFiltersAdapter);
    rvFilters.setLayoutManager(
        new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
  }

  private void animateShutter() {
    vShutter.setVisibility(View.VISIBLE);
    vShutter.setAlpha(0.f);

    ObjectAnimator alphaInAnim = ObjectAnimator.ofFloat(vShutter, "alpha", 0f, 0.8f);
    alphaInAnim.setDuration(100);
    alphaInAnim.setStartDelay(100);
    alphaInAnim.setInterpolator(ACCELERATE_INTERPOLATOR);

    ObjectAnimator alphaOutAnim = ObjectAnimator.ofFloat(vShutter, "alpha", 0.8f, 0f);
    alphaOutAnim.setDuration(200);
    alphaOutAnim.setInterpolator(DECELERATE_INTERPOLATOR);

    AnimatorSet animatorSet = new AnimatorSet();
    animatorSet.playSequentially(alphaInAnim, alphaOutAnim);
    animatorSet.addListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationEnd(Animator animation) {
        vShutter.setVisibility(View.GONE);
      }
    });
    animatorSet.start();
  }

  @Override public void onStateChange(int state) {
    if (RevealBackgroundView.STATE_FINISHED == state) {
      vTakePhotoRoot.setVisibility(View.VISIBLE);
      if (pendingIntro) {
        startIntroAnimation();
      }
    } else {
      vTakePhotoRoot.setVisibility(View.INVISIBLE);
    }
  }

  private void startIntroAnimation() {
    vUpperPanel.animate().translationY(0).setDuration(400).setInterpolator(DECELERATE_INTERPOLATOR);
    vLowerPanel.animate()
        .translationY(0)
        .setDuration(400)
        .setInterpolator(DECELERATE_INTERPOLATOR)
        .start();
  }


  private void showTakenPicture(Bitmap bitmap) {
    vUpperPanel.showNext();
    vLowerPanel.showNext();
    ivTakenPhoto.setImageBitmap(bitmap);
    updateState(STATE_SETUP_PHOTO);
  }

  @Override public void onBackPressed() {
    if (currentState == STATE_SETUP_PHOTO) {
      btnTakePhoto.setEnabled(true);
      vUpperPanel.showNext();
      vLowerPanel.showNext();
      updateState(STATE_TAKE_PHOTO);
    } else {
      super.onBackPressed();
    }
  }

  private void updateState(int state) {
    currentState = state;
    if (currentState == STATE_TAKE_PHOTO) {
      vUpperPanel.setInAnimation(this, R.anim.slide_in_from_right);
      vLowerPanel.setInAnimation(this, R.anim.slide_in_from_right);
      vUpperPanel.setOutAnimation(this, R.anim.slide_out_to_left);
      vLowerPanel.setOutAnimation(this, R.anim.slide_out_to_left);
      new Handler().postDelayed(new Runnable() {
        @Override public void run() {
          ivTakenPhoto.setVisibility(View.GONE);
        }
      }, 400);
    } else if (currentState == STATE_SETUP_PHOTO) {
      vUpperPanel.setInAnimation(this, R.anim.slide_in_from_left);
      vLowerPanel.setInAnimation(this, R.anim.slide_in_from_left);
      vUpperPanel.setOutAnimation(this, R.anim.slide_out_to_right);
      vLowerPanel.setOutAnimation(this, R.anim.slide_out_to_right);
      ivTakenPhoto.setVisibility(View.VISIBLE);
    }
  }
}
