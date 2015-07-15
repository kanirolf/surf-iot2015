package lab.star.surf_iot2015.check_band_dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import lab.star.surf_iot2015.R;


// Base class for DialogFragments using the dialog_check_band layout. As is, the layout is empty;
// override the decorateLayout method to decorate the view before it is used in onCreateDialog.

abstract public class CheckBandDialog extends DialogFragment {

    // loads the layout at dialog_check_band.xml, passes the resulting LinearLayout to
    // the child decorateLayout(), then creates the dialog by passing the Layout to
    // the AlertDialog.Builder instance.
    public Dialog onCreateDialog(Bundle savedInstanceState){
        LinearLayout baseLayout = (LinearLayout) getActivity().getLayoutInflater().inflate(
                R.layout.dialog_check_band, null);

        decorateLayout(baseLayout);


        return new AlertDialog.Builder(getActivity())
                .setView(baseLayout)
                .setPositiveButton("ok!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .create();

    }

    // implement in a concrete child class to decorate before Dialog creation
    abstract protected void decorateLayout(LinearLayout dialogLayout);

}
