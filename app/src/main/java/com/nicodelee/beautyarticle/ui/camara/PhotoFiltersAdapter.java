package com.nicodelee.beautyarticle.ui.camara;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.nicodelee.beautyarticle.R;
import com.nicodelee.beautyarticle.utils.L;
import com.nicodelee.utils.ListUtils;
import java.util.List;
import java.util.logging.Logger;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageView;

public class PhotoFiltersAdapter
    extends RecyclerView.Adapter<PhotoFiltersAdapter.PhotoFilterViewHolder> {

  private Context context;

  List<FilterEffect> filterUris;
  private Bitmap background;

  public PhotoFiltersAdapter(Context context) {
    this.context = context;
  }

  public PhotoFiltersAdapter(Context context, List<FilterEffect> effects, Bitmap backgroud) {
    filterUris = effects;
    this.context = context;
    this.background = backgroud;
  }

  @Override public PhotoFilterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    final View view =
        LayoutInflater.from(context).inflate(R.layout.item_bottom_filter, parent, false);
    return new PhotoFilterViewHolder(view);
  }

  @Override public void onBindViewHolder(PhotoFilterViewHolder holder, final int position) {

    holder.smallFilter.setTag(position);

    final FilterEffect effect = filterUris.get(position);
    holder.filterName.setText(effect.getTitle());
    holder.smallFilter.setImage(background);
    GPUImageFilter filter = GPUImageFilterTools.createFilterForType(context, effect.getType());
    holder.smallFilter.setFilter(filter);

    holder.itemView.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        GPUImageFilter filter =
            GPUImageFilterTools.createFilterForType(context, filterUris.get(position).getType());
        PhotoProcessActivity.photoProcessActivity.gpuimage.setFilter(filter);

      }
    });
  }

  @Override public int getItemCount() {
    return ListUtils.getSize(filterUris);
  }

  public static class PhotoFilterViewHolder extends RecyclerView.ViewHolder {
    @Bind(R.id.small_filter) GPUImageView smallFilter;
    @Bind(R.id.small_filter_container) LinearLayout smallFilterContainer;
    @Bind(R.id.filter_name) TextView filterName;

    public final View mView;

    public PhotoFilterViewHolder(View view) {
      super(view);
      mView = view;
      ButterKnife.bind(this, view);
    }
  }
}
