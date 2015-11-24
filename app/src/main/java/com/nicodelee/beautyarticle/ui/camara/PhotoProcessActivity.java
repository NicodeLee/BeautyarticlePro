package com.nicodelee.beautyarticle.ui.camara;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.nicodelee.beautyarticle.R;
import com.nicodelee.beautyarticle.app.APP;
import com.nicodelee.beautyarticle.app.BaseAct;
import com.nicodelee.beautyarticle.utils.L;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import jp.co.cyberagent.android.gpuimage.GPUImageView;

/**
 * Created by NocodeLee on 15/11/24.
 * Email：lirizhilirizhi@163.com
 * 照片编辑
 */
public class PhotoProcessActivity extends BaseAct {

  @Bind(R.id.gpuimage) GPUImageView gpuimage;
  @Bind(R.id.drawing_view_container) RelativeLayout drawingViewContainer;
  @Bind(R.id.list_tools) RecyclerView listTools;
  @Bind(R.id.toolbar_area) LinearLayout toolbarArea;
  @Bind(R.id.main_area) LinearLayout mainArea;
  @Bind(R.id.title) TextView title;
  @Bind(R.id.left) ImageView left;
  @Bind(R.id.right) ImageView right;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_image_process);
    ButterKnife.bind(this);
    initView();
  }

  private void initView() {
    title.setText("编辑");
    left.setImageResource(R.drawable.ic_arrow_back_white_24dp);
    Intent intent = getIntent();
    Uri uri = intent.getData();
    L.e(String.format("uri = %S", uri));

    if (uri != null) {
      Bitmap bitmap =
          APP.getInstance().imageLoader.loadImageSync(uri + "", new ImageSize(720, 1280));
      gpuimage.setImage(bitmap);
    }
  }
}
