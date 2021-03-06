package base.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import base.utils.ColorUtil;

/**
 * Created by zhangliangming on 2017/8/2.
 */
public class BaseEditTextBG extends RelativeLayout {


    public BaseEditTextBG(Context context) {
        super(context);
        init(context);
    }

    public BaseEditTextBG(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public BaseEditTextBG(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        int strokeWidth = 1; // 3dp 边框宽度
        float[] roundRadius = {15, 15, 15, 15, 15, 15, 15, 15}; // 圆角半径
        int strokeColor = Color.TRANSPARENT;
        int fillColor = ColorUtil.parserColor("#efefef", 255);

        GradientDrawable gd = new GradientDrawable();// 创建drawable
        gd.setColor(fillColor);
        gd.setCornerRadii(roundRadius);
        gd.setStroke(strokeWidth, strokeColor);
        setBackgroundDrawable(gd);

    }
}
