package lab.star.surf_iot2015.check_band_dialog;

import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import lab.star.surf_iot2015.R;

// Specialization for CheckBandDialog, used when the band isn't paired to prompt user to pair the
// Band, or to turn on Bluetooth, etc.
public class CheckBandPairedDialog extends CheckBandDialog {

    protected void decorateLayout(LinearLayout dialogLayout){

        ((ImageView) dialogLayout.findViewById(R.id.dialog_check_band_icon))
                .setImageResource(R.drawable.band_not_paired);

        ((TextView) dialogLayout.findViewById(R.id.dialog_check_band_warning))
                .setText(R.string.dialog_check_band_paired_warning);
        ((TextView) dialogLayout.findViewById(R.id.dialog_check_band_detail))
                .setText(R.string.dialog_check_band_paired_detail);
        ((TextView) dialogLayout.findViewById(R.id.dialog_check_band_request))
                .setText(R.string.dialog_check_band_paired_request);

    }

}
