package lab.star.surf_iot2015.check_band_dialog;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import lab.star.surf_iot2015.R;

// Specialization for CheckBandDialog, used when the band isn't on to tell the user that sensor
// data will not be reliable if the band is off
public class CheckBandOnDialog extends CheckBandDialog {

    @Override
    public void decorateLayout(LinearLayout layout){
        ((ImageView) layout.findViewById(R.id.dialog_check_band_icon))
                .setImageResource(R.drawable.band_is_off);

        ((TextView) layout.findViewById(R.id.dialog_check_band_warning))
                .setText(R.string.dialog_check_band_on_warning);
        ((TextView) layout.findViewById(R.id.dialog_check_band_detail))
                .setText(R.string.dialog_check_band_paired_detail);
        ((TextView) layout.findViewById(R.id.dialog_check_band_request))
                .setText(R.string.dialog_check_band_paired_request);
    }
}
