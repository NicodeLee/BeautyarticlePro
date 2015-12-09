package com.nicodelee.beautyarticle.ui.presenter;

import android.os.Bundle;
import android.widget.Toast;
import com.nicodelee.beautyarticle.api.BeautyApi;
import com.nicodelee.beautyarticle.app.APP;
import com.nicodelee.beautyarticle.app.BaseRxPresenter;
import com.nicodelee.beautyarticle.mode.ActicleMod;
import com.nicodelee.beautyarticle.mode.ActicleMod$Table;
import com.nicodelee.beautyarticle.ui.view.fragment.ActicleListFragment;
import com.raizlabs.android.dbflow.sql.language.Select;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Action2;
import rx.functions.Func0;
import rx.schedulers.Schedulers;

import static rx.android.schedulers.AndroidSchedulers.mainThread;

/**
 * Created by NocodeLee on 15/12/8.
 * Email：lirizhilirizhi@163.com
 */
public class ArticleListPresenter extends BaseRxPresenter<ActicleListFragment> {

  private static final int REQUEST_ARCICLE_ID = 1;
  private static final int REQUEST_ARCICLE_LOCAL_ID = 2;

  @Inject BeautyApi mbeautyApi;

  private int page;
  private int id;

  @Override protected void onCreate(Bundle savedState) {
    super.onCreate(savedState);

    Action2<ActicleListFragment, ArrayList<ActicleMod>> onNext =
        new Action2<ActicleListFragment, ArrayList<ActicleMod>>() {
          @Override public void call(ActicleListFragment acticleListFragment,
              ArrayList<ActicleMod> acticleMods) {
            acticleListFragment.onChangeItems(acticleMods, page);
          }
        };

    Action2<ActicleListFragment, Throwable> onError =
        new Action2<ActicleListFragment, Throwable>() {
          @Override public void call(ActicleListFragment acticleListFragment, Throwable throwable) {
            acticleListFragment.onNetworkError(throwable, page);
          }
        };

    restartableLatestCache(REQUEST_ARCICLE_ID, new Func0<Observable<ArrayList<ActicleMod>>>() {
      @Override public Observable<ArrayList<ActicleMod>> call() {
        return mbeautyApi.getActicle(page,id).subscribeOn(Schedulers.io()).observeOn(mainThread());
      }
    }, onNext, onError);

    restartableLatestCache(REQUEST_ARCICLE_LOCAL_ID, new Func0<Observable<List<ActicleMod>>>() {
      @Override public Observable<List<ActicleMod>> call() {
        return Observable.create(new Observable.OnSubscribe<List<ActicleMod>>() {
          @Override public void call(Subscriber<? super List<ActicleMod>> subscriber) {
            List<ActicleMod> acticleMods =
                new Select().from(ActicleMod.class).orderBy(false, ActicleMod$Table.ID).queryList();
            subscriber.onNext(acticleMods);
            subscriber.onCompleted();
          }
        })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
      }
    }, new Action2<ActicleListFragment, List<ActicleMod>>() {
      @Override
      public void call(ActicleListFragment acticleListFragment, List<ActicleMod> acticleMods) {
        acticleListFragment.setLocalData(acticleMods);
      }
    }, new Action2<ActicleListFragment, Throwable>() {
      @Override public void call(ActicleListFragment acticleListFragment, Throwable throwable) {

      }
    });
  }

  public void getData(int page,int id) {
    this.page = page;
    this.id = id;
    start(REQUEST_ARCICLE_ID);
  }

  public void setLocal() {
    start(REQUEST_ARCICLE_LOCAL_ID);
  }
}
