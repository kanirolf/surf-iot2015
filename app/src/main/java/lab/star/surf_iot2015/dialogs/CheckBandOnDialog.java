package lab.star.surf_iot2015.dialogs;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import lab.star.surf_iot2015.R;

// Specialization for CheckBandDialog, used when the band isn't on to tell the user that sensor
// data will not be reliable if the band is off
public class CheckBandOnDialog extends DialogFragment {

    private Button closeButton;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){

        LinearLayout layout =
                (LinearLayout) inflater.inflate(R.layout.dialog_check_band, container, false);

        ((ImageView) layout.findViewById(R.id.dialog_check_band_icon))
                .setImageResource(R.drawable.band_is_off);

        ((TextView) layout.findViewById(R.id.dialog_check_band_warning))
                .setText(R.string.dialog_check_band_on_warning);
        ((TextView) layout.findViewById(R.id.dialog_check_band_detail))
                .setText(R.string.dialog_check_band_on_detail);
        ((TextView) layout.findViewById(R.id.dialog_check_band_request))
                .setText(R.string.dialog_check_band_on_request);

        closeButton = ((Button) layout.findViewById(R.id.dialog_check_band_close_button));

        return layout;
    }

    public void setOnClick(View.OnClickListener clickListener){
        closeButton.setOnClickListener(clickListener);
    }

}
