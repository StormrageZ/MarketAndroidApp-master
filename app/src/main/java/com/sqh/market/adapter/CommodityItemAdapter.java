package com.sqh.market.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.sqh.market.R;
import com.sqh.market.models.CommodityModel;
import com.sqh.market.utils.ImageUtil;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * 要显示的商品的adapter
 *
 * @author 郑龙
 */
public class CommodityItemAdapter extends BaseAdapter {
    private Handler mHandler;
    private Context mContext;
    private List<CommodityModel> menuDatas;
    private int selectedPos = -1;
    private String mClickedItemName = null;
    private ListView listview;
    private LruCache<String, BitmapDrawable> mImageCache;
    //正常调用时的构造方法
    public CommodityItemAdapter(Context mContext, List<CommodityModel> menuDatas) {
        this.mContext = mContext;
        this.menuDatas = menuDatas;
        int maxCache = (int) Runtime.getRuntime().maxMemory();
        int cacheSize = maxCache / 6;
        mImageCache = new LruCache<String, BitmapDrawable>(cacheSize) {
              @Override
              protected int sizeOf(String key, BitmapDrawable value) {
                                 return value.getBitmap().getByteCount();
                             }
          };
        //实例化ImageLoader
        ImageLoaderConfiguration configuration = ImageLoaderConfiguration.createDefault(mContext);
        ImageLoader.getInstance().init(configuration);
    }

    //debug时使用的构造方法
    public CommodityItemAdapter(Context mContext, List<CommodityModel> menuDatas, Handler handler) {
        this.mContext = mContext;
        this.menuDatas = menuDatas;
        this.mHandler = handler;
        int maxCache = (int) Runtime.getRuntime().maxMemory();
        int cacheSize = maxCache / 8;
        mImageCache = new LruCache<String, BitmapDrawable>(cacheSize) {
            @Override
            protected int sizeOf(String key, BitmapDrawable value) {
                return value.getBitmap().getByteCount();
            }
        };
        //实例化ImageLoader
        ImageLoaderConfiguration configuration = ImageLoaderConfiguration.createDefault(mContext);
        ImageLoader.getInstance().init(configuration);
    }

    //选中的position,及时更新数据
    public void setSelectedPos(int selectedPos) {
        this.selectedPos = selectedPos;
        notifyDataSetChanged();
    }

    //绑定数据源
    public void setData(List<CommodityModel> data) {
        this.menuDatas = data;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (menuDatas == null) {
            return 0;
        }
        return menuDatas.size();
    }

    @Override
    public Object getItem(int position) {
        if (menuDatas == null) {
            return null;
        }
        return menuDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CommodityViewHolder holder;
        if (listview == null) {
                         listview = (ListView) parent;
                    }
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_commodity_for_searchview, null);

            //布局文件中所有组件的对象封装到ViewHolder对象中
            holder = new CommodityViewHolder();
            holder.commodityName = convertView.findViewById(R.id.commodityName);
            holder.commodityInfo = convertView.findViewById(R.id.commodityInfo);
            holder.commodityPrice = convertView.findViewById(R.id.commodityPrice);
            holder.img = convertView.findViewById(R.id.commodityImg);
            //把ViewHolder对象封装到View对象中
            convertView.setTag(holder);

        } else {
            holder = (CommodityViewHolder) convertView.getTag();
        }


        //获取点击的子菜单的View
        CommodityModel commodity = menuDatas.get(position);
        String name = commodity.getCommodityName();
        String info = commodity.getCommodityInfo();
        Double price = commodity.getCommodityPrice();
        String imgUrl = commodity.getCommodityOtherImgUrls();
        holder.commodityInfo.setText(info);
        holder.commodityName.setText(name);
        holder.commodityPrice.setText("￥ " + price + "元");
      //  holder.img.setTag(imgUrl);
       // holder.img.setImageResource(R.drawable.cart);
        //绑定图片到控件
       // ImageLoader.getInstance().displayImage(commodity.getCommodityOtherImgUrls(), holder.img);
//        if (mImageCache.get(imgUrl) != null) {
//                         holder.img.setImageDrawable(mImageCache.get(imgUrl));
//                         notifyDataSetChanged();
//                    } else {
           //ImageView imageView = holder.img;
            //if (imageView.getTag() != null && imageView.getTag().equals(imgUrl)) {
               // ImageTask it = new ImageTask();
               // it.execute(imgUrl);
            DisplayImageOptions configuration = new DisplayImageOptions.Builder()
                    .showImageOnLoading(R.drawable.cart)//设置加载中的图片
                    .cacheInMemory(true)//可以通过自己的内存缓存实现
                    .build();
            ImageLoader. getInstance().displayImage(imgUrl, holder.img,configuration);

//                     }
        return convertView;
    }

    /*class ImageTask extends AsyncTask<String, Void, BitmapDrawable> {

        private String imageUrl;

        @Override
        protected BitmapDrawable doInBackground(String... params) {
            imageUrl = params[0];
            Bitmap bitmap = downloadImage();
            if( bitmap == null){
                return (BitmapDrawable) ContextCompat.getDrawable(mContext, R.drawable.cart);
            }

            BitmapDrawable db = new BitmapDrawable(listview.getResources(),
                    bitmap);
            // 如果本地还没缓存该图片，就缓存
            if (mImageCache.get(imageUrl) == null) {
                mImageCache.put(imageUrl, db);
            }
            return db;
        }

        @Override
        protected void onPostExecute(BitmapDrawable result) {
            // 通过Tag找到我们需要的ImageView，如果该ImageView所在的item已被移出页面，就会直接返回null
            ImageView iv = (ImageView) listview.findViewWithTag(imageUrl);
            if (iv != null&& result != null) {
                iv.setImageDrawable(result);
                notifyDataSetChanged();
            }
            //if(result == null) return;
        }

        /**
         * 根据url从网络上下载图片
         *
         * @return
         */
       /* private Bitmap downloadImage() {
            HttpURLConnection con = null;
            Bitmap bitmap = null;
            try {
                URL url = new URL(imageUrl);
                con = (HttpURLConnection) url.openConnection();
                con.setConnectTimeout(5 * 1000);
                con.setReadTimeout(10 * 1000);
                if(con == null) return null;
                bitmap = BitmapFactory.decodeStream(con.getInputStream());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (con != null) {
                    con.disconnect();
                }
            }

            return bitmap;
        }

    }*/

}
