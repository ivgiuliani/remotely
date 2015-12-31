package pw.bitset.remotely.extrakeys;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import pw.bitset.remotely.R;

public class ExtraKeysView extends FrameLayout {
    public ExtraKeysView(Context context) {
        super(context);
        init(context);
    }

    public ExtraKeysView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ExtraKeysView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        View root = inflate(context, R.layout.view_extra_keys, null);
        addView(root);
    }
}
