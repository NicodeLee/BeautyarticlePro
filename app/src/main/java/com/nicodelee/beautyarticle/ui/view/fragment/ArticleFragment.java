package com.nicodelee.beautyarticle.ui.view.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.nicodelee.beautyarticle.R;
import com.nicodelee.beautyarticle.app.APP;
import com.nicodelee.beautyarticle.app.BaseFragment;
import com.nicodelee.beautyarticle.app.BaseSupportFragment;
import com.nicodelee.beautyarticle.mode.ActicleMod;
import com.nicodelee.beautyarticle.ui.presenter.ArticleDetailPresenter;
import com.nicodelee.beautyarticle.utils.Logger;
import com.nicodelee.beautyarticle.utils.UILUtils;
import java.util.ArrayList;
import nucleus.factory.PresenterFactory;
import nucleus.factory.RequiresPresenter;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

@RequiresPresenter(ArticleDetailPresenter.class) public class ArticleFragment
    extends BaseSupportFragment<ArticleDetailPresenter> {

  @Bind(R.id.wv_acticle_detail) WebView webView;
  @Bind(R.id.tv_acticle_detail) TextView tvDetail;
  @Bind(R.id.ic_acticle) ImageView ivActicle;
  @Bind(R.id.collapsing_toolbar) CollapsingToolbarLayout collapsingToolbar;
  @Bind(R.id.toolbar) Toolbar toolbar;
  @Bind(R.id.fb_share) FloatingActionButton share;

  public static final String EXTRA_POSITION = "ARTICLE_POSITION";
  private int position;

  @Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_article, container, false);
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    position = getArguments().getInt(EXTRA_POSITION);
    initView();
  }

  @JavascriptInterface private void initView() {
    ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
    ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    WebSettings webSettings = webView.getSettings();
    webSettings.setJavaScriptEnabled(true);
    webSettings.setLoadWithOverviewMode(true);//自适应
    webSettings.setUseWideViewPort(true);
    webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
    webSettings.setAppCacheEnabled(true);
    webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
    webView.addJavascriptInterface(this, "handler");
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        mActivity.finish();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @OnClick(R.id.fb_share) public void Click(View view) {
    //share
  }

  @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
  public void onEvent(ArrayList<ActicleMod> eventList) {
    ActicleMod mod = eventList.get(position);
    collapsingToolbar.setTitle(mod.title + "");
    APP.getInstance().imageLoader.displayImage(mod.image, ivActicle, APP.options,
        new UILUtils.AnimateFirstDisplayListener());
    if (mod.type.equals("Markdown")) {
      webView.setVisibility(View.VISIBLE);
      tvDetail.setVisibility(View.GONE);

      setMardown(mod.details);
      //setUpWebView(mod.details);
    } else if (mod.type.equals("text")) {
      webView.setVisibility(View.GONE);
      tvDetail.setVisibility(View.VISIBLE);
      tvDetail.setText(mod.details);
    }
  }

  private void setMardown(final String text) {
    Observable.create(new Observable.OnSubscribe<Boolean>() {
      @Override public void call(Subscriber<? super Boolean> subscriber) {
        setUpWebView(text);
        subscriber.onNext(true);
        subscriber.onCompleted();
      }
    })
        //.subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Boolean>() {
      @Override public void call(Boolean aBoolean) {
      }
    });
  }

  private void setUpWebView(final String mdText) {
    webView.setWebViewClient(new WebViewClient() {
      @Override public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        loadMarkDown(mdText.replaceAll("(\\r|\\n|\\r\\n)+", "\\\\n"));
      }
    });
    webView.loadUrl("file:///android_asset/markdown.html");
  }

  private void loadMarkDown(String str) {
    webView.loadUrl("javascript:parseMarkdown(\"" + str + "\")");
  }

  @Override protected void injectorPresenter() {
    super.injectorPresenter();
    final PresenterFactory<ArticleDetailPresenter> superFactory = super.getPresenterFactory();
    setPresenterFactory(new PresenterFactory<ArticleDetailPresenter>() {
      @Override public ArticleDetailPresenter createPresenter() {
        ArticleDetailPresenter presenter = superFactory.createPresenter();
        getApiComponent().inject(presenter);
        return presenter;
      }
    });
  }
}
