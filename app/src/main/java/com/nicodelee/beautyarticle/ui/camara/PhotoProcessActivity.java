package com.nicodelee.beautyarticle.ui.camara;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.nicodelee.beautyarticle.R;
import com.nicodelee.beautyarticle.app.APP;
import com.nicodelee.beautyarticle.app.BaseAct;
import com.nicodelee.beautyarticle.utils.AndroidUtils;
import com.nicodelee.beautyarticle.utils.DevicesUtil;
import com.nicodelee.beautyarticle.utils.L;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import java.util.List;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageView;

/**
 * Created by NocodeLee on 15/11/24.
 * Email：lirizhilirizhi@163.com
 * 照片编辑
 */
public class PhotoProcessActivity extends BaseAct {

  @Bind(R.id.gpuimage) GPUImageView gpuimage;
  @Bind(R.id.list_tools) RecyclerView listTools;
  @Bind(R.id.title) TextView title;
  @Bind(R.id.left) ImageView left;
  @Bind(R.id.right) ImageView right;

  //用于预览的小图片
  private Bitmap smallImageBackgroud;

  static PhotoProcessActivity photoProcessActivity;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_image_process);
    ButterKnife.bind(this);
    initView();
  }

  @OnClick({ R.id.left }) public void Click(View view) {
    switch (view.getId()) {
      case R.id.left:
        finish();
        break;
    }
  }

  private void initView() {
    photoProcessActivity = this;
    title.setText("编辑");
    left.setImageResource(R.drawable.ic_arrow_back_white_24dp);
    Intent intent = getIntent();
    Uri uri = intent.getData();
    L.e(String.format("uri = %S", uri));

    if (uri != null) {
      Bitmap bitmap = APP.getInstance().imageLoader.loadImageSync(uri + "");
      gpuimage.setImage(bitmap);

      smallImageBackgroud =
          APP.getInstance().imageLoader.loadImageSync(uri + "", new ImageSize(120, 120));
    }
    RelativeLayout.LayoutParams rparams =
        new RelativeLayout.LayoutParams(DevicesUtil.screenHeight, DevicesUtil.screenWidth);
    gpuimage.setLayoutParams(rparams);
    initFilterToolBar();
  }

  //初始化滤镜
  private void initFilterToolBar() {
    final List<FilterEffect> filters = EffectService.getInst().getLocalFilters();
    final PhotoFiltersAdapter adapter =
        new PhotoFiltersAdapter(PhotoProcessActivity.this, filters, smallImageBackgroud);
    listTools.setAdapter(adapter);
    listTools.setHasFixedSize(true);
    listTools.setLayoutManager(
        new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
  }
}
