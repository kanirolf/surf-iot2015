package lab.star.surf_iot2015.dialogs;

import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import lab.star.surf_iot2015.R;

// Specialization for CheckBandDialog, used when the band isn't paired to pair the Band
public class CheckBandPairedDialog extends DialogFragment {

    private Callback callback;
    private Button closeButton;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setStyle(STYLE_NO_TITLE, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){

        Log.d("CheckBandPairedDialog", "band paired viewing...");
        LinearLayout layout =
                (LinearLayout) inflater.inflate(R.layout.dialog_check_band, container, false);

        ((ImageView) layout.findViewById(R.id.dialog_check_band_icon))
                .setImageResource(R.drawable.band_not_paired);

        ((TextView) layout.findViewById(R.id.dialog_check_band_warning))
                .setText(R.string.dialog_check_band_paired_warning);
        ((TextView) layout.findViewById(R.id.dialog_check_band_detail))
                .setText(R.string.dialog_check_band_paired_detail);
        ((TextView) layout.findViewById(R.id.dialog_check_band_request))
                .setText(R.string.dialog_check_band_paired_request);

        layout.findViewById(R.id.dialog_check_band_close_button)
            .setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                }
            });

        return layout;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if (callback != null){
            callback.onDismiss();
        }
    }

    public void setDismissCallback(Callback callback){
        this.callback = callback;
    }

    public interface Callback {
        void onDismiss();
    }



}
