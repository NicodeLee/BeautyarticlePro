package com.nicodelee.beautyarticle.ui.camara;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
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
import com.nicodelee.beautyarticle.utils.TimeUtils;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import de.greenrobot.event.EventBus;
import it.sephiroth.android.library.widget.AdapterView;
import it.sephiroth.android.library.widget.HListView;
import java.util.Date;
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
  @Bind(R.id.left) ImageView left;
  @Bind(R.id.right) ImageView right;
  @Bind(R.id.list_tools) HListView bottomToolBar;
  //@Bind(R.id.list_tools) RecyclerView listTools;
  @Bind(R.id.title) TextView title;

  //用于预览的小图片
  private Bitmap smallImageBackgroud;

  static PhotoProcessActivity photoProcessActivity;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_image_process);
    ButterKnife.bind(this);
    initView();
  }

  @OnClick({ R.id.left,R.id.right }) public void Click(View view) {
    switch (view.getId()) {
      case R.id.left:
        finish();
        break;
      case R.id.right:
        savePicture();
        break;
    }
  }

  private void initView() {
    photoProcessActivity = this;
    title.setText("图片美化");
    left.setImageResource(R.drawable.ic_arrow_back_white_24dp);
    right.setImageResource(R.drawable.ic_arrow_forward_white_24dp);

    Intent intent = getIntent();
    String uri = intent.getStringExtra("uri");
    //Uri uri = intent.getData();

    if (uri != null) {
      Bitmap bitmap = APP.getInstance().imageLoader.loadImageSync(uri,
          new ImageSize(DevicesUtil.screenWidth, DevicesUtil.screenWidth));
      gpuimage.setImage(bitmap);

      smallImageBackgroud =
          APP.getInstance().imageLoader.loadImageSync(uri + "", new ImageSize(80, 80));

      RelativeLayout.LayoutParams rparams =
          new RelativeLayout.LayoutParams(DevicesUtil.screenHeight, DevicesUtil.screenWidth);
      gpuimage.setLayoutParams(rparams);
      initFilterToolBar();
    }
  }

  //保存图片
  private void savePicture() {
    loadingDialog.setMessage("图片保存中..");
    String picName = TimeUtils.dtFormat(new Date(), "yyyyMMddHHmmss");
    gpuimage.saveToPictures(AndroidUtils.PIC_CACHE_PATH, picName + ".jpg",
        new GPUImageView.OnPictureSavedListener() {
          @Override public void onPictureSaved(Uri uri) {
            EventBus.getDefault().post(uri);
            loadingDialog.dismiss();
            finish();
          }
        });
  }

  //初始化滤镜
  private void initFilterToolBar() {
    //RecyclerView bug https://github.com/CyberAgent/android-gpuimage/issues/189
    //LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
    //final List<FilterEffect> filters = EffectService.getInst().getLocalFilters();
    //final PhotoFiltersAdapter adapter =
    //    new PhotoFiltersAdapter(PhotoProcessActivity.this, filters, smallImageBackgroud,linearLayoutManager);
    //listTools.setAdapter(adapter);
    //listTools.setHasFixedSize(true);
    //listTools.setLayoutManager(linearLayoutManager);

    final List<FilterEffect> filters = EffectService.getInst().getLocalFilters();
    final FilterAdapter adapter =
        new FilterAdapter(PhotoProcessActivity.this, filters, smallImageBackgroud);
    bottomToolBar.setAdapter(adapter);
    bottomToolBar.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (adapter.getSelectFilter() != position) {
          adapter.setSelectFilter(position);
          GPUImageFilter filter = GPUImageFilterTools.createFilterForType(PhotoProcessActivity.this,
              filters.get(position).getType());
          gpuimage.setFilter(filter);
          GPUImageFilterTools.FilterAdjuster mFilterAdjuster =
              new GPUImageFilterTools.FilterAdjuster(filter);
          //可调节颜色的滤镜
          if (mFilterAdjuster.canAdjust()) {
            //mFilterAdjuster.adjust(100); 给可调节的滤镜选一个合适的值
          }
        }
      }
    });
  }
}
