package lab.star.surf_iot2015;

import android.app.Fragment;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.microsoft.band.BandClient;

// Base Fragment class for data cards: elements that are responsible for displaying sensor data
// Don't use this directly; subclass it and override onCreateView to personalize it. :D
public class DataCardFragment extends Fragment {

    private TextView valueDisplay;

    public DataCardFragment newInstance (){
        return new DataCardFragment();
    }

    // this should be called to use the Band's sensor listeners to update the DataCard's state.
    // since each entry uses a different sensor, this must be implemented on an individual level
    public void registerClient (BandClient client) throws Exception {
        throw new Exception("DataCardFragment subclass does not implement a register method.");
    }

    // retrieves the base data card layout from fragment_data_card.xml. call super.onCreateView()
    // to get this
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_data_card, container, false);

        valueDisplay = (TextView) view.findViewById(R.id.dataValue);

        return view;
    }

    // decorates the view using resource IDs. meant to be called by subclasses to decorate the View
    // returned from super.onCreateView()
    protected View decorateView(View toDecorate, int defaultValue, int units, int identifier,
                                int iconImage, int color){
        ((TextView) toDecorate.findViewById(R.id.dataValue)).setText(Integer.toString(defaultValue));
        ((TextView) toDecorate.findViewById(R.id.dataUnits)).setText(
                getResources().getString(units)
        );
        ((TextView) toDecorate.findViewById(R.id.dataIdentifier)).setText(
                getResources().getString(identifier)
        );

        ((ImageView) toDecorate.findViewById(R.id.dataIcon)).setImageResource(iconImage);

        LayerDrawable bkgd = (LayerDrawable)
                toDecorate.findViewById(R.id.dataDisplayContainer).getBackground();

        for (int layer = 0; layer < bkgd.getNumberOfLayers(); ++layer){
            ((GradientDrawable) bkgd.getDrawable(layer)).setColor(
                    getResources().getColor(color)
            );
        }
        return toDecorate;
    }

    // either updateValue should be called to update the value displayed by the fragment
    protected void updateValue (final float value){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                valueDisplay.setText(Float.toString(value));
            }
        });
    }

    protected void updateValue (final int value){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                valueDisplay.setText(Integer.toString(value));
            }
        });
    }

}
