package com.nicodelee.beautyarticle.ui.fun;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.nicodelee.beautyarticle.R;
import com.nicodelee.beautyarticle.app.APP;
import com.nicodelee.beautyarticle.bus.CropEvent;
import com.nicodelee.beautyarticle.ui.camara.CameraActivity;
import com.nicodelee.beautyarticle.utils.AndroidUtils;
import com.nicodelee.beautyarticle.utils.DevicesUtil;
import com.nicodelee.beautyarticle.utils.L;
import com.nicodelee.beautyarticle.utils.SharImageHelper;
import com.nicodelee.beautyarticle.utils.ShareHelper;
import com.nicodelee.beautyarticle.viewhelper.LayoutToImage;
import com.nicodelee.view.CropImageView;
import de.greenrobot.event.EventBus;
import java.io.File;
import java.util.ArrayList;
import me.nereo.multi_image_selector.MultiImageSelectorActivity;

/**
 * Created by Nicodelee on 15/9/25.
 */
public class SquareFragment extends TemplateBase {

  private Bitmap bitmap;
  private static final int REQUEST_IMAGE = 2;
  private static final int REQUEST_PORTRAIT_FFC = 3;

  @Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_square, container, false);
    ButterKnife.bind(this, view);
    initView();
    return view;
  }

  private void initView() {
    inflater = LayoutInflater.from(mActivity);
    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) ivFun.getLayoutParams();
    int width = DevicesUtil.screenWidth - DevicesUtil.dip2px(getActivity(), 16f);
    params.width = width;
    params.height = width;
    ivFun.setLayoutParams(params);
    layoutToImage = new LayoutToImage(scFun);
  }

  @OnClick({ R.id.fb_share, R.id.fb_make, R.id.iv_fun }) public void Click(View view) {
    famFun.close(true);
    switch (view.getId()) {
      case R.id.fb_share:
        bitmap = layoutToImage.convertlayout();
        SharImageHelper sharImageHelper = new SharImageHelper();
        if (sharImageHelper.saveBitmap(bitmap, SharImageHelper.sharePicName)) {
          ShareHelper.showUp(mActivity, sharImageHelper.getShareMod(bitmap));
        }

        break;
      case R.id.fb_make:
        showEdDialig(false);
        break;
      case R.id.iv_fun:
        //int selectedMode = MultiImageSelectorActivity.MODE_SINGLE;
        //MultiImageSelectorActivity.startSelect(SquareFragment.this, REQUEST_IMAGE, 1, selectedMode);
        showChiocePicDialog();
        break;
    }
  }

  private void showChiocePicDialog(){
    String[] items = new String[]{"拍照", "相册"};
    new AlertDialog.Builder(mActivity).setItems(items, new DialogInterface.OnClickListener() {
      @Override public void onClick(DialogInterface dialog, int which) {
        switch (which){
          case 0:
            Intent i=new CameraActivity.IntentBuilder(getActivity())
                .skipConfirm()
                .facing(CameraActivity.Facing.BACK)
                .to(new File(AndroidUtils.IMAGE_CACHE_PATH, "nicodelee.jpg"))
                .debug()
                .updateMediaStore()
                .build();

            startActivityForResult(i, REQUEST_PORTRAIT_FFC);
            break;
          case 1:
            int selectedMode = MultiImageSelectorActivity.MODE_SINGLE;
            MultiImageSelectorActivity.startSelect(getActivity(), REQUEST_IMAGE, 1, selectedMode);
            break;
        }
      }
    }).create().show();
  }

  @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode == mActivity.RESULT_OK && requestCode == REQUEST_IMAGE) {
      ArrayList<String> mSelectPath =
          data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
      CropEvent cropEvent = new CropEvent();
      cropEvent.setCropMode(CropImageView.CropMode.RATIO_1_1);
      cropEvent.setImagePath(mSelectPath.get(0));
      EventBus.getDefault().postSticky(cropEvent);
      skipIntent(CropAct.class, false);
    }
  }

  public void onEvent(Bitmap corpBitmap) {
    ivFun.setImageBitmap(corpBitmap);
  }

  public void onEvent(Uri uri) {//拍照后编辑
    L.e("图片编辑");
    APP.getInstance().imageLoader.displayImage(uri+"",ivFun,APP.options);
  }
}
