package com.nicodelee.beautyarticle.ui.fun;

import android.os.Bundle;
import butterknife.ButterKnife;
import com.nicodelee.beautyarticle.R;
import com.nicodelee.beautyarticle.app.BaseAct;

/**
 * Created by NicodeLee on 15/11/4.
 */
public class TakePhotoActivity extends BaseAct {

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_take_photo);
    ButterKnife.bind(this);
  }



}
